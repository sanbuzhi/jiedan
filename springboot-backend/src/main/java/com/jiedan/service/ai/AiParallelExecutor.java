package com.jiedan.service.ai;

import com.jiedan.dto.ai.SplitTasksRequest;
import com.jiedan.dto.ai.SplitTasksResponse;
import com.jiedan.service.ai.TaskDecisionService.DecisionResult;
import com.jiedan.service.ai.VersionCollector.VersionInfo;
import com.jiedan.service.ai.feedback.FeedbackShadowService;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AI并行任务执行器
 * 【改造】集成Feedback Shadow验证，每个并行任务内部进行验证和重试
 * 【解决循环依赖】不作为Spring Bean，由AiService直接实例化使用
 * 【多轮对话】支持携带上一轮内容进行针对性修改
 */
@Slf4j
public class AiParallelExecutor {

    /**
     * 自定义函数式接口，支持4个参数：请求、版本ID、上一轮内容、上一轮问题列表
     */
    @FunctionalInterface
    public interface TaskExecutorFunction {
        SplitTasksResponse apply(SplitTasksRequest request, String versionId, 
                                 String previousContent, List<String> previousIssues);
    }

    private final TaskDecisionService taskDecisionService;
    private final VersionCollector versionCollector;
    private final FeedbackShadowService feedbackShadowService;
    private final AiRetryService aiRetryService;
    private final SessionManager sessionManager;
    private final AiStrategyFactory strategyFactory;
    
    // 【关键】通过函数式接口回调AiService的方法，避免循环依赖
    private final TaskExecutorFunction taskExecutor;

    // 线程池配置
    private static final int PARALLEL_COUNT = 3;  // 并行任务数
    private static final long TASK_TIMEOUT_MS = 900000;  // 10分钟超时
    private static final long POLL_INTERVAL_MS = 1000;  // 1秒轮询间隔

    // 线程池
    private final ExecutorService executorService = Executors.newFixedThreadPool(PARALLEL_COUNT);

    /**
     * 构造函数 - 由AiService直接调用
     */
    public AiParallelExecutor(
            TaskDecisionService taskDecisionService,
            VersionCollector versionCollector,
            FeedbackShadowService feedbackShadowService,
            AiRetryService aiRetryService,
            SessionManager sessionManager,
            AiStrategyFactory strategyFactory,
            TaskExecutorFunction taskExecutor) {
        this.taskDecisionService = taskDecisionService;
        this.versionCollector = versionCollector;
        this.feedbackShadowService = feedbackShadowService;
        this.aiRetryService = aiRetryService;
        this.sessionManager = sessionManager;
        this.strategyFactory = strategyFactory;
        this.taskExecutor = taskExecutor;
    }

    /**
     * 并行执行结果
     */
    public record ParallelResult(
            boolean success,
            String content,
            String selectedVersion,
            String decisionReason,
            List<String> improvements,
            Map<String, String> allVersions
    ) {}

    /**
     * 并行执行AI拆分任务
     * 【改造】每个并行任务内部进行Feedback Shadow验证和重试
     */
    public ParallelResult executeSplitTasksParallel(
            SplitTasksRequest request,
            String systemPrompt,
            String userPrompt) {

        String projectId = request.getProjectId();
        log.info("========== 开始并行执行AI拆分任务 ==========, projectId: {}, thread: {}", projectId, Thread.currentThread().getName());

        // 初始化版本收集器
        versionCollector.initProject(projectId);

        // 用于标记是否有任务生成ALLOW版本
        AtomicBoolean allowVersionFound = new AtomicBoolean(false);
        AtomicReference<VersionInfo> bestAllowVersion = new AtomicReference<>();
        CountDownLatch completionLatch = new CountDownLatch(PARALLEL_COUNT);

        // 提交并行任务
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 1; i <= PARALLEL_COUNT; i++) {
            final int taskIndex = i;
            Future<?> future = executorService.submit(() -> {
                try {
                    executeSingleTaskWithFeedback(projectId, taskIndex, systemPrompt, userPrompt,
                            allowVersionFound, bestAllowVersion);
                } finally {
                    completionLatch.countDown();
                }
            });
            futures.add(future);
        }

        // 启动监控线程，检查是否有ALLOW版本生成
        Thread monitorThread = new Thread(() -> {
            while (!allowVersionFound.get() && completionLatch.getCount() > 0) {
                try {
                    Thread.sleep(POLL_INTERVAL_MS);

                    // 检查版本收集器中是否有ALLOW版本
                    List<VersionInfo> allowVersions = versionCollector.getAllowVersions(projectId);
                    if (!allowVersions.isEmpty()) {
                        // 获取最新的ALLOW版本
                        VersionInfo latestAllow = allowVersions.stream()
                                .max(Comparator.comparingLong(VersionInfo::getTimestamp))
                                .orElse(null);

                        if (latestAllow != null) {
                            log.info("检测到ALLOW版本生成: {}", latestAllow.getVersionId());
                            bestAllowVersion.set(latestAllow);
                            allowVersionFound.set(true);

                            // 取消其他任务
                            futures.forEach(f -> f.cancel(true));
                            break;
                        }
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        monitorThread.start();

        try {
            // 等待所有任务完成或超时
            boolean allCompleted = completionLatch.await(TASK_TIMEOUT_MS, TimeUnit.MILLISECONDS);

            if (!allCompleted) {
                log.warn("并行任务超时，部分任务未完成");
                futures.forEach(f -> f.cancel(true));
            }

            // 如果已经找到ALLOW版本，直接返回
            if (allowVersionFound.get()) {
                VersionInfo allowVersion = bestAllowVersion.get();
                if (allowVersion != null) {
                    log.info("返回ALLOW版本: {}, 内容长度: {}",
                            allowVersion.getVersionId(), allowVersion.getContent().length());

                    // 保存为最终文档
                    saveFinalDocument(projectId, allowVersion.getContent());

                    return new ParallelResult(
                            true,
                            allowVersion.getContent(),
                            allowVersion.getVersionId(),
                            "Feedback Shadow验证通过",
                            List.of(),
                            versionCollector.buildVersionContentMap(projectId)
                    );
                }
            }

            // 【重要】并行任务完成后，从文件系统中扫描所有版本文件
            log.info("开始扫描版本文件目录...");
            loadVersionsFromFileSystem(projectId);
            log.info("版本文件扫描完成，当前收集器版本数: {}", versionCollector.getAllVersions(projectId).size());

            // 获取所有版本
            Map<String, String> allVersions = versionCollector.buildVersionContentMap(projectId);
            
            // 调试：打印所有版本信息
            log.info("所有版本详情: {}", versionCollector.getAllVersions(projectId).stream()
                    .map(v -> v.getVersionId() + "(" + v.getValidationDecision() + ")")
                    .toList());

            // 如果没有生成任何版本，返回失败
            if (allVersions.isEmpty()) {
                log.error("未生成任何版本");
                return new ParallelResult(false, null, null, null, List.of(), Map.of());
            }

            // 调用任务决策者选择最佳版本
            log.info("调用任务决策者选择最佳版本，候选版本数: {}", allVersions.size());

            // 构建带验证信息的决策Prompt
            Map<String, Object> versionStats = versionCollector.getVersionStats(projectId);
            log.info("版本统计: ALLOW={}, REPAIR={}, REJECT={}",
                    versionStats.get("allowCount"),
                    versionStats.get("repairCount"),
                    versionStats.get("rejectCount"));

            log.info("准备调用任务决策服务，版本列表: {}", versionCollector.getAllVersions(projectId).stream()
                    .map(v -> v.getVersionId() + "(" + v.getValidationDecision() + ")")
                    .toList());

            DecisionResult decision = taskDecisionService.selectBestVersionWithValidation(
                    userPrompt, versionCollector.getAllVersions(projectId));

            log.info("任务决策完成，选中版本: {}, 理由: {}, selectedContent长度: {}",
                    decision.selectedVersion(), decision.reason(), 
                    decision.selectedContent() != null ? decision.selectedContent().length() : 0);

            // 保存最终文档
            log.info("准备保存最终文档到task目录, content长度: {}", 
                    decision.selectedContent() != null ? decision.selectedContent().length() : 0);
            saveFinalDocument(projectId, decision.selectedContent());
            log.info("最终文档保存完成");

            return new ParallelResult(
                    true,
                    decision.selectedContent(),
                    decision.selectedVersion(),
                    decision.reason(),
                    decision.improvements(),
                    allVersions
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("并行执行被中断", e);
            futures.forEach(f -> f.cancel(true));
            return new ParallelResult(false, null, null, null, List.of(), Map.of());

        } finally {
            monitorThread.interrupt();
            // 清理版本收集器
            versionCollector.clearProject(projectId);
        }
    }

    /**
     * 【改造】执行单个任务，内部进行Feedback Shadow验证和重试
     * 【真正多轮会话】使用SessionManager维持对话历史
     */
    private void executeSingleTaskWithFeedback(String projectId, int taskIndex, String systemPrompt, String userPrompt,
                                               AtomicBoolean allowVersionFound, AtomicReference<VersionInfo> bestAllowVersion) {
        try {
            log.info("启动并行任务 #{}, projectId: {}", taskIndex, projectId);

            // 为每个任务创建独立的会话ID
            String sessionId = sessionManager.createSession(projectId, "split-tasks-V" + taskIndex, systemPrompt);
            
            // 添加初始用户消息（需求文档）
            sessionManager.addUserMessage(sessionId, userPrompt);

            // 用于传递版本ID
            AtomicReference<String> versionIdRef = new AtomicReference<>();

            // 使用重试服务执行（真正多轮会话模式），内部包含Feedback Shadow验证
            // Feedback Shadow使用独立会话，不共享对话历史
            // 回调函数：每次AI生成内容后立即保存版本文件
            SplitTasksResponse response = null;
            try {
                response = aiRetryService.executeWithRetryAndSession(
                        sessionManager,
                        sessionId,
                        strategyFactory,
                        projectId,
                        "split-tasks",
                        taskIndex,
                        16000,  // maxTokens
                        0.7,    // temperature
                        (splitResponse) -> {
                            // 每次生成内容后只保存版本文件到文件系统
                            // 【重要】不在这里操作收集器，等并行任务结束后再扫描文件加载
                            if (splitResponse != null && splitResponse.getDocumentContent() != null) {
                                int retryCount = splitResponse.getRetryCount() > 0 ? splitResponse.getRetryCount() : 1;
                                String vid = "V" + taskIndex + "-" + retryCount;
                                versionIdRef.set(vid);
                                try {
                                    // 只保存文件
                                    saveDocumentVersionWithVersionId(projectId, "split-tasks",
                                            splitResponse.getDocumentContent(), vid, false);
                                    log.info("任务 #{} 第{}次生成，版本文件已保存: {}", taskIndex, retryCount, vid);
                                } catch (Exception e) {
                                    log.error("保存版本文件失败: {}", e.getMessage(), e);
                                }
                            }
                        }
                );
            } catch (Exception e) {
                log.error("任务 #{} 执行失败: {}", taskIndex, e.getMessage(), e);
            }
            
            // 防御性检查：确保 response 不为 null
            if (response == null) {
                log.error("任务 #{} 返回null，创建空响应", taskIndex);
                response = SplitTasksResponse.builder()
                        .success(false)
                        .errorMessage("AI执行失败，返回null")
                        .build();
            }
            
            // 计算版本ID（基于重试次数）- 使用响应中的retryCount
            int retryCount = response.getRetryCount() > 0 ? response.getRetryCount() : 1;
            String versionId = "V" + taskIndex + "-" + retryCount;
            versionIdRef.set(versionId);
            
            log.info("任务 #{} 版本ID计算: retryCount={}, versionId={}", taskIndex, retryCount, versionId);

            log.info("任务 #{} 完成, validationDecision={}, 当前收集器版本数: {}", 
                    taskIndex, response.getValidationDecision(), versionCollector.getAllVersions(projectId).size());

            // 清理会话
            sessionManager.closeSession(sessionId);

        } catch (Exception e) {
            log.error("并行任务 #{} 执行异常", taskIndex, e);
        }
    }

    /**
     * 保存文档版本
     */
    private void saveDocumentVersionWithVersionId(String projectId, String apiType,
                                                String content, String versionId, boolean isFeedback) {
        try {
            Path feedbackDir = Paths.get("projects", projectId, "feedback", apiType);
            if (!isFeedback) {
                feedbackDir = Paths.get("projects", projectId, "feedback", "split-tasks");
            }
            Files.createDirectories(feedbackDir);
            
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd-HHmmss")
                    .format(new java.util.Date());
            String fileName = versionId + "-" + timestamp + "-" + 
                    java.util.UUID.randomUUID().toString().substring(0, 8) + ".md";
            
            Path filePath = feedbackDir.resolve(fileName);
            Files.writeString(filePath, content);
            log.info("文档版本已保存: {}", filePath);
        } catch (Exception e) {
            log.error("保存文档版本失败: {}", e.getMessage(), e);
            throw new RuntimeException("保存文档版本失败", e);
        }
    }

    /**
     * 保存最终文档
     */
    private void saveFinalDocument(String projectId, String content) {
        // 防御性检查
        if (content == null || content.isEmpty()) {
            log.error("保存最终文档失败: content为空, projectId: {}", projectId);
            return;
        }
        
        try {
            Path taskDir = Paths.get("projects", projectId, "task");
            Files.createDirectories(taskDir);
            Path finalFile = taskDir.resolve("TASKS.md");
            Files.writeString(finalFile, content);
            log.info("最终文档已保存: {}, 内容长度: {}", finalFile, content.length());
        } catch (Exception e) {
            log.error("保存最终文档失败", e);
        }
    }

    /**
     * 关闭执行器
     */
    public void shutdown() {
        log.info("关闭并行执行器");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 从文件系统中扫描所有版本文件并加载到收集器
     * 在并行任务全部完成后调用，避免线程安全问题
     */
    private void loadVersionsFromFileSystem(String projectId) {
        try {
            Path feedbackDir = Paths.get("projects", projectId, "feedback", "split-tasks");
            if (!Files.exists(feedbackDir)) {
                log.warn("版本文件目录不存在: {}", feedbackDir);
                return;
            }

            // 扫描所有 V*.md 文件
            DirectoryStream<Path> stream = Files.newDirectoryStream(feedbackDir, "V*.md");
            int loadedCount = 0;
            for (Path file : stream) {
                try {
                    String fileName = file.getFileName().toString();
                    // 解析版本ID：V1-1-20260318-123456-abc.md -> V1-1
                    String versionId = fileName.split("-")[0] + "-" + fileName.split("-")[1];
                    
                    // 读取文件内容
                    String content = Files.readString(file);
                    
                    // 从Feedback报告获取验证决策
                    String decision = getValidationDecisionFromFeedback(projectId, versionId);
                    
                    // 添加到收集器
                    VersionCollector.VersionInfo versionInfo = new VersionCollector.VersionInfo(versionId);
                    versionInfo.setContent(content);
                    versionInfo.setValidationDecision(decision);
                    versionCollector.addVersion(projectId, versionInfo);
                    loadedCount++;
                    log.info("已加载版本文件: {}, versionId: {}, decision: {}", fileName, versionId, decision);
                } catch (Exception e) {
                    log.error("加载版本文件失败: {}", file.getFileName(), e);
                }
            }
            stream.close();
            log.info("版本文件扫描完成，共加载 {} 个版本", loadedCount);
        } catch (Exception e) {
            log.error("扫描版本文件目录失败", e);
        }
    }

    /**
     * 从Feedback报告获取验证决策
     */
    private String getValidationDecisionFromFeedback(String projectId, String versionId) {
        try {
            // 解析versionId: V{taskIndex}-{retryCount}
            String[] parts = versionId.replace("V", "").split("-");
            if (parts.length < 2) {
                return "UNKNOWN";
            }
            int taskNum = Integer.parseInt(parts[0]);
            int retryNum = Integer.parseInt(parts[1]);

            // Feedback报告现在保存在 projects/{projectId}/feedback/split-tasks/ 目录下
            Path feedbackDir = Paths.get("projects", projectId, "feedback", "split-tasks");
            if (!Files.exists(feedbackDir)) {
                log.warn("Feedback目录不存在: {}", feedbackDir);
                return "UNKNOWN";
            }

            // 查找对应的Feedback报告文件
            // 文件名格式：FB-V{taskIndex}-{retryCount}-{timestamp}-{uuid}.md
            DirectoryStream<Path> stream = Files.newDirectoryStream(feedbackDir, "FB-V*.md");
            Path bestMatch = null;
            long bestTime = Long.MAX_VALUE;

            for (Path fbFile : stream) {
                String fbName = fbFile.getFileName().toString();
                if (fbName.startsWith("FB-V")) {
                    String[] fbParts = fbName.split("-");
                    if (fbParts.length >= 4) {
                        try {
                            // FB-V{taskIndex}-{retryCount}-{timestamp}-{uuid}.md
                            // fbParts[1] = "V{taskIndex}", fbParts[2] = "{retryCount}"
                            int fbTaskNum = Integer.parseInt(fbParts[1].replace("V", ""));
                            int fbRetryNum = Integer.parseInt(fbParts[2]);
                            
                            // 匹配taskIndex和retryCount
                            if (fbTaskNum == taskNum && fbRetryNum == retryNum) {
                                String timeStr = fbParts[3];
                                long fbTime = Long.parseLong(timeStr);
                                if (fbTime < bestTime) {
                                    bestTime = fbTime;
                                    bestMatch = fbFile;
                                }
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
            stream.close();

            if (bestMatch != null) {
                String content = Files.readString(bestMatch);
                // 从Feedback内容中提取决策
                if (content.contains("ALLOW")) {
                    return "ALLOW";
                } else if (content.contains("REPAIR")) {
                    return "REPAIR";
                } else if (content.contains("REJECT")) {
                    return "REJECT";
                }
            }
        } catch (Exception e) {
            log.warn("获取验证决策失败: {}", e.getMessage());
        }
        return "UNKNOWN";
    }
}
