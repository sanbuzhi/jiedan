package com.jiedan.service.ai;

import com.jiedan.dto.ai.GenerateCodeRequest;
import com.jiedan.dto.ai.GenerateCodeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 【步骤5】模块化代码生成服务
 * 按模块顺序逐个生成代码，维护模块间一致性
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModularCodeGenerator {

    private final AiService aiService;
    private final TaskDocumentParser taskDocumentParser;

    /**
     * 模块生成结果
     */
    public static class ModuleGenerationResult {
        private String moduleName;
        private boolean success;
        private String errorMessage;
        private List<String> generatedFiles;
        private String codeSummary;  // 用于传递给后续模块

        public ModuleGenerationResult(String moduleName) {
            this.moduleName = moduleName;
            this.success = true;
            this.generatedFiles = new ArrayList<>();
        }

        // Getters and setters
        public String getModuleName() { return moduleName; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public List<String> getGeneratedFiles() { return generatedFiles; }
        public void setGeneratedFiles(List<String> generatedFiles) { this.generatedFiles = generatedFiles; }
        public String getCodeSummary() { return codeSummary; }
        public void setCodeSummary(String codeSummary) { this.codeSummary = codeSummary; }
    }

    /**
     * 完整项目生成结果
     */
    public static class ProjectGenerationResult {
        private boolean success;
        private String projectId;
        private List<ModuleGenerationResult> moduleResults;
        private String errorMessage;

        public ProjectGenerationResult(String projectId) {
            this.projectId = projectId;
            this.moduleResults = new ArrayList<>();
            this.success = true;
        }

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getProjectId() { return projectId; }
        public List<ModuleGenerationResult> getModuleResults() { return moduleResults; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    /**
     * 生成完整项目（按模块顺序）
     * @param projectId 项目ID
     * @param requirementDoc 需求书
     * @param taskDoc 任务书
     * @return 项目生成结果
     */
    public ProjectGenerationResult generateProject(String projectId, String requirementDoc, String taskDoc) {
        log.info("开始模块化生成项目, projectId: {}", projectId);

        ProjectGenerationResult projectResult = new ProjectGenerationResult(projectId);

        // 1. 解析任务书，获取模块列表（传入projectId以保存章节文件）
        TaskDocumentParser.ParseResult parseResult = taskDocumentParser.parse(taskDoc, projectId);
        if (!parseResult.isSuccess()) {
            projectResult.setSuccess(false);
            projectResult.setErrorMessage("任务书解析失败: " + parseResult.getErrorMessage());
            return projectResult;
        }

        List<TaskDocumentParser.ModuleInfo> modules = parseResult.getModules();
        if (modules.isEmpty()) {
            projectResult.setSuccess(false);
            projectResult.setErrorMessage("未从任务书中提取到任何模块");
            return projectResult;
        }

        log.info("任务书解析完成，共 {} 个模块", modules.size());

        // 2. 按顺序生成每个模块
        Map<String, String> generatedModulesSummary = new HashMap<>();

        for (int i = 0; i < modules.size(); i++) {
            TaskDocumentParser.ModuleInfo moduleInfo = modules.get(i);
            boolean isLastModule = (i == modules.size() - 1);

            log.info("开始生成模块 {}/{}: {}", i + 1, modules.size(), moduleInfo.getName());

            ModuleGenerationResult moduleResult = generateModule(
                    projectId,
                    requirementDoc,
                    taskDoc,
                    moduleInfo,
                    generatedModulesSummary,
                    isLastModule
            );

            projectResult.getModuleResults().add(moduleResult);

            if (!moduleResult.isSuccess()) {
                log.error("模块生成失败: {}, 错误: {}", moduleInfo.getName(), moduleResult.getErrorMessage());
                // 继续生成其他模块，但记录失败
            } else {
                // 更新模块摘要，传递给后续模块
                generatedModulesSummary.put(moduleInfo.getName(), moduleResult.getCodeSummary());
                log.info("模块生成成功: {}", moduleInfo.getName());
            }
        }

        // 3. 统计结果
        long successCount = projectResult.getModuleResults().stream()
                .filter(ModuleGenerationResult::isSuccess)
                .count();

        log.info("项目生成完成, projectId: {}, 成功: {}/{} 个模块",
                projectId, successCount, modules.size());

        if (successCount < modules.size()) {
            projectResult.setSuccess(false);
            projectResult.setErrorMessage("部分模块生成失败，成功: " + successCount + "/" + modules.size());
        }

        return projectResult;
    }

    /**
     * 生成单个模块
     */
    private ModuleGenerationResult generateModule(
            String projectId,
            String requirementDoc,
            String taskDoc,
            TaskDocumentParser.ModuleInfo moduleInfo,
            Map<String, String> generatedModulesSummary,
            boolean isLastModule) {

        ModuleGenerationResult result = new ModuleGenerationResult(moduleInfo.getName());

        try {
            // 构建生成请求
            GenerateCodeRequest request = new GenerateCodeRequest();
            request.setProjectId(projectId);
            request.setRequirementDoc(requirementDoc);
            request.setTaskDoc(taskDoc);
            request.setModuleName(moduleInfo.getName());
            request.setFileList(moduleInfo.getFiles());
            request.setModuleOrder(moduleInfo.getOrder());
            request.setIsLastModule(isLastModule);
            request.setGeneratedModulesSummary(generatedModulesSummary);

            // 调用AI生成代码
            GenerateCodeResponse response = aiService.generateCode(request);

            if (!response.isSuccess()) {
                result.setSuccess(false);
                result.setErrorMessage("AI生成失败: " + response.getExplanation());
                return result;
            }

            // 提取生成的文件列表
            List<String> generatedFiles = extractGeneratedFiles(response.getCode());
            result.setGeneratedFiles(generatedFiles);

            // 生成代码摘要（用于传递给后续模块）
            String codeSummary = generateCodeSummary(moduleInfo.getName(), response.getCode());
            result.setCodeSummary(codeSummary);

            log.info("模块 [{}] 生成完成，文件数: {}", moduleInfo.getName(), generatedFiles.size());

        } catch (Exception e) {
            log.error("模块生成异常: {}", moduleInfo.getName(), e);
            result.setSuccess(false);
            result.setErrorMessage("生成异常: " + e.getMessage());
        }

        return result;
    }

    /**
     * 从代码中提取生成的文件列表
     */
    private List<String> extractGeneratedFiles(String code) {
        List<String> files = new ArrayList<>();
        if (code == null || code.isEmpty()) {
            return files;
        }

        // 匹配 ===FILE:文件路径=== 格式
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "===FILE:(.+?)===",
                java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher matcher = pattern.matcher(code);

        while (matcher.find()) {
            String filePath = matcher.group(1).trim();
            if (!filePath.isEmpty()) {
                files.add(filePath);
            }
        }

        return files;
    }

    /**
     * 生成代码摘要（用于传递给后续模块）
     * 提取关键类名、接口定义等
     */
    private String generateCodeSummary(String moduleName, String code) {
        StringBuilder summary = new StringBuilder();
        summary.append(moduleName).append(": ");

        // 提取Java类名
        java.util.regex.Pattern classPattern = java.util.regex.Pattern.compile(
                "(?:class|interface|enum)\\s+(\\w+)");
        java.util.regex.Matcher classMatcher = classPattern.matcher(code);

        List<String> classNames = new ArrayList<>();
        while (classMatcher.find() && classNames.size() < 5) {
            classNames.add(classMatcher.group(1));
        }

        if (!classNames.isEmpty()) {
            summary.append(String.join(", ", classNames));
        } else {
            summary.append("无关键类");
        }

        // 提取关键方法（简化版）
        java.util.regex.Pattern methodPattern = java.util.regex.Pattern.compile(
                "(?:public|private|protected)\\s+(?:static\\s+)?(?:[\\w<>\\[\\]]+\\s+)?(\\w+)\\s*\\(");
        java.util.regex.Matcher methodMatcher = methodPattern.matcher(code);

        List<String> methodNames = new ArrayList<>();
        while (methodMatcher.find() && methodNames.size() < 3) {
            String methodName = methodMatcher.group(1);
            if (!methodName.equals(classNames.stream().findFirst().orElse(""))) {
                methodNames.add(methodName);
            }
        }

        if (!methodNames.isEmpty()) {
            summary.append(" | 方法: ").append(String.join(", ", methodNames));
        }

        return summary.toString();
    }

    /**
     * 生成指定模块（用于前端选择性生成）
     */
    public ModuleGenerationResult generateSingleModule(
            String projectId,
            String requirementDoc,
            String taskDoc,
            String moduleName,
            Map<String, String> generatedModulesSummary) {

        // 解析任务书获取模块信息
        TaskDocumentParser.ParseResult parseResult = taskDocumentParser.parse(taskDoc);
        if (!parseResult.isSuccess()) {
            ModuleGenerationResult result = new ModuleGenerationResult(moduleName);
            result.setSuccess(false);
            result.setErrorMessage("任务书解析失败");
            return result;
        }

        // 查找指定模块
        TaskDocumentParser.ModuleInfo targetModule = parseResult.getModules().stream()
                .filter(m -> m.getName().equals(moduleName))
                .findFirst()
                .orElse(null);

        if (targetModule == null) {
            ModuleGenerationResult result = new ModuleGenerationResult(moduleName);
            result.setSuccess(false);
            result.setErrorMessage("未找到模块: " + moduleName);
            return result;
        }

        return generateModule(projectId, requirementDoc, taskDoc, targetModule,
                generatedModulesSummary, false);
    }

    /**
     * 解析生成的代码，提取文件
     * @param generatedCode AI生成的代码
     * @return 文件路径到内容的映射
     */
    public Map<String, String> parseGeneratedCode(String generatedCode) {
        Map<String, String> files = new HashMap<>();
        if (generatedCode == null || generatedCode.isEmpty()) {
            return files;
        }

        // 匹配 ===FILE:文件路径=== ... ===END=== 格式
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "===FILE:(.+?)===\\s*\\n(.*?)\\n===END===",
                java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher matcher = pattern.matcher(generatedCode);

        while (matcher.find()) {
            String filePath = matcher.group(1).trim();
            String content = matcher.group(2).trim();
            if (!filePath.isEmpty()) {
                files.put(filePath, content);
            }
        }

        return files;
    }

    /**
     * 保存生成的文件
     * @param files 文件路径到内容的映射
     * @param outputPath 输出目录
     * @param strategy 保存策略：skip/overwrite/merge
     */
    public void saveGeneratedFiles(Map<String, String> files, String outputPath, String strategy) {
        java.nio.file.Path basePath = java.nio.file.Paths.get(outputPath);

        try {
            for (Map.Entry<String, String> entry : files.entrySet()) {
                String filePath = entry.getKey();
                String content = entry.getValue();

                java.nio.file.Path fullPath = basePath.resolve(filePath);

                // 检查文件是否存在
                if (java.nio.file.Files.exists(fullPath)) {
                    if ("skip".equals(strategy)) {
                        log.info("跳过已存在文件: {}", filePath);
                        continue;
                    } else if ("merge".equals(strategy)) {
                        log.info("合并文件: {}", filePath);
                        // 简单合并：在文件末尾添加新内容
                        String existing = new String(java.nio.file.Files.readAllBytes(fullPath));
                        content = existing + "\n\n// === Merged Content ===\n\n" + content;
                    }
                    // overwrite策略直接覆盖
                }

                // 创建目录
                java.nio.file.Files.createDirectories(fullPath.getParent());

                // 写入文件
                java.nio.file.Files.writeString(fullPath, content);
                log.info("保存文件: {}", fullPath);
            }
        } catch (Exception e) {
            log.error("保存文件失败", e);
        }
    }

    /**
     * 生成代码摘要（从文件映射）
     * @param files 文件路径到内容的映射
     * @return 代码摘要
     */
    public String generateCodeSummary(Map<String, String> files) {
        StringBuilder summary = new StringBuilder();

        // 合并所有代码
        StringBuilder allCode = new StringBuilder();
        for (Map.Entry<String, String> entry : files.entrySet()) {
            allCode.append("\n// File: ").append(entry.getKey()).append("\n");
            allCode.append(entry.getValue()).append("\n");
        }

        String code = allCode.toString();

        // 提取Java类名
        java.util.regex.Pattern classPattern = java.util.regex.Pattern.compile(
                "(?:class|interface|enum)\\s+(\\w+)");
        java.util.regex.Matcher classMatcher = classPattern.matcher(code);

        List<String> classNames = new ArrayList<>();
        while (classMatcher.find() && classNames.size() < 10) {
            classNames.add(classMatcher.group(1));
        }

        if (!classNames.isEmpty()) {
            summary.append("关键类: ").append(String.join(", ", classNames));
        }

        // 提取API端点
        java.util.regex.Pattern apiPattern = java.util.regex.Pattern.compile(
                "@(GetMapping|PostMapping|PutMapping|DeleteMapping)\\s*\\(\\s*['\"]?([^'\"\\)]+)");
        java.util.regex.Matcher apiMatcher = apiPattern.matcher(code);

        List<String> apiEndpoints = new ArrayList<>();
        while (apiMatcher.find() && apiEndpoints.size() < 5) {
            apiEndpoints.add(apiMatcher.group(2));
        }

        if (!apiEndpoints.isEmpty()) {
            summary.append(" | API端点: ").append(String.join(", ", apiEndpoints));
        }

        return summary.toString();
    }

    /**
     * 判断是否为代码文件
     */
    public boolean isCodeFile(String filename) {
        return filename.endsWith(".java") || filename.endsWith(".js")
            || filename.endsWith(".ts") || filename.endsWith(".vue")
            || filename.endsWith(".sql") || filename.endsWith(".xml")
            || filename.endsWith(".yml") || filename.endsWith(".yaml")
            || filename.endsWith(".json");
    }
}
