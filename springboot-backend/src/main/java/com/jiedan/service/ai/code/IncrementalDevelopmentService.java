package com.jiedan.service.ai.code;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiedan.dto.ai.AiChatRequest;
import com.jiedan.dto.ai.AiChatResponse;
import com.jiedan.dto.ai.code.*;
import com.jiedan.entity.CodeContext;
import com.jiedan.entity.TaskStatus;
import com.jiedan.repository.CodeContextRepository;
import com.jiedan.repository.TaskStatusRepository;
import com.jiedan.service.ai.AIProviderStrategy;
import com.jiedan.service.ai.AiStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.jiedan.repository.CodeStyleRepository;

/**
 * 增量开发服务
 * 负责任务循环开发（提取→开发→自测→标记）
 * 核心组件，实现Type 2代码生成
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IncrementalDevelopmentService {

    private final AiStrategyFactory strategyFactory;
    private final TaskScheduler taskScheduler;
    private final CodeCompiler codeCompiler;
    private final TaskStatusRepository taskStatusRepository;
    private final CodeContextRepository codeContextRepository;
    private final CodeStyleRepository codeStyleRepository;
    private final ObjectMapper objectMapper;

    // 最大重试次数
    private static final int MAX_RETRY_COUNT = 3;
    // Token限制
    private static final int MAX_INPUT_TOKENS = 8000;
    private static final int MAX_OUTPUT_TOKENS = 4000;
    // 最大迭代次数（防止无限循环）
    private static final int MAX_ITERATIONS = 1000;

    /**
     * 执行增量开发循环
     * 主入口：提取→开发→自测→标记
     */
    public void executeIncrementalDevelopment(String projectId, String projectType, String prdSummary) {
        log.info("开始增量开发循环, projectId: {}, projectType: {}", projectId, projectType);

        int iterations = 0;
        while (iterations < MAX_ITERATIONS) {
            iterations++;

            // 1. 提取下一个可执行任务
            TaskStatus task = taskScheduler.getNextExecutableTask(projectId);

            if (task == null) {
                // 检查是否所有任务完成
                if (taskScheduler.isAllTasksCompleted(projectId)) {
                    log.info("所有任务已完成, projectId: {}", projectId);
                    break;
                } else {
                    log.warn("没有可执行任务，但仍有未完成任务，可能存在依赖问题, projectId: {}", projectId);
                    break;
                }
            }

            // 2. 开发任务
            boolean success = developTask(projectId, projectType, task, prdSummary);

            if (!success) {
                log.error("任务开发失败, taskId: {}", task.getTaskId());
                // 继续处理下一个任务，避免阻塞
            }
        }

        if (iterations >= MAX_ITERATIONS) {
            log.error("达到最大迭代次数限制({})，强制退出, projectId: {}", MAX_ITERATIONS, projectId);
        }
    }

    /**
     * 开发单个任务
     * 包含：生成代码→编译检查→自测修复→保存上下文→标记完成
     */
    private boolean developTask(String projectId, String projectType, TaskStatus task, String prdSummary) {
        String taskId = task.getTaskId();
        log.info("开始开发任务, projectId: {}, taskId: {}", projectId, taskId);

        // 标记任务进行中
        taskScheduler.markTaskInProgress(projectId, taskId);

        int retryCount = 0;
        List<CompilationError> previousErrors = new ArrayList<>();

        while (retryCount < MAX_RETRY_COUNT) {
            try {
                // 1. 获取上下文（已完成任务的代码摘要）
                List<CodeSummary> contextSummaries = taskScheduler.getCompletedTaskSummaries(projectId);

                // 2. 构建Prompt
                String prompt = buildDevelopmentPrompt(task, prdSummary, contextSummaries, previousErrors);

                // 3. 调用AI生成代码
                GenerateCodeResponse response = generateCodeWithAI(prompt);

                if (!response.isSuccess()) {
                    log.error("代码生成失败: {}", response.getErrorMessage());
                    retryCount++;
                    taskScheduler.incrementRetryCount(projectId, taskId);
                    continue;
                }

                // 4. 编译检查
                String projectPath = "projects/" + projectId;
                CompilationResult compileResult = codeCompiler.compileTaskCode(
                        projectPath, taskId, response.getFiles(), projectType);

                if (!compileResult.isSuccess()) {
                    log.warn("编译失败，需要修复, taskId: {}, 错误数: {}",
                            taskId, compileResult.getErrors().size());

                    // 保存错误信息，用于下一次重试
                    previousErrors = compileResult.getErrors();
                    retryCount++;
                    taskScheduler.incrementRetryCount(projectId, taskId);
                    continue;
                }

                // 5. 编译通过，保存代码
                saveTaskCode(projectPath, taskId, response.getFiles());

                // 6. 提取代码摘要，保存上下文
                CodeSummary codeSummary = extractCodeSummary(taskId, task.getTaskName(), response.getFiles());
                saveCodeContext(projectId, taskId, codeSummary);

                // 7. 提取并保存代码风格（只有第一个完成的任务会保存）
                extractAndSaveCodeStyle(projectId, response.getFiles());

                // 8. 标记任务完成
                taskScheduler.markTaskCompleted(projectId, taskId);

                log.info("任务开发完成, projectId: {}, taskId: {}", projectId, taskId);
                return true;

            } catch (Exception e) {
                log.error("任务开发异常, taskId: {}", taskId, e);
                retryCount++;
                taskScheduler.incrementRetryCount(projectId, taskId);
            }
        }

        // 超过最大重试次数，标记失败
        taskScheduler.markTaskFailed(projectId, taskId, "超过最大重试次数，开发失败");
        return false;
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
                    .addSystemMessage("你是一位资深开发工程师。请根据需求生成高质量的代码。")
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
     * 构建开发Prompt
     */
    private String buildDevelopmentPrompt(TaskStatus task, String prdSummary,
                                          List<CodeSummary> contextSummaries,
                                          List<CompilationError> previousErrors) {
        StringBuilder prompt = new StringBuilder();

        // 1. 角色定义
        prompt.append("【角色】\n");
        prompt.append("你是一位资深开发工程师，负责实现具体的开发任务。\n\n");

        // 2. 任务信息
        prompt.append("【当前任务】\n");
        prompt.append("任务ID: ").append(task.getTaskId()).append("\n");
        prompt.append("任务名称: ").append(task.getTaskName()).append("\n");
        prompt.append("任务类型: ").append(task.getTaskType()).append("\n");
        prompt.append("优先级: ").append(task.getPriority()).append("\n\n");

        // 3. PRD摘要（截断控制Token）
        prompt.append("【PRD摘要】\n");
        String truncatedPrd = truncateContent(prdSummary, 2000);
        prompt.append(truncatedPrd).append("\n\n");

        // 4. 上下文（已完成任务的代码摘要）
        if (!contextSummaries.isEmpty()) {
            prompt.append("【已完成的依赖任务代码摘要】\n");
            prompt.append("这些任务已完成，你可以依赖它们的代码：\n\n");

            for (CodeSummary summary : contextSummaries) {
                prompt.append("任务: ").append(summary.getTaskName()).append("\n");
                for (FileSummary file : summary.getFiles()) {
                    prompt.append("  - 文件: ").append(file.getFilePath()).append("\n");
                    if (file.getClassName() != null) {
                        prompt.append("    类名: ").append(file.getClassName()).append("\n");
                    }
                    if (file.getPublicMethods() != null && !file.getPublicMethods().isEmpty()) {
                        prompt.append("    方法: ").append(
                                String.join(", ", file.getPublicMethods())).append("\n");
                    }
                }
                prompt.append("\n");
            }
        }

        // 5. 之前的编译错误（如果有）
        if (!previousErrors.isEmpty()) {
            prompt.append("【需要修复的编译错误】\n");
            prompt.append("之前的代码存在以下编译错误，请修复：\n\n");

            for (int i = 0; i < previousErrors.size() && i < 10; i++) {
                CompilationError error = previousErrors.get(i);
                prompt.append(i + 1).append(". ").append(error.getFilePath());
                if (error.getLineNumber() != null) {
                    prompt.append(":").append(error.getLineNumber());
                }
                prompt.append("\n   ").append(error.getMessage()).append("\n");
            }
            prompt.append("\n");
        }

        // 6. 开发要求
        prompt.append("【开发要求】\n");
        prompt.append("1. 只实现当前任务的功能，不要实现其他任务的功能\n");
        prompt.append("2. 代码必须能编译通过，不能有任何编译错误\n");
        prompt.append("3. 遵循项目中已有的代码风格和命名规范\n");
        prompt.append("4. 正确使用依赖任务提供的类和方法\n");
        prompt.append("5. 添加必要的注释说明关键逻辑\n");
        prompt.append("6. 如果是Java代码，确保包路径正确\n\n");

        // 7. 输出格式
        prompt.append("【输出格式】\n");
        prompt.append("请按以下格式输出代码文件：\n\n");
        prompt.append("```\n");
        prompt.append("## 文件列表\n\n");
        prompt.append("### {文件路径}\n");
        prompt.append("```{语言}\n");
        prompt.append("{代码内容}\n");
        prompt.append("```\n");
        prompt.append("**说明**: {文件说明}\n");
        prompt.append("```\n\n");

        prompt.append("请生成完整的代码文件。");

        return prompt.toString();
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

    /**
     * 提取代码内容
     */
    private String extractCodeContent(String section) {
        int start = section.indexOf("```");
        if (start == -1) return "";

        start = section.indexOf("\n", start);
        if (start == -1) return "";

        int end = section.indexOf("```", start + 1);
        if (end == -1) return "";

        return section.substring(start + 1, end).trim();
    }

    /**
     * 提取文件说明
     */
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

    /**
     * 检测编程语言
     */
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

    /**
     * 保存任务代码
     */
    private void saveTaskCode(String projectPath, String taskId, List<GeneratedFile> files) throws IOException {
        String taskCodePath = projectPath + "/src/task-" + taskId;

        for (GeneratedFile file : files) {
            Path filePath = Paths.get(taskCodePath, file.getPath());
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, file.getContent());
            log.info("保存任务代码文件: {}", filePath);
        }

        // 同时更新任务状态中的代码路径
        TaskStatus task = taskStatusRepository
                .findByProjectIdAndTaskId(
                        Paths.get(projectPath).getFileName().toString(), taskId)
                .orElse(null);

        if (task != null) {
            task.setGeneratedCodePath(taskCodePath);
            taskStatusRepository.save(task);
        }
    }

    /**
     * 提取代码摘要
     */
    private CodeSummary extractCodeSummary(String taskId, String taskName, List<GeneratedFile> files) {
        List<FileSummary> fileSummaries = new ArrayList<>();

        for (GeneratedFile file : files) {
            FileSummary summary = FileSummary.builder()
                    .filePath(file.getPath())
                    .className(extractClassName(file))
                    .publicMethods(extractPublicMethods(file))
                    .dependencies(extractDependencies(file))
                    .build();

            fileSummaries.add(summary);
        }

        return CodeSummary.builder()
                .taskId(taskId)
                .taskName(taskName)
                .files(fileSummaries)
                .build();
    }

    /**
     * 提取类名
     */
    private String extractClassName(GeneratedFile file) {
        String content = file.getContent();
        String language = file.getLanguage();

        if ("java".equals(language)) {
            // 匹配 public class ClassName
            Pattern pattern = Pattern.compile("public\\s+(?:class|interface|enum)\\s+(\\w+)");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } else if ("javascript".equals(language) || "typescript".equals(language)) {
            // 匹配 class ClassName 或 export class ClassName
            Pattern pattern = Pattern.compile("(?:export\\s+)?class\\s+(\\w+)");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } else if ("python".equals(language)) {
            // 匹配 class ClassName
            Pattern pattern = Pattern.compile("class\\s+(\\w+)");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        return null;
    }

    /**
     * 提取public方法签名
     */
    private List<String> extractPublicMethods(GeneratedFile file) {
        List<String> methods = new ArrayList<>();
        String content = file.getContent();
        String language = file.getLanguage();

        if ("java".equals(language)) {
            // 匹配 public 方法
            Pattern pattern = Pattern.compile(
                    "public\\s+(?:static\\s+)?(?:<[^>]+>\\s+)?([\\w<>,\\s\\[\\]]+)\\s+(\\w+)\\s*\\([^)]*\\)"
            );
            Matcher matcher = pattern.matcher(content);

            while (matcher.find()) {
                String returnType = matcher.group(1).trim();
                String methodName = matcher.group(2);
                methods.add(returnType + " " + methodName + "()");
            }
        } else if ("javascript".equals(language) || "typescript".equals(language)) {
            // 匹配方法定义
            Pattern pattern = Pattern.compile(
                    "(?:async\\s+)?(\\w+)\\s*\\([^)]*\\)\\s*\\{"
            );
            Matcher matcher = pattern.matcher(content);

            while (matcher.find()) {
                methods.add(matcher.group(1) + "()");
            }
        }

        return methods;
    }

    /**
     * 提取依赖的类
     */
    private List<String> extractDependencies(GeneratedFile file) {
        List<String> dependencies = new ArrayList<>();
        String content = file.getContent();
        String language = file.getLanguage();

        if ("java".equals(language)) {
            // 匹配 import 语句
            Pattern pattern = Pattern.compile("import\\s+([\\w.]+);");
            Matcher matcher = pattern.matcher(content);

            while (matcher.find()) {
                String importPath = matcher.group(1);
                // 只保留项目内部依赖（非JDK和第三方库）
                if (!importPath.startsWith("java.") &&
                        !importPath.startsWith("javax.") &&
                        !importPath.startsWith("org.springframework") &&
                        !importPath.startsWith("lombok.")) {
                    dependencies.add(importPath);
                }
            }
        }

        return dependencies;
    }

    /**
     * 保存代码上下文
     */
    private void saveCodeContext(String projectId, String taskId, CodeSummary codeSummary) {
        try {
            for (FileSummary file : codeSummary.getFiles()) {
                CodeContext context = CodeContext.builder()
                        .projectId(projectId)
                        .taskId(taskId)
                        .taskName(codeSummary.getTaskName())
                        .className(file.getClassName())
                        .publicMethods(toJson(file.getPublicMethods()))
                        .dependencies(toJson(file.getDependencies()))
                        .build();

                codeContextRepository.save(context);
            }

            log.info("代码上下文保存完成, projectId: {}, taskId: {}", projectId, taskId);
        } catch (Exception e) {
            log.error("保存代码上下文失败", e);
        }
    }

    /**
     * 提取并保存代码风格（第一个任务完成后调用）
     */
    private void extractAndSaveCodeStyle(String projectId, List<GeneratedFile> files) {
        try {
            // 检查是否已存在代码风格
            Optional<com.jiedan.entity.CodeStyle> existingStyle = codeStyleRepository.findByProjectId(projectId);
            if (existingStyle.isPresent()) {
                log.info("代码风格已存在, projectId: {}", projectId);
                return;
            }

            // 从第一个代码文件提取风格
            GeneratedFile firstFile = files.stream()
                    .filter(f -> f.getContent() != null && !f.getContent().isEmpty())
                    .findFirst()
                    .orElse(null);

            if (firstFile == null) {
                log.warn("没有可提取风格的代码文件, projectId: {}", projectId);
                return;
            }

            com.jiedan.entity.CodeStyle codeStyle = extractCodeStyle(projectId, firstFile);
            if (codeStyle != null) {
                codeStyleRepository.save(codeStyle);
                log.info("代码风格提取并保存成功, projectId: {}", projectId);
            }

        } catch (Exception e) {
            log.error("提取代码风格失败, projectId: {}", projectId, e);
        }
    }

    /**
     * 从代码文件中提取代码风格
     */
    private com.jiedan.entity.CodeStyle extractCodeStyle(String projectId, GeneratedFile file) {
        String content = file.getContent();
        String language = file.getLanguage();

        if (content == null || content.isEmpty()) {
            return null;
        }

        com.jiedan.entity.CodeStyle.CodeStyleBuilder builder = com.jiedan.entity.CodeStyle.builder()
                .projectId(projectId);

        // 提取缩进方式
        String indentation = detectIndentation(content);
        builder.indentation(indentation);

        // 提取命名规范
        String namingConvention = detectNamingConvention(content, language);
        builder.namingConvention(namingConvention);

        // 提取包结构
        String packageStructure = detectPackageStructure(content, language);
        builder.packageStructure(packageStructure);

        return builder.build();
    }

    /**
     * 检测缩进方式
     */
    private String detectIndentation(String content) {
        int tabCount = 0;
        int fourSpaceCount = 0;
        int twoSpaceCount = 0;

        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.startsWith("\t")) {
                tabCount++;
            } else if (line.startsWith("    ")) {
                fourSpaceCount++;
            } else if (line.startsWith("  ")) {
                twoSpaceCount++;
            }
        }

        if (tabCount > fourSpaceCount && tabCount > twoSpaceCount) {
            return "tab";
        } else if (fourSpaceCount > twoSpaceCount) {
            return "4_spaces";
        } else if (twoSpaceCount > 0) {
            return "2_spaces";
        }
        return "4_spaces"; // 默认值
    }

    /**
     * 检测命名规范
     */
    private String detectNamingConvention(String content, String language) {
        if ("java".equals(language)) {
            // Java通常使用驼峰命名
            return "camelCase";
        } else if ("python".equals(language)) {
            // Python通常使用下划线命名
            return "snake_case";
        } else if ("javascript".equals(language) || "typescript".equals(language)) {
            // JavaScript/TypeScript通常使用驼峰命名
            return "camelCase";
        }
        return "camelCase"; // 默认值
    }

    /**
     * 检测包结构
     */
    private String detectPackageStructure(String content, String language) {
        if ("java".equals(language)) {
            Pattern pattern = Pattern.compile("package\\s+([\\w.]+);");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } else if ("python".equals(language)) {
            // Python通常使用模块结构
            return "module_based";
        }
        return "default";
    }

    /**
     * 截断内容（控制Token数量）
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null || content.length() <= maxLength) {
            return content;
        }

        // 保留开头和结尾，中间用省略号
        int headLength = maxLength / 2;
        int tailLength = maxLength / 2 - 10;

        return content.substring(0, headLength) +
                "\n\n... [内容已截断] ...\n\n" +
                content.substring(content.length() - tailLength);
    }

    /**
     * 转换为JSON
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("JSON转换失败", e);
            return "[]";
        }
    }

    /**
     * 从JSON解析
     */
    private <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            log.error("JSON解析失败", e);
            return null;
        }
    }
}
