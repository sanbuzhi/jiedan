package com.jiedan.service.ai;

import com.jiedan.dto.ai.*;
import com.jiedan.dto.ai.feedback.FeedbackShadowValidateRequest;
import com.jiedan.dto.ai.feedback.FeedbackShadowValidateResponse;
import com.jiedan.dto.ai.feedback.ValidationDecision;
import com.jiedan.entity.Requirement;
import com.jiedan.repository.RequirementRepository;
import com.jiedan.service.ai.code.GitVersionControlService;
import com.jiedan.service.ai.feedback.FeedbackShadowService;
import com.jiedan.service.ai.prompt.AiPromptTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI服务
 * 封装AI相关的业务逻辑，集成Feedback Shadow验证
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final AiStrategyFactory strategyFactory;
    private final GitVersionControlService gitVersionControlService;
    private final FeedbackShadowService feedbackShadowService;
    private final AiRetryService aiRetryService;
    private final CodeValidator codeValidator;
    private final TaskDecisionService taskDecisionService;
    private final VersionCollector versionCollector;
    private final SessionManager sessionManager;
    private final RequirementRepository requirementRepository;

    // 文档版本计数器：projectId -> apiType -> version
    private final Map<String, Map<String, Integer>> documentVersionCounter = new ConcurrentHashMap<>();

    /**
     * AI明确需求
     * 在需求明确后，自动初始化Git仓库并保存需求文档
     * 【启用重试机制】携带Feedback Shadow反馈建议进行重试
     * 【文档保存】每次执行保存一版，最终放行后保存最终交付版到req目录
     */
    public ClarifyRequirementResponse clarifyRequirement(ClarifyRequirementRequest request) {
        log.info("开始AI明确需求（带重试）, projectId: {}, 需求描述长度: {}",
                request.getProjectId(),
                request.getRequirementDescription() != null ? request.getRequirementDescription().length() : 0);

        // 初始化版本计数器
        documentVersionCounter.putIfAbsent(request.getProjectId(), new ConcurrentHashMap<>());
        Map<String, Integer> apiVersions = documentVersionCounter.get(request.getProjectId());

        // 使用重试服务执行AI任务
        ClarifyRequirementResponse response = aiRetryService.executeWithRetry(
                (previousIssues) -> {
                    // 执行AI任务
                    ClarifyRequirementResponse result = executeClarifyRequirementOnce(request, previousIssues);

                    // 每次执行成功都保存一版文档（放到feedback目录）
                    if (result.getDocumentContent() != null) {
                        int version = apiVersions.getOrDefault("clarify-requirement", 0) + 1;
                        apiVersions.put("clarify-requirement", version);
                        try {
                            saveDocumentVersion(request.getProjectId(), "clarify-requirement",
                                    result.getDocumentContent(), version, false);
                        } catch (Exception e) {
                            log.error("保存需求文档版本失败, projectId: {}, version: {}", request.getProjectId(), version, e);
                        }
                    }

                    return result;
                },
                request.getProjectId(),
                "clarify-requirement"
        );

        // 最终放行后保存最终交付版到数据库和文件
        if (response.isSuccess() && response.getDocumentContent() != null) {
            try {
                // 保存到文件
                saveFinalDocument(request.getProjectId(), "clarify-requirement", response.getDocumentContent());
                // 保存到数据库
                saveRequirementDocToDatabase(request.getProjectId(), response.getDocumentContent());
                log.info("需求文档最终版已保存到数据库和文件, projectId: {}", request.getProjectId());
            } catch (Exception e) {
                log.error("保存需求文档最终版失败, projectId: {}", request.getProjectId(), e);
            }
        }

        return response;
    }

    /**
     * 单次执行AI明确需求
     * 【规范化】使用标准化的Prompt模板
     * @param previousIssues 之前验证的问题列表（重试时携带）
     */
    private ClarifyRequirementResponse executeClarifyRequirementOnce(ClarifyRequirementRequest request,
                                                                     List<String> previousIssues) {
        // 使用默认策略（传入null获取默认策略）
        AIProviderStrategy strategy = strategyFactory.getStrategy(null);

        // 【规范化】使用标准化的System Prompt
        StringBuilder systemPrompt = new StringBuilder(AiPromptTemplate.CLARIFY_REQUIREMENT_SYSTEM);

        // 【重试时】携带之前的反馈建议
        if (previousIssues != null && !previousIssues.isEmpty()) {
            systemPrompt.append("\n\n【重要】之前生成的文档存在以下问题，请务必修正：\n");
            for (int i = 0; i < previousIssues.size(); i++) {
                systemPrompt.append(i + 1).append(". ").append(previousIssues.get(i)).append("\n");
            }
            systemPrompt.append("\n请根据以上问题重新生成完整的需求文档，确保所有问题都已解决。");
        }

        // 【规范化】使用标准化的User Prompt
        String userPrompt = AiPromptTemplate.buildClarifyRequirementUserPrompt(request.getRequirementDescription());

        // 【修复】使用续传机制，避免文档被截断
        String fullContent = chatWithContinuation(strategy, systemPrompt.toString(), userPrompt, 8000, 5);

        // 【简化】直接返回文档内容
        return ClarifyRequirementResponse.builder()
                .documentContent(fullContent)
                .build();
    }

    /**
     * AI拆分任务
     * 【改造】改为并行执行策略，同时执行3个任务，选择最佳结果
     * 【解决循环依赖】直接实例化AiParallelExecutor，不通过Spring注入
     */
    public SplitTasksResponse splitTasks(SplitTasksRequest request) {
        log.info("开始AI拆分任务（并行执行+真正多轮会话）, projectId: {}", request.getProjectId());

        // 获取需求文档：从数据库查询
        String requirementDoc = getRequirementDocFromDatabase(request.getProjectId());
        if (requirementDoc == null) {
            log.error("无法获取需求文档, projectId: {}", request.getProjectId());
            return SplitTasksResponse.builder()
                    .success(false)
                    .errorMessage("无法获取需求文档，请先明确需求")
                    .build();
        }
        
        // 构建提示词
        String systemPrompt = AiPromptTemplate.SPLIT_TASKS_SYSTEM;
        String userPrompt = "需求文档：\n\n" + requirementDoc;

        // 【解决循环依赖】直接实例化并行执行器，传入回调函数
        // 【真正多轮会话】使用sessionManager和strategyFactory
        AiParallelExecutor parallelExecutor = new AiParallelExecutor(
                taskDecisionService,
                versionCollector,
                feedbackShadowService,
                aiRetryService,
                sessionManager,
                strategyFactory,
                // 回调函数：执行单次AI任务（真正多轮会话模式）
                // 参数：(messages, previousIssues) -> SplitTasksResponse
                (taskRequest, versionId, previousContent, previousIssues) -> {
                    // 解析versionId获取taskIndex和retryCount
                    String[] parts = versionId.split("-");
                    int taskIndex = Integer.parseInt(parts[0].replace("V", ""));
                    int retryCount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
                    
                    // 调用AiService的内部方法
                    return executeSplitTasksOnceInternal(taskRequest, systemPrompt, taskIndex, retryCount);
                }
        );

        // 使用并行执行器执行
        AiParallelExecutor.ParallelResult result = parallelExecutor.executeSplitTasksParallel(
                request, systemPrompt, userPrompt);

        // 关闭执行器
        parallelExecutor.shutdown();

        if (!result.success()) {
            log.error("并行执行AI拆分任务失败, projectId: {}", request.getProjectId());
            return SplitTasksResponse.builder()
                    .success(false)
                    .errorMessage("AI拆分任务执行失败")
                    .build();
        }

        // 保存最终文档
        if (result.content() != null) {
            try {
                // 保存到文件
                saveFinalDocument(request.getProjectId(), "split-tasks", result.content());
                // 保存到数据库
                saveTaskDocToDatabase(request.getProjectId(), result.content());
                log.info("任务文档最终版已保存到task目录和数据库, projectId: {}, 选中版本: {}, 理由: {}",
                        request.getProjectId(), result.selectedVersion(), result.decisionReason());
            } catch (Exception e) {
                log.error("保存任务文档最终版失败, projectId: {}", request.getProjectId(), e);
            }
        }

        return SplitTasksResponse.builder()
                .success(true)
                .documentContent(result.content())
                .selectedVersion(result.selectedVersion())
                .decisionReason(result.decisionReason())
                .improvements(result.improvements())
                .build();
    }

    /**
     * 【内部方法】供并行执行器调用，执行单次AI拆分任务
     * 【注意】此方法当前未被使用，由executeWithRetryAndSession替代
     * 保留此方法仅用于兼容性和备用
     * @param taskIndex 任务索引（1-3）
     * @param retryCount 重试次数（1-3）
     */
    @Deprecated
    public SplitTasksResponse executeSplitTasksOnceInternal(SplitTasksRequest request,
                                                            String systemPrompt,
                                                            int taskIndex,
                                                            int retryCount) {
        String versionId = "V" + taskIndex + "-" + retryCount;
        log.info("执行单次AI拆分任务内部方法（已废弃）, projectId: {}, versionId: {}",
                request.getProjectId(), versionId);

        // 使用默认策略
        AIProviderStrategy strategy = strategyFactory.getStrategy(null);

        // 构建用户提示词
        String userPrompt = "需求文档：\n\n" + request.getRequirementDoc();

        // 使用续传机制确保内容完整，不截断
        String fullContent = chatWithContinuation(strategy, systemPrompt, userPrompt, 16000, 5);

        // 保存版本到feedback目录
        try {
            saveDocumentVersionWithVersionId(request.getProjectId(), "split-tasks",
                    fullContent, versionId, false);
            log.info("任务版本 {} 已保存, projectId: {}, 内容长度: {}",
                    versionId, request.getProjectId(), fullContent.length());
        } catch (Exception e) {
            log.error("保存任务版本失败, projectId: {}, versionId: {}",
                    request.getProjectId(), versionId, e);
        }

        return SplitTasksResponse.builder()
                .documentContent(fullContent)
                .build();
    }

    /**
     * 【新增】使用版本ID保存文档版本
     * @param versionId 版本ID，如 V1-1, V1-2, V2-1
     */
    private void saveDocumentVersionWithVersionId(String projectId, String apiType,
                                                   String content, String versionId,
                                                   boolean isFinal) throws java.io.IOException {
        String projectPath = "projects/" + projectId;
        String feedbackDir = projectPath + "/feedback/" + apiType;

        // 确保目录存在
        java.nio.file.Path dirPath = java.nio.file.Paths.get(feedbackDir);
        if (!java.nio.file.Files.exists(dirPath)) {
            java.nio.file.Files.createDirectories(dirPath);
        }

        // 生成文件名: {versionId}-{timestamp}-{random}.md
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String random = java.util.UUID.randomUUID().toString().substring(0, 8);
        String fileName = versionId + "-" + timestamp + "-" + random + ".md";

        java.nio.file.Path filePath = dirPath.resolve(fileName);
        java.nio.file.Files.writeString(filePath, content);
        log.info("文档版本已保存: {}, versionId: {}, final: {}", filePath, versionId, isFinal);
    }

    /**
     * 单次执行AI拆分任务
     * 【优化】生成程序员可用的详细技术任务书，包含前后端具体开发任务
     * @param previousIssues 之前验证的问题列表（重试时携带）
     */
    private SplitTasksResponse executeSplitTasksOnce(SplitTasksRequest request, List<String> previousIssues) {
        // 使用默认策略（传入null获取默认策略）
        AIProviderStrategy strategy = strategyFactory.getStrategy(null);

        // 【规范化】使用标准化的System Prompt
        StringBuilder systemPrompt = new StringBuilder(AiPromptTemplate.SPLIT_TASKS_SYSTEM);

        // 【重试时】携带之前的反馈建议
        if (previousIssues != null && !previousIssues.isEmpty()) {
            systemPrompt.append("\n\n【重要】之前的任务拆分存在以下问题，请务必修正：\n");
            for (int i = 0; i < previousIssues.size(); i++) {
                systemPrompt.append(i + 1).append(". ").append(previousIssues.get(i)).append("\n");
            }
            systemPrompt.append("\n请根据以上问题重新生成完整的任务拆分，确保所有问题都已解决。");
        }

        // 【优化】明确指定项目类型，强调基于需求文档生成
        String userPrompt = "需求文档：\n\n" + request.getRequirementDoc();

        // 【修复】使用续传机制，避免文档被截断
        String fullContent = chatWithContinuation(strategy, systemPrompt.toString(), userPrompt, 16000, 5);

        // 【简化】直接返回文档内容
        return SplitTasksResponse.builder()
                .documentContent(fullContent)
                .build();
    }

    /**
     * 保存文档版本（每次执行保存一版）
     * 保存到 feedback/{apiType}/ 目录下
     */
    private void saveDocumentVersion(String projectId, String apiType, String content,
                                      int version, boolean isFinal) throws java.io.IOException {
        String projectPath = "projects/" + projectId;
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

        // 确定保存目录：feedback/{apiType}/
        java.nio.file.Path feedbackDir = java.nio.file.Paths.get(projectPath, "feedback", apiType);
        java.nio.file.Files.createDirectories(feedbackDir);

        // 文件名：V{version}-{timestamp}.md 或 FINAL-{timestamp}.md
        String fileName = isFinal
                ? String.format("FINAL-%s.md", timestamp)
                : String.format("V%d-%s.md", version, timestamp);

        java.nio.file.Path filePath = feedbackDir.resolve(fileName);
        java.nio.file.Files.writeString(filePath, content);
        log.info("文档版本已保存: {}, version: {}, final: {}", filePath, version, isFinal);
    }

    /**
     * 保存最终交付版文档
     * - clarify-requirement -> req/REQUIREMENT.md
     * - split-tasks -> task/TASKS.md
     * - generate-code -> code/ 目录下
     */
    private void saveFinalDocument(String projectId, String apiType, String content) throws java.io.IOException {
        String projectPath = "projects/" + projectId;

        // 【修改】generate-code类型已经在parseAndSaveCodeFiles中保存为代码文件，这里只保存到feedback目录
        if ("generate-code".equals(apiType)) {
            // 只保存到feedback目录作为备份
            saveDocumentVersion(projectId, apiType, content, 0, true);
            log.info("代码内容已保存到feedback目录作为备份, projectId: {}", projectId);
            return;
        }

        // 确定目标目录和文件名
        String targetDir;
        String fileName;

        switch (apiType) {
            case "clarify-requirement":
                targetDir = "req";
                fileName = "REQUIREMENT.md";
                break;
            case "split-tasks":
                targetDir = "task";
                fileName = "TASKS.md";
                break;
            case "functional-test":
                targetDir = "code/tests";
                fileName = "FUNCTIONAL_TEST.md";
                break;
            case "security-test":
                targetDir = "code/tests";
                fileName = "SECURITY_TEST.md";
                break;
            default:
                targetDir = "docs";
                fileName = apiType + ".md";
        }

        java.nio.file.Path finalDir = java.nio.file.Paths.get(projectPath, targetDir);
        java.nio.file.Files.createDirectories(finalDir);

        java.nio.file.Path filePath = finalDir.resolve(fileName);
        java.nio.file.Files.writeString(filePath, content);
        log.info("最终交付文档已保存: {}", filePath);

        // 同时保存一版到feedback目录作为备份
        saveDocumentVersion(projectId, apiType, content, 0, true);
    }

    /**
     * AI生成代码
     * 【启用重试机制】携带Feedback Shadow反馈建议进行重试
     * 【文档保存】每次执行保存一版，最终放行后保存最终交付版到code目录
     * 【优化】支持模块化生成，每个模块32k token，避免超长上下文
     */
    public GenerateCodeResponse generateCode(GenerateCodeRequest request) {
        String moduleInfo = request.getModuleName() != null ? 
                "模块[" + request.getModuleName() + "]" : "完整项目";
        log.info("开始AI生成代码（带重试）, projectId: {}, {}", request.getProjectId(), moduleInfo);

        // 获取需求文档：从数据库查询
        String requirementDoc = getRequirementDocFromDatabase(request.getProjectId());
        if (requirementDoc == null) {
            log.error("无法获取需求文档, projectId: {}", request.getProjectId());
            return GenerateCodeResponse.builder()
                    .success(false)
                    .errorMessage("无法获取需求文档！")
                    .build();
        } else
            request.setRequirementDoc(requirementDoc);

        // 获取任务文档：从数据库查询
        String taskDoc = getTaskDocFromDatabase(request.getProjectId());
        if (taskDoc == null) {
            log.error("无法获取任务书文档, projectId: {}", request.getProjectId());
            return GenerateCodeResponse.builder()
                    .success(false)
                    .errorMessage("无法获取任务书文档！")
                    .build();
        }
        request.setTaskDoc(taskDoc);

        // 初始化版本计数器
        documentVersionCounter.putIfAbsent(request.getProjectId(), new ConcurrentHashMap<>());
        Map<String, Integer> apiVersions = documentVersionCounter.get(request.getProjectId());

        // 构建API类型（区分不同模块）
        String apiType = request.getModuleName() != null ? 
                "generate-code-" + request.getModuleName() : "generate-code";

        // 使用重试服务执行AI任务
        GenerateCodeResponse response = aiRetryService.executeWithRetry(
                (previousIssues) -> {
                    // 执行AI任务
                    GenerateCodeResponse result = executeGenerateCodeOnce(request, previousIssues);

                    // 每次执行成功都保存一版代码（放到feedback目录）
                    if (result.getCode() != null) {
                        int version = apiVersions.getOrDefault(apiType, 0) + 1;
                        apiVersions.put(apiType, version);
                        try {
                            saveDocumentVersion(request.getProjectId(), apiType,
                                    result.getCode(), version, false);
                        } catch (Exception e) {
                            log.error("保存代码版本失败, projectId: {}, module: {}, version: {}", 
                                    request.getProjectId(), request.getModuleName(), version, e);
                        }
                    }

                    return result;
                },
                request.getProjectId(),
                apiType
        );

        // 保存代码文件到code目录
        if (response.isSuccess() && response.getCode() != null) {
            try {
                // 解析并保存代码文件
                Map<String, String> savedFiles = parseAndSaveCodeFilesWithResult(request.getProjectId(), response.getCode());
                
                // 【步骤3】代码验证
                if (codeValidator != null && !savedFiles.isEmpty()) {
                    CodeValidator.ValidationResult validationResult = codeValidator.validateProject(savedFiles);
                    log.info("代码验证完成, projectId: {}, 得分: {}, 错误: {}, 警告: {}",
                            request.getProjectId(), validationResult.getScore(),
                            validationResult.getErrors().size(), validationResult.getWarnings().size());
                    
                    // 将验证结果关联到响应
                    response.setValidationDecision(validationResult.isValid() ? "PASSED" : "FAILED");
                    
                    if (!validationResult.getErrors().isEmpty()) {
                        response.setErrorMessage(String.join("; ", validationResult.getErrors()));
                    }
                    
                    // 如果验证不通过，记录警告
                    if (!validationResult.isValid()) {
                        log.warn("代码验证发现错误, projectId: {}", request.getProjectId());
                        for (String error : validationResult.getErrors()) {
                            log.warn("  错误: {}", error);
                        }
                    }
                    for (String warning : validationResult.getWarnings()) {
                        log.warn("  警告: {}", warning);
                    }
                }
                
                // 如果是模块化生成，记录模块摘要
                if (request.getModuleName() != null) {
                    log.info("模块[{}]代码已保存到code目录, projectId: {}", 
                            request.getModuleName(), request.getProjectId());
                } else {
                    // 保存到feedback目录作为备份
                    saveFinalDocument(request.getProjectId(), "generate-code", response.getCode());
                    log.info("代码最终版已保存, projectId: {}", request.getProjectId());
                }
            } catch (Exception e) {
                log.error("保存代码失败, projectId: {}, module: {}", 
                        request.getProjectId(), request.getModuleName(), e);
            }
        }

        return response;
    }

    /**
     * 单次执行AI生成代码
     * 【重构】基于需求书和任务书生成完整项目代码
     * @param previousIssues 之前验证的问题列表（重试时携带）
     */
    private GenerateCodeResponse executeGenerateCodeOnce(GenerateCodeRequest request, List<String> previousIssues) {
        AIProviderStrategy strategy = strategyFactory.getStrategy(request.getModel());

        // 【规范化】使用标准化的System Prompt
        StringBuilder systemPrompt = new StringBuilder(AiPromptTemplate.GENERATE_CODE_SYSTEM);
        
        // 添加项目结构规范到System Prompt
        systemPrompt.append("\n\n").append(AiPromptTemplate.CODE_GENERATION_PROJECT_STRUCTURE);

        // 【重试时】携带之前的反馈建议
        if (previousIssues != null && !previousIssues.isEmpty()) {
            systemPrompt.append("\n\n【重要】之前生成的代码存在以下问题，请务必修正：\n");
            for (int i = 0; i < previousIssues.size(); i++) {
                systemPrompt.append(i + 1).append(". ").append(previousIssues.get(i)).append("\n");
            }
            systemPrompt.append("\n请根据以上问题重新生成完整的代码，确保所有问题都已解决。");
        }

        // 【重构】构建用户提示词，支持模块化生成
        StringBuilder userPrompt = new StringBuilder();
        
        // 1. 需求书（完整传递，不允许截断）
        if (request.getRequirementDoc() != null && !request.getRequirementDoc().isEmpty()) {
            userPrompt.append("【需求书 - 项目整体方向】\n");
            userPrompt.append(request.getRequirementDoc());
            userPrompt.append("\n\n");
        }
        
        // 2. 任务书（完整传递，不允许截断）
        if (request.getTaskDoc() != null && !request.getTaskDoc().isEmpty()) {
            userPrompt.append("【任务书 - 技术实现参考】\n");
            userPrompt.append(request.getTaskDoc());
            userPrompt.append("\n\n");
        }
        
        // 3. 【模块化生成】当前模块信息
        if (request.getModuleName() != null) {
            userPrompt.append("【当前模块】\n");
            userPrompt.append("模块名称：").append(request.getModuleName()).append("\n");
            userPrompt.append("模块顺序：").append(request.getModuleOrder()).append("\n");
            
            if (request.getFileList() != null && !request.getFileList().isEmpty()) {
                userPrompt.append("需要生成的文件：\n");
                for (String file : request.getFileList()) {
                    userPrompt.append("- ").append(file).append("\n");
                }
            }
            userPrompt.append("\n");
            
            // 4. 已生成模块的摘要（用于保持一致性）
            if (request.getGeneratedModulesSummary() != null && !request.getGeneratedModulesSummary().isEmpty()) {
                userPrompt.append("【已生成模块摘要】（供参考，确保接口一致性）\n");
                request.getGeneratedModulesSummary().forEach((module, summary) -> {
                    userPrompt.append(module).append("：").append(summary).append("\n");
                });
                userPrompt.append("\n");
            }
        }
        
        // 5. 特殊要求
        if (request.getRequirements() != null && !request.getRequirements().isEmpty()) {
            userPrompt.append("【特殊要求】\n");
            for (String req : request.getRequirements()) {
                userPrompt.append("- ").append(req).append("\n");
            }
            userPrompt.append("\n");
        }
        
        // 6. 开发指令
        userPrompt.append("【开发指令】\n");
        if (request.getModuleName() != null) {
            userPrompt.append("请生成当前模块的所有代码文件，确保与已生成模块的接口保持一致。\n");
            userPrompt.append("如果这是第一个模块，请优先定义好实体类和接口规范。\n");
        } else {
            userPrompt.append("请根据以上需求书和任务书，生成完整的项目代码。\n");
        }
        userPrompt.append("确保所有文件完整、可运行，严格遵循技术规格。\n");

        // 【步骤2】使用16k maxTokens（保守值），增加续传次数到5次
        // 原因：32k场景下上下文容易超限，16k更稳定
        // 预估：输入约3000token，输出约12000token，续传5次可生成6万token内容
        String fullContent = chatWithContinuation(strategy, systemPrompt.toString(), userPrompt.toString(), 16000, 5);

        // 构建响应（暂不设置success，由重试服务设置）
        return GenerateCodeResponse.builder()
                .code(fullContent)
                .explanation(request.getModuleName() != null ? 
                        "模块[" + request.getModuleName() + "]代码已生成" : 
                        "代码已生成，请查看项目code目录")
                .rawResponse(fullContent)
                .model(request.getModel())
                .build();
    }

    /**
     * 【新增】解析代码内容并保存为文件
     * 解析格式：===FILE:文件路径===\n```语言\n代码内容\n```
     * 支持各种文件类型：.java, .vue, .js, .ts, .css, .scss, .html, .xml, .yml, .yaml, .json, .properties, .sql等
     * 【优化】添加文件存在检查和合并逻辑，避免模块间文件覆盖
     */
    private void parseAndSaveCodeFiles(String projectId, String content) {
        if (content == null || content.isEmpty()) {
            log.warn("代码内容为空，无法解析文件");
            return;
        }

        String projectPath = "projects/" + projectId;
        java.nio.file.Path codeDir = java.nio.file.Paths.get(projectPath, "code");

        // 正则匹配文件路径和代码内容
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "===FILE:(.+?)===\\s*```(?:[\\w+-]+)?\\s*(.+?)```",
                java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher matcher = pattern.matcher(content);

        int fileCount = 0;
        int errorCount = 0;
        int skipCount = 0;
        int mergeCount = 0;
        
        while (matcher.find()) {
            String filePath = matcher.group(1).trim();
            String fileContent = matcher.group(2).trim();

            // 跳过空文件路径
            if (filePath.isEmpty()) {
                log.warn("发现空的文件路径，跳过");
                continue;
            }

            try {
                // 构建完整文件路径
                java.nio.file.Path fullFilePath = codeDir.resolve(filePath);
                
                // 安全检查：确保文件路径在code目录下（防止目录遍历攻击）
                if (!fullFilePath.normalize().startsWith(codeDir.normalize())) {
                    log.warn("非法文件路径，跳过: {}", filePath);
                    continue;
                }
                
                // 创建父目录
                java.nio.file.Files.createDirectories(fullFilePath.getParent());
                
                // 【优化】检查文件是否已存在
                if (java.nio.file.Files.exists(fullFilePath)) {
                    String existingContent = java.nio.file.Files.readString(fullFilePath);
                    
                    // 如果内容相同，跳过
                    if (existingContent.equals(fileContent)) {
                        log.debug("文件内容相同，跳过: {}", filePath);
                        skipCount++;
                        continue;
                    }
                    
                    // 【优化】如果是配置文件或SQL文件，尝试合并
                    if (shouldMergeFile(filePath)) {
                        String mergedContent = mergeFileContent(filePath, existingContent, fileContent);
                        java.nio.file.Files.writeString(fullFilePath, mergedContent, 
                                java.nio.charset.StandardCharsets.UTF_8);
                        log.info("文件已合并: {} ({} 字符)", filePath, mergedContent.length());
                        mergeCount++;
                    } else {
                        // 备份旧文件
                        java.nio.file.Path backupPath = java.nio.file.Paths.get(
                                fullFilePath.toString() + ".backup." + System.currentTimeMillis());
                        java.nio.file.Files.copy(fullFilePath, backupPath);
                        
                        // 覆盖为新内容
                        java.nio.file.Files.writeString(fullFilePath, fileContent, 
                                java.nio.charset.StandardCharsets.UTF_8);
                        log.warn("文件已覆盖（已备份）: {}", filePath);
                    }
                } else {
                    // 新文件，直接保存
                    java.nio.file.Files.writeString(fullFilePath, fileContent, 
                            java.nio.charset.StandardCharsets.UTF_8);
                    log.info("文件已保存: {} ({} 字符)", filePath, fileContent.length());
                }
                
                fileCount++;
            } catch (Exception e) {
                log.error("保存文件失败: {}", filePath, e);
                errorCount++;
            }
        }

        if (fileCount == 0 && errorCount == 0 && skipCount == 0) {
            log.warn("未找到任何文件标记 (===FILE:...===)，请检查AI返回格式");
        } else {
            log.info("文件解析完成: 新建 {} 个, 跳过 {} 个, 合并 {} 个, 失败 {} 个", 
                    fileCount, skipCount, mergeCount, errorCount);
        }
    }

    /**
     * 【新增】解析代码内容并保存为文件，返回保存的文件映射（用于验证）
     * 与parseAndSaveCodeFiles逻辑相同，但返回Map供后续验证使用
     */
    private Map<String, String> parseAndSaveCodeFilesWithResult(String projectId, String content) {
        Map<String, String> savedFiles = new ConcurrentHashMap<>();
        
        if (content == null || content.isEmpty()) {
            log.warn("代码内容为空，无法解析文件");
            return savedFiles;
        }

        String projectPath = "projects/" + projectId;
        java.nio.file.Path codeDir = java.nio.file.Paths.get(projectPath, "code");

        // 正则匹配文件路径和代码内容
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "===FILE:(.+?)===\\s*```(?:[\\w+-]+)?\\s*(.+?)```",
                java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String filePath = matcher.group(1).trim();
            String fileContent = matcher.group(2).trim();

            if (filePath.isEmpty()) {
                continue;
            }

            try {
                java.nio.file.Path fullFilePath = codeDir.resolve(filePath);
                
                // 安全检查
                if (!fullFilePath.normalize().startsWith(codeDir.normalize())) {
                    continue;
                }
                
                // 创建父目录
                java.nio.file.Files.createDirectories(fullFilePath.getParent());
                
                // 保存文件
                java.nio.file.Files.writeString(fullFilePath, fileContent, 
                        java.nio.charset.StandardCharsets.UTF_8);
                
                // 添加到结果映射
                savedFiles.put(filePath, fileContent);
                
            } catch (Exception e) {
                log.error("保存文件失败: {}", filePath, e);
            }
        }

        return savedFiles;
    }

    /**
     * 【新增】判断文件是否应该合并而非覆盖
     */
    private boolean shouldMergeFile(String filePath) {
        String lowerPath = filePath.toLowerCase();
        return lowerPath.endsWith(".sql") ||           // SQL文件合并
               lowerPath.endsWith(".md") ||           // Markdown文档合并
               lowerPath.contains("application") ||   // 配置文件合并
               lowerPath.contains("pom.xml") ||       // Maven配置合并
               lowerPath.contains("package.json");    // NPM配置合并
    }

    /**
     * 【新增】合并文件内容
     * 对于SQL文件，追加新语句；对于其他文件，智能合并
     */
    private String mergeFileContent(String filePath, String existing, String newContent) {
        String lowerPath = filePath.toLowerCase();
        
        if (lowerPath.endsWith(".sql")) {
            // SQL文件：追加新语句
            return existing + "\n\n-- 新增语句\n" + newContent;
        } else if (lowerPath.contains("application")) {
            // 配置文件：合并YAML/Properties
            return mergeConfigFile(existing, newContent);
        } else {
            // 其他文件：简单追加并标记
            return existing + "\n\n/* ===== 新增内容 ===== */\n" + newContent;
        }
    }

    /**
     * 【新增】合并配置文件（简单实现）
     */
    private String mergeConfigFile(String existing, String newContent) {
        // 简单合并：去重后追加
        java.util.Set<String> existingLines = new java.util.HashSet<>(
                java.util.Arrays.asList(existing.split("\n")));
        StringBuilder merged = new StringBuilder(existing);
        
        for (String line : newContent.split("\n")) {
            if (!existingLines.contains(line.trim()) && !line.trim().isEmpty()) {
                merged.append("\n").append(line);
            }
        }
        
        return merged.toString();
    }

    /**
     * AI功能测试
     * 生成测试文档并保存到项目目录
     * 【启用重试机制】携带Feedback Shadow反馈建议进行重试
     */
    public FunctionalTestResponse functionalTest(FunctionalTestRequest request) {
        log.info("开始AI功能测试（带重试）, projectId: {}", request.getProjectId());

        // 使用重试服务执行AI任务
        return aiRetryService.executeWithRetry(
                (previousIssues) -> executeFunctionalTestOnce(request, previousIssues),
                request.getProjectId(),
                "functional-test"
        );
    }

    /**
     * 单次执行AI功能测试
     * @param previousIssues 之前验证的问题列表（重试时携带）
     */
    private FunctionalTestResponse executeFunctionalTestOnce(FunctionalTestRequest request, List<String> previousIssues) {
        AIProviderStrategy strategy = strategyFactory.getStrategy(request.getModel());

        // 构建系统提示词
        StringBuilder systemPrompt = new StringBuilder("""
                你是一位专业的测试工程师，擅长编写全面的功能测试用例。
                请为提供的代码生成JUnit测试用例，包含：
                1. 正常场景测试
                2. 边界条件测试
                3. 异常场景测试
                4. 测试覆盖率分析
                """);

        // 【重试时】携带之前的反馈建议
        if (previousIssues != null && !previousIssues.isEmpty()) {
            systemPrompt.append("\n\n【重要】之前的测试用例存在以下问题，请务必修正：\n");
            for (int i = 0; i < previousIssues.size(); i++) {
                systemPrompt.append(i + 1).append(". ").append(previousIssues.get(i)).append("\n");
            }
            systemPrompt.append("\n请根据以上问题重新生成完整的测试用例，确保所有问题都已解决。");
        }

        // 构建用户提示词
        StringBuilder userPrompt = new StringBuilder("请为以下代码生成功能测试用例：\n");
        if (request.getLanguage() != null) {
            userPrompt.append("编程语言：").append(request.getLanguage()).append("\n");
        }
        if (request.getFunctionDescription() != null) {
            userPrompt.append("功能描述：").append(request.getFunctionDescription()).append("\n");
        }
        userPrompt.append("```\n").append(request.getCode()).append("\n```");

        // 【修复】使用续传机制，避免测试用例被截断
        String fullContent = chatWithContinuation(strategy, systemPrompt.toString(), userPrompt.toString(), 8000, 5);

        // 解析测试用例
        List<TestCase> testCases = parseTestCasesFromContent(fullContent);

        // 构建响应（暂不设置success，由重试服务设置）
        return FunctionalTestResponse.builder()
                .testCases(testCases)
                .testCode(fullContent)
                .rawResponse(fullContent)
                .model(request.getModel())
                .build();
    }

    /**
     * AI安全测试
     * 生成安全测试文档并保存到项目目录
     * 【启用重试机制】携带Feedback Shadow反馈建议进行重试
     */
    public SecurityTestResponse securityTest(SecurityTestRequest request) {
        log.info("开始AI安全测试（带重试）, projectId: {}", request.getProjectId());

        // 使用重试服务执行AI任务
        return aiRetryService.executeWithRetry(
                (previousIssues) -> executeSecurityTestOnce(request, previousIssues),
                request.getProjectId(),
                "security-test"
        );
    }

    /**
     * 单次执行AI安全测试
     * @param previousIssues 之前验证的问题列表（重试时携带）
     */
    private SecurityTestResponse executeSecurityTestOnce(SecurityTestRequest request, List<String> previousIssues) {
        AIProviderStrategy strategy = strategyFactory.getStrategy(request.getModel());

        // 构建系统提示词
        StringBuilder systemPrompt = new StringBuilder("""
                你是一位专业的安全工程师，擅长发现代码中的安全漏洞。
                请对提供的代码进行安全扫描，输出：
                1. 发现的漏洞列表（包含漏洞类型、严重程度、位置）
                2. 漏洞详细描述
                3. 修复建议
                4. 安全最佳实践建议
                """);

        // 【重试时】携带之前的反馈建议
        if (previousIssues != null && !previousIssues.isEmpty()) {
            systemPrompt.append("\n\n【重要】之前的安全测试报告存在以下问题，请务必修正：\n");
            for (int i = 0; i < previousIssues.size(); i++) {
                systemPrompt.append(i + 1).append(". ").append(previousIssues.get(i)).append("\n");
            }
            systemPrompt.append("\n请根据以上问题重新生成完整的安全测试报告，确保所有问题都已解决。");
        }

        // 构建用户提示词
        StringBuilder userPrompt = new StringBuilder("请对以下代码进行安全测试：\n");
        if (request.getLanguage() != null) {
            userPrompt.append("编程语言：").append(request.getLanguage()).append("\n");
        }
        if (request.getApplicationType() != null) {
            userPrompt.append("应用类型：").append(request.getApplicationType()).append("\n");
        }
        userPrompt.append("```\n").append(request.getCode()).append("\n```");

        // 【修复】使用续传机制，避免安全测试报告被截断
        String fullContent = chatWithContinuation(strategy, systemPrompt.toString(), userPrompt.toString(), 8000, 5);

        // 解析漏洞列表
        List<SecurityVulnerability> vulnerabilities = parseVulnerabilitiesFromContent(fullContent);

        // 统计风险等级
        int highRisk = 0, mediumRisk = 0, lowRisk = 0;
        for (SecurityVulnerability v : vulnerabilities) {
            switch (v.getSeverity() != null ? v.getSeverity().toLowerCase() : "") {
                case "高", "high" -> highRisk++;
                case "中", "medium" -> mediumRisk++;
                case "低", "low" -> lowRisk++;
            }
        }

        // 构建响应（暂不设置success，由重试服务设置）
        return SecurityTestResponse.builder()
                .vulnerabilities(vulnerabilities)
                .totalVulnerabilities(vulnerabilities.size())
                .highRiskCount(highRisk)
                .mediumRiskCount(mediumRisk)
                .lowRiskCount(lowRisk)
                .rawResponse(fullContent)
                .model(request.getModel())
                .build();
    }

    /**
     * 从内容中解析任务列表
     */
    private List<TaskItem> parseTasksFromContent(String content) {
        List<TaskItem> tasks = new ArrayList<>();
        if (content == null || content.trim().isEmpty()) {
            return tasks;
        }

        // 按行解析，寻找任务模式
        String[] lines = content.split("\\n");
        TaskItem currentTask = null;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // 匹配任务标题（支持多种格式）
            // 格式1: 1. 任务名称 / 格式2: - 任务名称 / 格式3: ### 任务名称
            if (line.matches("^\\d+[.．]\\s*.+") || line.matches("^[-•]\\s+.+") || line.matches("^#{1,3}\\s+.+") || line.matches("^任务\\s*\\d*[：:].*")) {
                if (currentTask != null) {
                    tasks.add(currentTask);
                }
                currentTask = new TaskItem();
                // 提取任务名称
                String taskName = line.replaceAll("^\\d+[.．]\\s*", "")
                        .replaceAll("^[-•]\\s+", "")
                        .replaceAll("^#{1,3}\\s+", "")
                        .replaceAll("^任务\\s*\\d*[：:]\\s*", "")
                        .trim();
                currentTask.setName(taskName);
                currentTask.setPriority("中");
                currentTask.setEstimatedHours(8);
            }
            // 匹配任务描述
            else if (line.matches("^描述[：:].*") || line.matches("^说明[：:].*") || line.toLowerCase().startsWith("description:")) {
                if (currentTask != null) {
                    String desc = line.replaceAll("^描述[：:]\\s*", "")
                            .replaceAll("^说明[：:]\\s*", "")
                            .replaceAll("^description:\\s*", "")
                            .trim();
                    currentTask.setDescription(desc);
                }
            }
            // 匹配预估工时
            else if (line.matches(".*工时.*") || line.matches(".*时间.*") || line.matches(".*小时.*") || line.toLowerCase().matches(".*hours?.*")) {
                if (currentTask != null) {
                    // 提取数字
                    java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\d+").matcher(line);
                    if (matcher.find()) {
                        try {
                            int hours = Integer.parseInt(matcher.group());
                            currentTask.setEstimatedHours(hours);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
            // 匹配优先级
            else if (line.matches(".*优先级.*") || line.matches(".*重要.*") || line.toLowerCase().matches(".*priority.*")) {
                if (currentTask != null) {
                    if (line.contains("高") || line.toLowerCase().contains("high")) {
                        currentTask.setPriority("高");
                    } else if (line.contains("低") || line.toLowerCase().contains("low")) {
                        currentTask.setPriority("低");
                    } else {
                        currentTask.setPriority("中");
                    }
                }
            }
        }

        // 添加最后一个任务
        if (currentTask != null) {
            tasks.add(currentTask);
        }

        // 如果没有解析到任务，创建一个默认任务
        if (tasks.isEmpty()) {
            TaskItem defaultTask = new TaskItem();
            defaultTask.setName("开发任务");
            defaultTask.setDescription(content.length() > 100 ? content.substring(0, 100) + "..." : content);
            defaultTask.setPriority("中");
            defaultTask.setEstimatedHours(8);
            tasks.add(defaultTask);
        }

        return tasks;
    }

    /**
     * 从内容中解析测试用例（简化实现）
     */
    private List<TestCase> parseTestCasesFromContent(String content) {
        // 简化实现
        List<TestCase> testCases = new ArrayList<>();
        return testCases;
    }

    /**
     * 从内容中解析漏洞列表（简化实现）
     */
    private List<SecurityVulnerability> parseVulnerabilitiesFromContent(String content) {
        // 简化实现
        List<SecurityVulnerability> vulnerabilities = new ArrayList<>();
        return vulnerabilities;
    }

    /**
     * 保存测试文档到项目目录
     */
    private void saveTestDocumentToProject(String projectId, String filename, String content) throws java.io.IOException {
        String projectPath = "projects/" + projectId;
        java.nio.file.Path docPath = java.nio.file.Paths.get(projectPath, "docs", "tests");
        java.nio.file.Files.createDirectories(docPath);

        java.nio.file.Path filePath = docPath.resolve(filename);
        java.nio.file.Files.writeString(filePath, content);
        log.info("测试文档已保存: {}", filePath);
    }

    /**
     * 【新增】支持续传的AI调用方法
     * 当内容被截断时，自动继续生成剩余内容
     * 【优化】添加上下文压缩机制，防止32k场景下上下文过长
     * @param strategy AI策略
     * @param systemPrompt 系统提示词
     * @param userPrompt 用户提示词
     * @param maxTokens 每次调用的最大token数
     * @param maxContinuation 最大续传次数（防止无限循环）
     * @return 完整的AI响应内容
     */
    private String chatWithContinuation(AIProviderStrategy strategy, String systemPrompt,
                                        String userPrompt, int maxTokens, int maxContinuation) {
        StringBuilder fullContent = new StringBuilder();
        List<AiMessage> messages = new ArrayList<>();
        messages.add(AiMessage.system(systemPrompt));
        messages.add(AiMessage.user(userPrompt));

        int continuationCount = 0;
        boolean isComplete = false;
        String lastContent = ""; // 记录上次生成的内容，用于去重

        while (!isComplete && continuationCount <= maxContinuation) {
            // 【优化】上下文压缩：如果消息太多，只保留系统提示、原始用户提示和最近的续传
            if (messages.size() > 10) {
                log.info("上下文消息过多({})，进行压缩", messages.size());
                List<AiMessage> compressedMessages = new ArrayList<>();
                compressedMessages.add(messages.get(0)); // 系统提示
                compressedMessages.add(messages.get(1)); // 原始用户提示
                // 添加最近的2轮对话（4条消息）
                int startIdx = Math.max(2, messages.size() - 4);
                for (int i = startIdx; i < messages.size(); i++) {
                    compressedMessages.add(messages.get(i));
                }
                messages = compressedMessages;
                log.info("上下文压缩完成，当前消息数: {}", messages.size());
            }

            // 构建请求
            AiChatRequest chatRequest = AiChatRequest.builder()
                    .temperature(0.7)
                    .maxTokens(maxTokens)
                    .messages(new ArrayList<>(messages))
                    .build();

            AiChatResponse chatResponse = strategy.chatCompletion(chatRequest);

            if (!Boolean.TRUE.equals(chatResponse.getSuccess())) {
                log.error("AI调用失败: {}", chatResponse.getErrorMessage());
                throw new RuntimeException("AI调用失败: " + chatResponse.getErrorMessage());
            }

            // 追加生成的内容
            String content = chatResponse.getContent();
            if (content != null && !content.isEmpty()) {
                // 【优化】去重检查：如果新内容与上次相同，可能是模型重复生成
                if (content.equals(lastContent)) {
                    log.warn("检测到重复内容，停止续传");
                    isComplete = true;
                    break;
                }
                fullContent.append(content);
                lastContent = content;
            }

            // 检查是否被截断
            String finishReason = chatResponse.getFinishReason();
            log.info("AI调用完成, finishReason: {}, 当前内容长度: {}", finishReason, fullContent.length());

            if ("length".equals(finishReason)) {
                // 内容被截断，需要续传
                continuationCount++;
                log.info("内容被截断，开始第{}次续传", continuationCount);

                // 添加已生成的内容作为assistant消息
                messages.add(AiMessage.assistant(content));
                // 添加续传指令
                messages.add(AiMessage.user("请继续生成剩余内容，从上次中断的地方开始，不要重复已生成的内容。当前已生成" + fullContent.length() + "字符。"));
            } else {
                // 内容完整，结束循环
                isComplete = true;
            }
        }

        if (continuationCount > maxContinuation) {
            log.warn("达到最大续传次数{}，内容可能不完整", maxContinuation);
        }

        return fullContent.toString();
    }

    /**
     * 保存需求文档到数据库
     */
    @Transactional
    public void saveRequirementDocToDatabase(String projectId, String content) {
        try {
            Long requirementId = Long.parseLong(projectId);
            Requirement requirement = requirementRepository.findById(requirementId).orElse(null);
            if (requirement != null) {
                requirement.setAiRequirementDoc(content);
                requirementRepository.save(requirement);
                log.info("需求文档已保存到数据库, requirementId: {}, content长度: {}", 
                        requirementId, content.length());
            } else {
                log.warn("未找到Requirement记录, requirementId: {}", requirementId);
            }
        } catch (Exception e) {
            log.error("保存需求文档到数据库失败, projectId: {}", projectId, e);
        }
    }

    /**
     * 保存任务文档到数据库
     */
    @Transactional
    public void saveTaskDocToDatabase(String projectId, String content) {
        try {
            Long requirementId = Long.parseLong(projectId);
            Requirement requirement = requirementRepository.findById(requirementId).orElse(null);
            if (requirement != null) {
                requirement.setAiTaskDoc(content);
                requirementRepository.save(requirement);
                log.info("任务文档已保存到数据库, requirementId: {}, content长度: {}", 
                        requirementId, content.length());
            } else {
                log.warn("未找到Requirement记录, requirementId: {}", requirementId);
            }
        } catch (Exception e) {
            log.error("保存任务文档到数据库失败, projectId: {}", projectId, e);
        }
    }

    /**
     * 从数据库获取任务文档
     */
    public String getTaskDocFromDatabase(String projectId) {
        try {
            Long requirementId = Long.parseLong(projectId);
            Requirement requirement = requirementRepository.findById(requirementId).orElse(null);
            if (requirement != null && requirement.getAiTaskDoc() != null) {
                log.info("从数据库获取任务文档, requirementId: {}, content长度: {}", 
                        requirementId, requirement.getAiTaskDoc().length());
                return requirement.getAiTaskDoc();
            } else {
                log.warn("数据库中未找到任务文档, requirementId: {}", requirementId);
            }
        } catch (Exception e) {
            log.error("从数据库获取任务文档失败, projectId: {}", projectId, e);
        }
        return null;
    }

    /**
     * 从数据库获取需求文档
     */
    public String getRequirementDocFromDatabase(String projectId) {
        try {
            Long requirementId = Long.parseLong(projectId);
            Requirement requirement = requirementRepository.findById(requirementId).orElse(null);
            if (requirement != null && requirement.getAiRequirementDoc() != null) {
                log.info("从数据库获取需求文档, requirementId: {}, content长度: {}", 
                        requirementId, requirement.getAiRequirementDoc().length());
                return requirement.getAiRequirementDoc();
            } else {
                log.warn("数据库中未找到需求文档, requirementId: {}", requirementId);
            }
        } catch (Exception e) {
            log.error("从数据库获取需求文档失败, projectId: {}", projectId, e);
        }
        return null;
    }
}
