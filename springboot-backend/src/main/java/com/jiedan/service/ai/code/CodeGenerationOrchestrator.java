package com.jiedan.service.ai.code;

import com.jiedan.dto.ai.AiChatRequest;
import com.jiedan.dto.ai.AiChatResponse;
import com.jiedan.dto.ai.code.*;
import com.jiedan.entity.ProjectStatus;
import com.jiedan.repository.ProjectStatusRepository;
import com.jiedan.service.ai.AIProviderStrategy;
import com.jiedan.service.ai.AiStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 代码生成编排器（AI自我验证版）
 * 流程：AI生成代码 → AI自我验证 → 保存 → Git提交
 * 特点：无本地编译检查，完全依赖AI自我约束
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeGenerationOrchestrator {

    private final CodeQualityChecker codeQualityChecker;
    private final AiStrategyFactory strategyFactory;
    private final ProjectStatusRepository projectStatusRepository;
    private final GitVersionControlService gitService;

    // 最大重试次数（静态验证）
    private static final int MAX_RETRY_COUNT = 5;
    // Token限制
    private static final int MAX_OUTPUT_TOKENS = 8000;

    /**
     * 直接生成完整项目代码（AI自我验证版）
     * AI在生成时进行自我验证，确保代码质量
     */
    public GenerateCodeResponse generateCompleteProject(String projectId, String projectType,
                                                         String prdDocument, String taskDocument) {
        log.info("开始生成完整项目代码(AI自我验证版), projectId: {}, projectType: {}", projectId, projectType);

        try {
            // 1. 更新项目状态
            updateProjectState(projectId, "GENERATING", "开始生成完整项目代码");

            int retryCount = 0;
            List<String> previousIssues = new ArrayList<>();

            while (retryCount < MAX_RETRY_COUNT) {
                // 2. 构建Prompt（包含自我验证指令）
                String prompt = buildSelfValidatingPrompt(projectType, prdDocument, taskDocument, previousIssues);

                // 3. 调用AI生成代码（AI在生成时已进行自我验证）
                GenerateCodeResponse response = generateCodeWithAI(prompt);

                if (!response.isSuccess()) {
                    log.error("代码生成失败: {}", response.getErrorMessage());
                    retryCount++;
                    continue;
                }

                // 4. AI自我验证（静态检查）
                SelfValidationResult validationResult = performAISelfValidation(
                        projectType, response.getFiles(), prdDocument);

                if (!validationResult.isPassed()) {
                    log.warn("AI自我验证发现问题: {}", validationResult.getIssues());
                    previousIssues = validationResult.getIssues();
                    retryCount++;
                    
                    if (retryCount >= MAX_RETRY_COUNT) {
                        log.info("达到最大重试次数({})，直接放行", MAX_RETRY_COUNT);
                        // 5次后放行，记录警告
                        saveProjectWithWarning(projectId, response.getFiles(), 
                                "AI自我验证未通过，但已达到最大重试次数，直接放行");
                        updateProjectState(projectId, "GENERATION_COMPLETED_WITH_WARNINGS", 
                                "代码生成完成（存在警告）");
                        return response;
                    }
                    continue;
                }

                // 5. 保存代码到项目目录
                String projectPath = "projects/" + projectId;
                saveGeneratedCode(projectPath, response.getFiles());

                // 6. Git提交
                gitService.commitChanges(projectId, "Initial code generation - attempt " + (retryCount + 1));

                // 7. 更新项目状态
                updateProjectState(projectId, "GENERATION_COMPLETED", "代码生成完成");

                log.info("完整项目代码生成完成, projectId: {}, 文件数: {}, 尝试次数: {}",
                        projectId, response.getFiles().size(), retryCount + 1);

                return response;
            }

            // 理论上不会执行到这里（因为达到MAX_RETRY_COUNT时会放行）
            updateProjectState(projectId, "GENERATION_FAILED", "代码生成失败");
            return GenerateCodeResponse.builder()
                    .success(false)
                    .errorMessage("代码生成失败")
                    .build();

        } catch (Exception e) {
            log.error("代码生成异常", e);
            updateProjectState(projectId, "GENERATION_FAILED", "异常: " + e.getMessage());
            return GenerateCodeResponse.builder()
                    .success(false)
                    .errorMessage("代码生成异常: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 构建自我验证Prompt
     * 强制AI在生成时进行自我验证
     */
    private String buildSelfValidatingPrompt(String projectType, String prdDocument,
                                              String taskDocument, List<String> previousIssues) {
        StringBuilder prompt = new StringBuilder();

        // 1. 【最高指令】自我验证要求
        prompt.append("【最高指令 - 强制性约束】\n");
        prompt.append("你是一位资深全栈开发工程师，必须严格遵守以下约束，否则代码将被拒绝：\n\n");
        
        prompt.append("1. **语法正确性**：所有代码必须语法正确，不能有任何编译错误\n");
        prompt.append("2. **完整性**：必须生成完整的项目，包含所有配置文件和依赖\n");
        prompt.append("3. **一致性**：代码风格统一，命名规范一致\n");
        prompt.append("4. **可运行性**：生成的代码必须可以直接运行，无需额外修改\n");
        prompt.append("5. **自我验证**：生成完成后，你必须自我检查一遍，确保没有错误\n\n");

        // 2. 项目类型
        prompt.append("【项目类型】\n");
        prompt.append(projectType).append("\n\n");

        // 3. PRD文档
        prompt.append("【产品需求文档】\n");
        prompt.append(truncateContent(prdDocument, 3000)).append("\n\n");

        // 4. 任务文档
        prompt.append("【任务文档】\n");
        prompt.append(truncateContent(taskDocument, 2000)).append("\n\n");

        // 5. 之前的问题（如果有）
        if (!previousIssues.isEmpty()) {
            prompt.append("【之前发现的问题，必须修复】\n");
            for (int i = 0; i < previousIssues.size() && i < 10; i++) {
                prompt.append(i + 1).append(". ").append(previousIssues.get(i)).append("\n");
            }
            prompt.append("\n");
        }

        // 6. 生成要求
        prompt.append("【生成要求】\n");
        prompt.append("1. 生成完整的项目代码，包含所有必要的配置文件\n");
        prompt.append("2. 代码必须语法正确，符合").append(projectType).append("规范\n");
        prompt.append("3. 实现PRD中的所有功能需求\n");
        prompt.append("4. 添加必要的注释说明关键逻辑\n");
        prompt.append("5. 生成的文件数量适中，优先保证代码质量\n\n");

        // 7. 输出格式
        prompt.append("【输出格式】\n");
        prompt.append("请按以下格式输出完整的项目文件：\n\n");
        prompt.append("```\n");
        prompt.append("## 文件列表\n\n");
        prompt.append("### {文件路径}\n");
        prompt.append("```{语言}\n");
        prompt.append("{代码内容}\n");
        prompt.append("```\n");
        prompt.append("**说明**: {文件说明}\n");
        prompt.append("```\n\n");

        prompt.append("【重要】生成完成后，请自我验证：\n");
        prompt.append("- 检查所有语法是否正确\n");
        prompt.append("- 检查所有依赖是否完整\n");
        prompt.append("- 检查是否符合").append(projectType).append("规范\n");
        prompt.append("- 确保代码可以直接运行\n\n");

        prompt.append("请生成完整的、高质量的、可直接运行的项目代码。");

        return prompt.toString();
    }

    /**
     * AI自我验证（静态检查）
     * 让AI检查自己生成的代码
     */
    private SelfValidationResult performAISelfValidation(String projectType, 
                                                          List<GeneratedFile> files,
                                                          String prdDocument) {
        try {
            // 构建验证Prompt
            StringBuilder validationPrompt = new StringBuilder();
            validationPrompt.append("你是一位严格的代码审查员，请对以下代码进行静态检查。\n\n");
            validationPrompt.append("【检查标准】\n");
            validationPrompt.append("1. 语法正确性：代码是否符合").append(projectType).append("语法规范\n");
            validationPrompt.append("2. 完整性：是否包含所有必要的文件和配置\n");
            validationPrompt.append("3. 一致性：命名规范、代码风格是否统一\n");
            validationPrompt.append("4. 需求匹配：是否实现了PRD中的所有功能\n\n");
            
            validationPrompt.append("【PRD摘要】\n");
            validationPrompt.append(truncateContent(prdDocument, 1000)).append("\n\n");
            
            validationPrompt.append("【待检查代码】\n");
            for (GeneratedFile file : files) {
                validationPrompt.append("文件: ").append(file.getPath()).append("\n");
                validationPrompt.append("```\n");
                validationPrompt.append(truncateContent(file.getContent(), 500)).append("\n");
                validationPrompt.append("```\n\n");
            }
            
            validationPrompt.append("【输出格式】\n");
            validationPrompt.append("检查结果: PASSED / FAILED\n");
            validationPrompt.append("问题列表（如有）:\n");
            validationPrompt.append("1. {问题描述}\n");
            validationPrompt.append("2. {问题描述}\n");

            AIProviderStrategy strategy = strategyFactory.getStrategy(null);
            AiChatRequest chatRequest = AiChatRequest.builder()
                    .model(null)
                    .temperature(0.1)
                    .maxTokens(2000)
                    .build()
                    .addSystemMessage(validationPrompt.toString());

            AiChatResponse response = strategy.chatCompletion(chatRequest);
            
            if (!Boolean.TRUE.equals(response.getSuccess())) {
                log.warn("AI自我验证调用失败: {}", response.getErrorMessage());
                return SelfValidationResult.failed(List.of("验证调用失败"));
            }

            // 解析验证结果
            String content = response.getContent();
            boolean passed = content.contains("PASSED") || content.contains("检查通过");
            
            List<String> issues = new ArrayList<>();
            if (!passed) {
                // 提取问题列表
                String[] lines = content.split("\n");
                for (String line : lines) {
                    if (line.matches("^\\d+\\..*") || line.contains("问题") || line.contains("错误")) {
                        issues.add(line.trim());
                    }
                }
            }

            return passed ? SelfValidationResult.passed() : SelfValidationResult.failed(issues);

        } catch (Exception e) {
            log.error("AI自我验证异常", e);
            return SelfValidationResult.failed(List.of("验证异常: " + e.getMessage()));
        }
    }

    /**
     * 调用AI生成代码
     */
    private GenerateCodeResponse generateCodeWithAI(String prompt) {
        try {
            AIProviderStrategy strategy = strategyFactory.getStrategy(null);

            AiChatRequest chatRequest = AiChatRequest.builder()
                    .model(null)
                    .temperature(0.2)
                    .maxTokens(MAX_OUTPUT_TOKENS)
                    .build()
                    .addSystemMessage("你是一位资深全栈开发工程师。请根据需求生成完整的、可运行的项目代码。")
                    .addUserMessage(prompt);

            long startTime = System.currentTimeMillis();
            AiChatResponse chatResponse = strategy.chatCompletion(chatRequest);
            long responseTime = System.currentTimeMillis() - startTime;

            if (!Boolean.TRUE.equals(chatResponse.getSuccess())) {
                return GenerateCodeResponse.builder()
                        .success(false)
                        .errorMessage("AI调用失败: " + chatResponse.getErrorMessage())
                        .build();
            }

            // 解析生成的文件
            List<GeneratedFile> files = parseGeneratedFiles(chatResponse.getContent());

            return GenerateCodeResponse.builder()
                    .success(true)
                    .files(files)
                    .usage(chatResponse.getUsage())
                    .responseTimeMs(responseTime)
                    .build();

        } catch (Exception e) {
            log.error("AI代码生成异常", e);
            return GenerateCodeResponse.builder()
                    .success(false)
                    .errorMessage("AI代码生成异常: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 解析生成的文件
     */
    private List<GeneratedFile> parseGeneratedFiles(String content) {
        List<GeneratedFile> files = new ArrayList<>();

        // 按文件分割
        String[] sections = content.split("### ");

        for (int i = 1; i < sections.length; i++) {
            String section = sections[i];

            // 提取文件路径
            int pathEnd = section.indexOf("\n");
            if (pathEnd == -1) {
                log.warn("无效的文件格式，未找到换行符，跳过此section");
                continue;
            }

            String filePath = section.substring(0, pathEnd).trim();

            // 提取代码内容
            String codeContent = extractCodeContent(section);

            // 提取文件说明
            String description = extractDescription(section);

            // 判断语言
            String language = detectLanguage(filePath);

            GeneratedFile file = GeneratedFile.builder()
                    .path(filePath)
                    .content(codeContent)
                    .language(language)
                    .description(description)
                    .build();

            files.add(file);
        }

        return files;
    }

    private String extractCodeContent(String section) {
        int start = section.indexOf("```");
        if (start == -1) return "";

        start = section.indexOf("\n", start);
        if (start == -1) return "";

        int end = section.indexOf("```", start + 1);
        if (end == -1) return "";

        return section.substring(start + 1, end).trim();
    }

    private String extractDescription(String section) {
        int start = section.indexOf("**说明**");
        if (start == -1) return "";

        start = section.indexOf(":", start);
        if (start == -1) return "";

        String desc = section.substring(start + 1).trim();
        int end = desc.indexOf("\n");
        if (end != -1) {
            desc = desc.substring(0, end);
        }

        return desc;
    }

    private String detectLanguage(String filePath) {
        if (filePath.endsWith(".java")) return "java";
        if (filePath.endsWith(".xml")) return "xml";
        if (filePath.endsWith(".yml") || filePath.endsWith(".yaml")) return "yaml";
        if (filePath.endsWith(".properties")) return "properties";
        if (filePath.endsWith(".js")) return "javascript";
        if (filePath.endsWith(".ts")) return "typescript";
        if (filePath.endsWith(".json")) return "json";
        if (filePath.endsWith(".wxml")) return "wxml";
        if (filePath.endsWith(".wxss")) return "css";
        if (filePath.endsWith(".css")) return "css";
        if (filePath.endsWith(".scss") || filePath.endsWith(".less")) return "css";
        if (filePath.endsWith(".py")) return "python";
        if (filePath.endsWith(".md")) return "markdown";
        if (filePath.endsWith(".html")) return "html";
        if (filePath.endsWith(".vue")) return "vue";
        return "text";
    }

    private void saveGeneratedCode(String projectPath, List<GeneratedFile> files) throws IOException {
        for (GeneratedFile file : files) {
            Path filePath = Paths.get(projectPath, file.getPath());
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, file.getContent());
            log.info("保存代码文件: {}", filePath);
        }
    }

    private void saveProjectWithWarning(String projectId, List<GeneratedFile> files, String warning) throws IOException {
        String projectPath = "projects/" + projectId;
        saveGeneratedCode(projectPath, files);
        
        // 保存警告信息
        Path warningPath = Paths.get(projectPath, "GENERATION_WARNING.txt");
        Files.writeString(warningPath, warning);
        
        log.warn("项目保存但存在警告: {}", warning);
    }

    private String truncateContent(String content, int maxLength) {
        if (content == null || content.length() <= maxLength) {
            return content;
        }

        int headLength = maxLength / 2;
        int tailLength = maxLength / 2 - 10;

        return content.substring(0, headLength) +
                "\n\n... [内容已截断] ...\n\n" +
                content.substring(content.length() - tailLength);
    }

    /**
     * 执行完整的AI开发流程（AI自我验证版）
     */
    public void executeFullDevelopmentFlow(String projectId, String projectType,
                                            String prdDocument, String taskDocument) {
        log.info("开始执行完整开发流程(AI自我验证版), projectId: {}", projectId);

        try {
            // 阶段1: 直接生成完整项目代码
            log.info("阶段1: 生成完整项目代码（AI自我验证）");
            GenerateCodeResponse codeResponse = generateCompleteProject(
                    projectId, projectType, prdDocument, taskDocument);

            if (!codeResponse.isSuccess()) {
                log.error("代码生成失败: {}", codeResponse.getErrorMessage());
                updateProjectState(projectId, "DEVELOPMENT_FAILED", "代码生成失败");
                return;
            }

            // 阶段2: 质量检查（静态检查）
            log.info("阶段2: 质量门禁检查（静态）");
            CodeQualityChecker.QualityCheckResult qualityResult =
                    codeQualityChecker.performQualityCheck(
                            "projects/" + projectId, projectType, codeResponse.getFiles());

            if (!qualityResult.isPassed()) {
                log.warn("质量检查发现问题: {}", qualityResult.getOverallMessage());
                updateProjectState(projectId, "DEVELOPMENT_COMPLETED_WITH_WARNINGS", 
                        "代码生成完成（存在质量警告）");
            } else {
                updateProjectState(projectId, "DEVELOPMENT_COMPLETED", "代码生成完成");
            }

            log.info("完整开发流程执行完成, projectId: {}", projectId);

        } catch (Exception e) {
            log.error("完整开发流程异常", e);
            updateProjectState(projectId, "DEVELOPMENT_FAILED", "流程异常: " + e.getMessage());
        }
    }

    private void updateProjectState(String projectId, String state, String message) {
        ProjectStatus project = projectStatusRepository.findByProjectId(projectId)
                .orElseGet(() -> ProjectStatus.builder()
                        .projectId(projectId)
                        .build());

        project.setState(state);
        project.setCurrentPhase(message);
        projectStatusRepository.save(project);

        log.info("项目状态更新, projectId: {}, state: {}, message: {}", projectId, state, message);
    }

    /**
     * 执行质量门禁检查
     */
    public CodeQualityChecker.QualityCheckResult performQualityGateCheck(String projectId, String projectType, List<GeneratedFile> files) {
        log.info("执行质量门禁检查, projectId: {}", projectId);
        return codeQualityChecker.performQualityCheck("projects/" + projectId, projectType, files);
    }

    /**
     * 修复代码
     */
    public GenerateCodeResponse repairCode(String projectId, String projectType, String prdDocument, String taskDocument, String errorReport) {
        log.info("开始修复代码, projectId: {}", projectId);
        
        try {
            // 构建修复Prompt
            StringBuilder repairPrompt = new StringBuilder();
            repairPrompt.append("【代码修复任务】\n\n");
            repairPrompt.append("请根据以下错误报告修复代码问题。\n\n");
            
            repairPrompt.append("【错误报告】\n");
            repairPrompt.append(errorReport).append("\n\n");
            
            repairPrompt.append("【项目类型】\n");
            repairPrompt.append(projectType).append("\n\n");
            
            repairPrompt.append("【PRD文档】\n");
            repairPrompt.append(truncateContent(prdDocument, 2000)).append("\n\n");
            
            repairPrompt.append("【任务文档】\n");
            repairPrompt.append(truncateContent(taskDocument, 1500)).append("\n\n");
            
            repairPrompt.append("请生成修复后的完整项目代码，确保所有问题都已解决。");
            
            // 调用AI生成修复后的代码
            GenerateCodeResponse response = generateCodeWithAI(repairPrompt.toString());
            
            if (response.isSuccess()) {
                // 保存修复后的代码
                String projectPath = "projects/" + projectId;
                saveGeneratedCode(projectPath, response.getFiles());
                
                // Git提交
                gitService.commitChanges(projectId, "Code repair based on error report");
                
                updateProjectState(projectId, "REPAIR_COMPLETED", "代码修复完成");
                log.info("代码修复完成, projectId: {}", projectId);
            } else {
                updateProjectState(projectId, "REPAIR_FAILED", "代码修复失败: " + response.getErrorMessage());
                log.error("代码修复失败, projectId: {}, error: {}", projectId, response.getErrorMessage());
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("代码修复异常, projectId: {}", projectId, e);
            updateProjectState(projectId, "REPAIR_FAILED", "修复异常: " + e.getMessage());
            return GenerateCodeResponse.builder()
                    .success(false)
                    .errorMessage("代码修复异常: " + e.getMessage())
                    .build();
        }
    }

    // ========== 内部类 ==========

    @lombok.Data
    @lombok.Builder
    public static class SelfValidationResult {
        private boolean passed;
        private List<String> issues;

        public static SelfValidationResult passed() {
            return SelfValidationResult.builder()
                    .passed(true)
                    .issues(new ArrayList<>())
                    .build();
        }

        public static SelfValidationResult failed(List<String> issues) {
            return SelfValidationResult.builder()
                    .passed(false)
                    .issues(issues)
                    .build();
        }
    }
}
