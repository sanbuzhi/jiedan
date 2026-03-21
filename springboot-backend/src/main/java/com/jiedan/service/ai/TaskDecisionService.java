package com.jiedan.service.ai;

import com.jiedan.dto.ai.AiChatRequest;
import com.jiedan.dto.ai.AiChatResponse;
import com.jiedan.dto.ai.AiMessage;
import com.jiedan.service.ai.VersionCollector.VersionInfo;
import com.jiedan.service.ai.prompt.AiPromptTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 任务决策者服务
 * 负责在多个AI生成的任务书版本中选择最佳版本
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskDecisionService {

    private final AiStrategyFactory strategyFactory;

    /**
     * 决策结果
     */
    public record DecisionResult(
        String selectedVersion,
        String reason,
        List<String> improvements,
        String selectedContent
    ) {}

    /**
     * 提取JSON内容
     */
    private String extractJson(String content) {
        int start = content.indexOf("{");
        int end = content.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return content.substring(start, end + 1);
        }
        return null;
    }

    /**
     * 提取字段值
     */
    private String extractField(String json, String field) {
        String pattern = "\"" + field + "\"\\s*:\\s*\"([^\"]*)\"";
        Matcher matcher = Pattern.compile(pattern).matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 提取改进建议
     */
    private List<String> extractImprovements(String json) {
        List<String> improvements = new ArrayList<>();
        String pattern = "\"improvements\"\\s*:\\s*\\[([^\\]]*)\\]";
        Matcher matcher = Pattern.compile(pattern).matcher(json);
        if (matcher.find()) {
            String arrayContent = matcher.group(1);
            Matcher itemMatcher = Pattern.compile("\"([^\"]*)\"").matcher(arrayContent);
            while (itemMatcher.find()) {
                improvements.add(itemMatcher.group(1));
            }
        }
        return improvements;
    }

    /**
     * 从带验证信息的版本中选择最佳任务书
     * 优先选择ALLOW状态的版本，其次REPAIR，最后REJECT
     *
     * @param userPrompt 用户提示词（需求文档）
     * @param versions 带验证信息的版本列表
     * @return 决策结果
     */
    public DecisionResult selectBestVersionWithValidation(String userPrompt,
                                                          List<VersionInfo> versions) {
        log.info("开始任务决策(带验证信息)，候选版本数: {}", versions.size());

        if (versions.isEmpty()) {
            throw new IllegalArgumentException("候选版本不能为空");
        }

        if (versions.size() == 1) {
            VersionInfo version = versions.get(0);
            log.info("只有一个候选版本，直接返回: {}", version.getVersionId());
            return new DecisionResult(
                    version.getVersionId(),
                    "只有一个候选版本，验证状态: " + version.getValidationDecision(),
                    List.of(),
                    version.getContent()
            );
        }

        // 按验证状态分组
        List<VersionInfo> allowVersions = versions.stream()
                .filter(v -> "ALLOW".equals(v.getValidationDecision()))
                .toList();
        List<VersionInfo> repairVersions = versions.stream()
                .filter(v -> "REPAIR".equals(v.getValidationDecision()))
                .toList();
        List<VersionInfo> rejectVersions = versions.stream()
                .filter(v -> "REJECT".equals(v.getValidationDecision()))
                .toList();

        log.info("版本分布: ALLOW={}, REPAIR={}, REJECT={}",
                allowVersions.size(), repairVersions.size(), rejectVersions.size());

        // 第一优先级：ALLOW版本
        if (!allowVersions.isEmpty()) {
            log.info("存在ALLOW版本，从中选择最佳版本");
            return selectBestFromVersions(userPrompt, allowVersions,
                    "ALLOW版本中选择最佳");
        }

        // 第二优先级：REPAIR版本
        if (!repairVersions.isEmpty()) {
            log.info("不存在ALLOW版本，从REPAIR版本中选择最佳");
            return selectBestFromVersions(userPrompt, repairVersions,
                    "REPAIR版本中选择最佳（建议人工复核）");
        }

        // 第三优先级：REJECT版本
        log.warn("不存在ALLOW/REPAIR版本，只能从REJECT版本中选择");
        return selectBestFromVersions(userPrompt, rejectVersions,
                "REJECT版本中选择相对最佳（质量不佳）");
    }

    /**
     * 从指定版本列表中选择最佳
     */
    private DecisionResult selectBestFromVersions(String userPrompt,
                                                  List<VersionInfo> versions, String selectionContext) {
        // 如果只有一个版本，直接返回
        if (versions.size() == 1) {
            VersionInfo version = versions.get(0);
            return new DecisionResult(
                    version.getVersionId(),
                    selectionContext + ": " + version.getVersionId(),
                    List.of(),
                    version.getContent()
            );
        }

        // 调用AI进行决策
        DecisionResult result = callAiForDecision(userPrompt, versions, selectionContext);
        if (result != null) {
            return result;
        }

        // AI调用失败，降级策略：返回最新版本
        VersionInfo latestVersion = versions.stream()
                .max(Comparator.comparingLong(VersionInfo::getTimestamp))
                .orElse(versions.get(0));
        return new DecisionResult(
                latestVersion.getVersionId(),
                selectionContext + "（决策失败，返回最新版本）",
                List.of(),
                latestVersion.getContent()
        );
    }

    /**
     * 调用AI进行决策，支持分段处理
     * 如果版本数量较多，采用锦标赛机制分批决策
     */
    private DecisionResult callAiForDecision(String userPrompt,
                                              List<VersionInfo> versions, String selectionContext) {
        // 如果版本数量超过3个，采用锦标赛机制
        if (versions.size() > 3) {
            log.info("版本数量超过3个，采用锦标赛机制分批决策");
            return tournamentDecision(userPrompt, versions, selectionContext);
        }

        // 直接决策
        return singleBatchDecision(userPrompt, versions, selectionContext);
    }

    /**
     * 单批次决策 - 整合内容后分批发送
     * 1. 第一轮：发送系统提示词和用户提示词，引导后续文件传送
     * 2. 整合需求文档+所有版本内容，添加"候选版本发送完毕！"
     * 3. 分割内容，按批次发送（不超过单次token上限）
     * 4. 最后批次获取结果并解析
     */
    private DecisionResult singleBatchDecision(String userPrompt,
                                                List<VersionInfo> versions, String selectionContext) {
        log.info("使用整合内容分批发送方式进行决策，版本数: {}", versions.size());

        AIProviderStrategy strategy = strategyFactory.getStrategy(null);
        List<AiMessage> messages = new ArrayList<>();

        String systemPrompt = buildDecisionSystemPrompt();
        messages.add(AiMessage.system(systemPrompt));

        String introPrompt = buildDecisionIntroPrompt(versions);
        messages.add(AiMessage.user(introPrompt));

        String aiResponse = sendMessageAndGetResponse(strategy, messages, 8000);
        if (aiResponse == null) {
            return null;
        }
        messages.add(AiMessage.assistant(aiResponse));
        log.info("【第1轮】系统提示+intro发送完成，AI响应: {}", truncateForLog(aiResponse));

        // 整合需求文档、多版本任务书
        String combinedContent = buildCombinedContent(userPrompt, versions);
        List<String> batches = splitContentIntoBatches(combinedContent, 20000);

        for (int i = 0; i < batches.size(); i++) {
            String batchPrompt = "【第" + (i + 1) + "批内容（共" + batches.size() + "批）】\n" + batches.get(i);
            messages.add(AiMessage.user(batchPrompt));

            aiResponse = sendMessageAndGetResponse(strategy, messages, 5000);
            if (aiResponse == null) {
                return null;
            }
            messages.add(AiMessage.assistant(aiResponse));
            log.info("【第{}批】内容发送完成，AI响应: {}", i + 2, truncateForLog(aiResponse));
        }

        String decisionPrompt = buildFinalDecisionPrompt(versions);
        messages.add(AiMessage.user(decisionPrompt));

        aiResponse = sendMessageAndGetResponse(strategy, messages, 8000);
        if (aiResponse == null) {
            return null;
        }
        messages.add(AiMessage.assistant(aiResponse));
        log.info("【最终轮】决策请求发送完成，AI响应: {}", truncateForLog(aiResponse));

        return parseDecisionResultWithValidation(aiResponse, versions, selectionContext);
    }

    /**
     * 将需求文档和所有版本内容整合为一个字符串
     */
    private String buildCombinedContent(String userPrompt, List<VersionInfo> versions) {
        StringBuilder sb = new StringBuilder();
        sb.append("【用户需求文档】\n").append(userPrompt).append("\n\n");
        sb.append("【候选版本内容】\n");
        for (VersionInfo version : versions) {
            sb.append("=== 版本：").append(version.getVersionId()).append(" ===\n");
            sb.append("【版本内容】\n").append(version.getContent()).append("\n\n");
        }
        return sb.toString();
    }

    /**
     * 将内容分割为多个批次，每批不超过指定字符数
     */
    private List<String> splitContentIntoBatches(String content, int maxCharsPerBatch) {
        List<String> batches = new ArrayList<>();
        if (content.length() <= maxCharsPerBatch) {
            batches.add(content);
            return batches;
        }

        String[] lines = content.split("\n");
        StringBuilder currentBatch = new StringBuilder();

        for (String line : lines) {
            if (currentBatch.length() + line.length() + 1 > maxCharsPerBatch && currentBatch.length() > 0) {
                batches.add(currentBatch.toString());
                currentBatch = new StringBuilder();
            }
            currentBatch.append(line).append("\n");
        }

        if (currentBatch.length() > 0) {
            batches.add(currentBatch.toString());
        }

        log.info("内容分割完成，共 {} 批，总字符数: {}", batches.size(), content.length());
        return batches;
    }

    /**
     * 截断日志内容
     */
    private String truncateForLog(String content) {
        if (content == null) return "null";
        int len = Math.min(200, content.length());
        return content.substring(0, len) + (content.length() > len ? "..." : "");
    }

    /**
     * 构建决策系统提示词
     */
    private String buildDecisionSystemPrompt() {
        return AiPromptTemplate.TASK_DECISION_SYSTEM;
    }

    /**
     * 构建初始提示词
     */
    private String buildDecisionIntroPrompt(List<VersionInfo> versions) {
        String versionList = versions.stream()
                .map(VersionInfo::getVersionId)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        return "【任务说明】\n" +
                "我需要从多个候选版本中选择最佳任务书。\n\n" +
                "【候选版本信息】\n" +
                "共有 " + versions.size() + " 个候选版本：\n" +
                "版本列表：" + versionList + "\n\n" +
                "【发送内容】\n" +
                "后续我将分批发送：\n" +
                "1. 需求文档（用户原始需求）\n" +
                "2. 所有候选版本的完整内容\n\n" +
                "【重要规则】\n" +
                "1. 内容是分批发送的，请耐心等待全部批次接收完成\n" +
                "2. 收到\"候选版本发送完毕！\"指令后，才能进行决策\n" +
                "3. 如果发现内容不完整或被截断，请在reason中说明\n";
    }

    /**
     * 构建版本内容提示词
     */
    private String buildVersionPrompt(VersionInfo version, int currentNum, int totalNum) {
        return "=== 候选版本 " + currentNum + "/" + totalNum + " ===\n" +
                "版本ID: " + version.getVersionId() + "\n" +
                "【版本内容开始】\n" +
                version.getContent() + "\n" +
                "【版本内容结束】\n\n";
    }

    /**
     * 构建最终决策提示词
     */
    private String buildFinalDecisionPrompt(List<VersionInfo> versions) {
        return "\n\n以上是全部 " + versions.size() + " 个候选版本的任务书内容。" +
                "请按系统提示词的要求从以上版本中选择最佳版本\n\n"+
                "【输出要求】\n" +
                "输出完整可解析的JSON格式：\n" +
                "{\n" +
                "  \"selectedVersion\": \"选中的版本号\",\n" +
                "  \"reason\": \"详细的选择理由，包括与其他版本的对比\",\n" +
                "  \"improvements\": [\"改进建议1\", \"改进建议2\"]\n" +
                "}\n\n" +
                "候选版本发送完毕！";
    }

    /**
     * 发送消息并获取响应
     */
    private String sendMessageAndGetResponse(AIProviderStrategy strategy, List<AiMessage> messages, int maxTokens) {
        // 上下文压缩：15万字/20000 ≈ 8批，总消息约14-16条，阈值设为12
        if (messages.size() > 12) {
            log.info("上下文消息过多({})，进行压缩", messages.size());
            List<AiMessage> compressedMessages = new ArrayList<>();
            // 保留系统提示
            compressedMessages.add(messages.get(0));
            // 保留intro（第2条）
            compressedMessages.add(messages.get(1));
            // 保留最近对话（压缩到8条，即4轮）
            int startIdx = Math.max(2, messages.size() - 8);
            for (int i = startIdx; i < messages.size(); i++) {
                compressedMessages.add(messages.get(i));
            }
            messages = compressedMessages;
            log.info("上下文压缩完成，当前消息数: {}", messages.size());
        }

        AiChatRequest chatRequest = AiChatRequest.builder()
                .model(null)
                .temperature(0.3)
                .maxTokens(maxTokens)
                .messages(new ArrayList<>(messages))
                .build();

        AiChatResponse response = strategy.chatCompletion(chatRequest);

        if (!Boolean.TRUE.equals(response.getSuccess())) {
            log.error("AI调用失败: {}", response.getErrorMessage());
            return null;
        }

        return response.getContent();
    }

    /**
     * 锦标赛决策机制 - 支持第一轮并行执行
     * 将版本分批比较，最终选出最佳版本
     */
    private DecisionResult tournamentDecision(String userPrompt,
                                               List<VersionInfo> versions, String selectionContext) {
        log.info("开始锦标赛决策，总版本数: {}", versions.size());

        List<VersionInfo> currentRound = new ArrayList<>(versions);
        int round = 1;

        while (currentRound.size() > 3) {
            log.info("锦标赛第{}轮，候选版本数: {}", round, currentRound.size());
            List<VersionInfo> winners;

            // 第一轮：并行执行决策
            if (round == 1) {
                log.info("第1轮：并行执行决策，共 {} 组", (currentRound.size() + 2) / 3);
                winners = parallelBatchDecision(userPrompt, currentRound, round, selectionContext);
            } else {
                // 后续轮次：串行执行
                winners = serialBatchDecision(userPrompt, currentRound, round, selectionContext);
            }

            currentRound = winners;
            round++;
        }

        // 最终轮：从剩余的3个或更少版本中选出最佳
        log.info("锦标赛最终轮，候选版本数: {}", currentRound.size());
        DecisionResult finalResult = singleBatchDecision(userPrompt, currentRound,
                selectionContext + " - 决赛轮");

        if (finalResult != null) {
            return finalResult;
        }

        // 最终决策失败，返回最新版本
        VersionInfo latestVersion = currentRound.stream()
                .max(Comparator.comparingLong(VersionInfo::getTimestamp))
                .orElse(currentRound.get(0));
        return new DecisionResult(
                latestVersion.getVersionId(),
                selectionContext + "（锦标赛决策失败，返回最新版本）",
                List.of(),
                latestVersion.getContent()
        );
    }

    /**
     * 并行批量决策 - 第一轮使用
     * 将版本分成多组，每组并行执行决策
     */
    private List<VersionInfo> parallelBatchDecision(String userPrompt,
                                                    List<VersionInfo> currentRound,
                                                    int round,
                                                    String selectionContext) {
        // 将版本分成每3个一组
        List<List<VersionInfo>> batches = new ArrayList<>();
        for (int i = 0; i < currentRound.size(); i += 3) {
            int end = Math.min(i + 3, currentRound.size());
            batches.add(currentRound.subList(i, end));
        }

        // 使用CompletableFuture并行执行每组的决策
        List<java.util.concurrent.CompletableFuture<VersionInfo>> futures = batches.stream()
                .map(batch -> java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                    if (batch.size() == 1) {
                        return batch.get(0);
                    }
                    DecisionResult result = singleBatchDecision(userPrompt, batch,
                            selectionContext + " - 第" + round + "轮");
                    if (result != null) {
                        return batch.stream()
                                .filter(v -> v.getVersionId().equals(result.selectedVersion()))
                                .findFirst()
                                .orElse(batch.get(0));
                    } else {
                        return batch.stream()
                                .max(Comparator.comparingLong(VersionInfo::getTimestamp))
                                .orElse(batch.get(0));
                    }
                }))
                .toList();

        // 等待所有并行任务完成
        List<VersionInfo> winners = futures.stream()
                .map(java.util.concurrent.CompletableFuture::join)
                .toList();

        log.info("第{}轮并行决策完成，晋级版本数: {}", round, winners.size());
        return winners;
    }

    /**
     * 串行批量决策 - 后续轮次使用
     */
    private List<VersionInfo> serialBatchDecision(String userPrompt,
                                                  List<VersionInfo> currentRound,
                                                  int round,
                                                  String selectionContext) {
        List<VersionInfo> winners = new ArrayList<>();

        for (int i = 0; i < currentRound.size(); i += 3) {
            int end = Math.min(i + 3, currentRound.size());
            List<VersionInfo> batch = currentRound.subList(i, end);

            if (batch.size() == 1) {
                winners.add(batch.get(0));
            } else {
                DecisionResult result = singleBatchDecision(userPrompt, batch,
                        selectionContext + " - 第" + round + "轮");
                if (result != null) {
                    VersionInfo winner = batch.stream()
                            .filter(v -> v.getVersionId().equals(result.selectedVersion()))
                            .findFirst()
                            .orElse(batch.get(0));
                    winners.add(winner);
                } else {
                    VersionInfo latest = batch.stream()
                            .max(Comparator.comparingLong(VersionInfo::getTimestamp))
                            .orElse(batch.get(0));
                    winners.add(latest);
                }
            }
        }

        log.info("第{}轮串行决策完成，晋级版本数: {}", round, winners.size());
        return winners;
    }

    /**
     * 构建带验证信息的决策Prompt
     */
    private String buildDecisionPromptWithValidation(String userPrompt,
                                                      List<VersionInfo> versions) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("【对任务书的严格要求】\n");
        String taskMdMustNeed = """
        1. 格式：Markdown
        2. 语言：中文
        3. 结构：必须包含以下7个章节，缺一不可：
           ## 1. 项目技术规格
           - 技术栈（同步需求文档的技术栈，如果需求文档没有则需要自行分析。含具体版本号，如Spring Boot 2.7.18）
           - 项目结构规范（各端目录结构（视技术栈决定））
           - 代码生成规则（依赖版本、校验规则、JWT配置等）
           ## 2. 前端页面开发清单
           - 按模块列出所有页面（页面路径、核心功能、必用组件、接口调用、跳转关系）
           ## 3. 后端接口开发清单
           - 按模块列出所有接口（URL、请求方式、请求参数、响应格式、业务逻辑、关联数据库表）
           ## 4. 数据库表结构设计
           - 所有业务表的字段定义（字段名、数据类型、约束、默认值、注释）
           - 主键、外键、索引
           - 表间关联关系
           ## 5. 业务逻辑规则
           - 核心业务流程
           - 数据校验规则
           - 状态流转规则
           - 异常处理规则
           ## 6. 开发执行顺序
           - 阶段划分（数据库→后端→前端→联调）
           - 依赖关系
           - 执行优先级
           ## 7. 代码生成规范
           - 命名规范（类名、方法名、变量名、表名、字段名）
           - 代码结构（Controller/Service/Mapper分层）
           - 注释要求
           - 错误码定义
        【质量标准】
        - 所有指令可直接映射为代码逻辑
        - 无冗余描述，每句话都指向代码实现
        - 文档结构完整，可被TaskDocumentParser解析
        - 项目类型与需求文档完全一致
        """;
        prompt.append(taskMdMustNeed).append("\n\n");

        prompt.append("【用户需求文档】\n");
        prompt.append(userPrompt).append("\n\n");

        prompt.append("【候选版本】\n");
        prompt.append("共有 ").append(versions.size()).append(" 个候选版本:\n\n");

        for (VersionInfo version : versions) {
            prompt.append("=== ").append(version.getVersionId()).append(" ===\n");
            prompt.append("验证状态: ").append(version.getValidationDecision()).append("\n");
            prompt.append("---\n");
            prompt.append(version.getContent()).append("\n\n");
        }

        prompt.append("【比较方法 - 请按以下步骤执行】\n");
        prompt.append("第一步：快速检查每个版本的验证状态（ALLOW > REPAIR > REJECT）\n");
        prompt.append("第二步：逐一检查每个版本是否包含全部 7 个章节\n");
        prompt.append("第三步：对比各版本的技术规格是否与需求文档一致（特别是项目类型）\n");
        prompt.append("第四步：评估数据库设计的完整性（字段、类型、约束是否详细）\n");
        prompt.append("第五步：检查前后端开发清单是否详细（页面路径、接口 URL 是否具体）\n");
        prompt.append("第六步：综合以上因素选择最佳版本\n\n");

        prompt.append("【重点关注 - 一票否决项】\n");
        prompt.append("❌ 项目类型与需求文档不一致（如需求是美妆店管理系统，任务书却是宠物社区/校园二手书等）\n");
        prompt.append("❌ 缺少 7 个必要章节中的任何一个\n");
        prompt.append("❌ 技术栈与需求文档冲突\n");
        prompt.append("✅ 优先选择：项目类型完全匹配 + 7 章节完整 + 技术规格详细 + 验证状态为 ALLOW\n\n");

        prompt.append("【输出要求】\n");
        prompt.append("请严格按照以下 JSON 格式输出（不要包含 Markdown 代码块标记）：\n");
        prompt.append("{\n");
        prompt.append("  \"selectedVersion\": \"V1-1\",\n");
        prompt.append("  \"reason\": \"详细说明为什么选择这个版本，包括与其他版本的对比分析\",\n");
        prompt.append("  \"improvements\": [\"改进建议 1\", \"改进建议 2\"]\n");
        prompt.append("}\n\n");

        prompt.append("【要求】\n");
        prompt.append("请根据决策维度分析以上候选版本，选择最佳版本。\n");
        prompt.append("注意：所有候选版本都已经过 Feedback Shadow 验证，请优先考虑用户需求匹配度和文档完整性。\n");
        prompt.append("输出格式必须是 JSON，包含以下字段:\n");
        prompt.append("- selectedVersion: 选中的版本号（如 V1-1）\n");
        prompt.append("- reason: 选择理由\n");
        prompt.append("- improvements: 改进建议列表\n");

        return prompt.toString();
    }

    /**
     * 解析带验证信息的决策结果
     */
    private DecisionResult parseDecisionResultWithValidation(String content, List<VersionInfo> versions,
                                                               String selectionContext) {
        try {
            // 尝试从JSON格式解析
            String json = extractJson(content);
            if (json != null) {
                String selectedVersion = extractField(json, "selectedVersion");
                String reason = extractField(json, "reason");

                // 查找选中的版本
                VersionInfo selected = versions.stream()
                        .filter(v -> v.getVersionId().equals(selectedVersion))
                        .findFirst()
                        .orElse(null);

                if (selected != null) {
                    log.info("决策完成，选中版本: {}, 理由: {}", selectedVersion, reason);
                    return new DecisionResult(
                            selectedVersion,
                            selectionContext + ": " + (reason != null ? reason : "AI决策选择"),
                            extractImprovements(json),
                            selected.getContent()
                    );
                }
            }

            // 解析失败，返回最新版本
            log.warn("决策结果解析失败，返回最新版本");
            VersionInfo latestVersion = versions.stream()
                    .max(Comparator.comparingLong(VersionInfo::getTimestamp))
                    .orElse(versions.get(0));
            return new DecisionResult(
                    latestVersion.getVersionId(),
                    selectionContext + "（决策解析失败，返回最新版本）",
                    List.of(),
                    latestVersion.getContent()
            );

        } catch (Exception e) {
            log.error("解析决策结果异常", e);
            VersionInfo latestVersion = versions.stream()
                    .max(Comparator.comparingLong(VersionInfo::getTimestamp))
                    .orElse(versions.get(0));
            return new DecisionResult(
                    latestVersion.getVersionId(),
                    selectionContext + "（决策解析异常: " + e.getMessage() + "）",
                    List.of(),
                    latestVersion.getContent()
            );
        }
    }
}
