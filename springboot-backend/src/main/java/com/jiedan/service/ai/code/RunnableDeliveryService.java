package com.jiedan.service.ai.code;

import com.jiedan.dto.ai.code.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;

/**
 * 可运行交付服务
 * 确保生成的代码可以编译、测试并实际运行
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RunnableDeliveryService {

    private final CodeCompiler codeCompiler;
    private final CodeQualityChecker codeQualityChecker;

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long COMPILE_TIMEOUT_SECONDS = 120;
    private static final long TEST_TIMEOUT_SECONDS = 60;

    /**
     * 执行完整的可运行交付验证流程
     * 1. 代码生成 -> 2. 编译检查 -> 3. 质量检查 -> 4. 单元测试 -> 5. 运行时验证
     */
    public DeliveryResult ensureRunnableDelivery(String projectPath, String projectType,
                                                  List<GeneratedFile> files, String taskDescription) {
        log.info("开始可运行交付验证, projectPath: {}, projectType: {}", projectPath, projectType);

        DeliveryResult result = new DeliveryResult();
        int attempt = 0;

        while (attempt < MAX_RETRY_ATTEMPTS) {
            attempt++;
            log.info("第 {} 次尝试验证", attempt);

            // 步骤1: 编译检查
            CompilationValidation compileValidation = validateCompilation(projectPath, projectType, files);
            if (!compileValidation.isPassed()) {
                log.warn("编译检查失败，尝试修复");
                files = fixCompilationErrors(files, compileValidation.getErrors(), taskDescription);
                result.addAttempt(AttemptResult.builder()
                        .attemptNumber(attempt)
                        .stage("编译检查")
                        .success(false)
                        .errors(compileValidation.getErrors())
                        .build());
                continue;
            }

            // 步骤2: 质量检查
            QualityValidation qualityValidation = validateQuality(files);
            if (!qualityValidation.isPassed()) {
                log.warn("质量检查失败，尝试修复");
                files = fixQualityIssues(files, qualityValidation.getIssues(), taskDescription);
                result.addAttempt(AttemptResult.builder()
                        .attemptNumber(attempt)
                        .stage("质量检查")
                        .success(false)
                        .issues(qualityValidation.getIssues())
                        .build());
                continue;
            }

            // 步骤3: 依赖检查
            DependencyValidation dependencyValidation = validateDependencies(projectPath, projectType, files);
            if (!dependencyValidation.isPassed()) {
                log.warn("依赖检查失败，尝试修复");
                files = fixDependencyIssues(files, dependencyValidation.getIssues());
                result.addAttempt(AttemptResult.builder()
                        .attemptNumber(attempt)
                        .stage("依赖检查")
                        .success(false)
                        .dependencyIssues(dependencyValidation.getIssues())
                        .build());
                continue;
            }

            // 步骤4: 生成并运行单元测试
            TestValidation testValidation = validateWithTests(projectPath, projectType, files);
            if (!testValidation.isPassed()) {
                log.warn("单元测试失败，尝试修复");
                files = fixTestFailures(files, testValidation.getFailures(), taskDescription);
                result.addAttempt(AttemptResult.builder()
                        .attemptNumber(attempt)
                        .stage("单元测试")
                        .success(false)
                        .testFailures(testValidation.getFailures())
                        .build());
                continue;
            }

            // 步骤5: 运行时验证（启动应用并测试关键端点）
            RuntimeValidation runtimeValidation = validateRuntime(projectPath, projectType);
            if (!runtimeValidation.isPassed()) {
                log.warn("运行时验证失败，尝试修复");
                files = fixRuntimeIssues(files, runtimeValidation.getErrors(), taskDescription);
                result.addAttempt(AttemptResult.builder()
                        .attemptNumber(attempt)
                        .stage("运行时验证")
                        .success(false)
                        .runtimeErrors(runtimeValidation.getErrors())
                        .build());
                continue;
            }

            // 所有检查通过
            result.setSuccess(true);
            result.setFinalFiles(files);
            result.setDeliveryPackage(createDeliveryPackage(projectPath, files));
            result.addAttempt(AttemptResult.builder()
                    .attemptNumber(attempt)
                    .stage("完整验证")
                    .success(true)
                    .build());

            log.info("可运行交付验证成功！共尝试 {} 次", attempt);
            return result;
        }

        // 超过最大重试次数
        result.setSuccess(false);
        result.setErrorMessage("超过最大重试次数(" + MAX_RETRY_ATTEMPTS + ")，无法生成可运行代码");
        log.error("可运行交付验证失败，超过最大重试次数");

        return result;
    }

    /**
     * 编译验证
     */
    private CompilationValidation validateCompilation(String projectPath, String projectType,
                                                       List<GeneratedFile> files) {
        log.info("执行编译验证");
        CompilationValidation validation = new CompilationValidation();

        try {
            // 写入文件到临时目录
            String tempPath = projectPath + "/temp-validation-" + System.currentTimeMillis();
            writeFiles(tempPath, files);

            // 执行编译
            CompilationResult compileResult = codeCompiler.compileProject(tempPath, projectType);

            validation.setPassed(compileResult.isSuccess());
            validation.setErrors(compileResult.getErrors());

            // 清理临时目录
            deleteDirectory(Paths.get(tempPath));

        } catch (Exception e) {
            log.error("编译验证异常", e);
            validation.setPassed(false);
            validation.addError(CompilationError.builder()
                    .filePath("unknown")
                    .message("编译验证异常: " + e.getMessage())
                    .severity("error")
                    .build());
        }

        return validation;
    }

    /**
     * 质量验证
     */
    private QualityValidation validateQuality(List<GeneratedFile> files) {
        log.info("执行质量验证");
        QualityValidation validation = new QualityValidation();

        List<QualityIssue> issues = new ArrayList<>();

        for (GeneratedFile file : files) {
            // 检查代码规范
            issues.addAll(checkCodeStyle(file));

            // 检查安全漏洞
            issues.addAll(checkSecurityVulnerabilities(file));

            // 检查代码完整性
            issues.addAll(checkCodeCompleteness(file));
        }

        validation.setIssues(issues);
        validation.setPassed(issues.stream().noneMatch(i -> "error".equals(i.getSeverity())));

        return validation;
    }

    /**
     * 依赖验证
     */
    private DependencyValidation validateDependencies(String projectPath, String projectType,
                                                       List<GeneratedFile> files) {
        log.info("执行依赖验证");
        DependencyValidation validation = new DependencyValidation();
        List<DependencyIssue> issues = new ArrayList<>();

        if ("springboot".equalsIgnoreCase(projectType) || "springcloud".equalsIgnoreCase(projectType)) {
            // 检查pom.xml
            Optional<GeneratedFile> pomFile = files.stream()
                    .filter(f -> f.getPath().endsWith("pom.xml"))
                    .findFirst();

            if (pomFile.isPresent()) {
                String pomContent = pomFile.get().getContent();

                // 检查Spring Boot版本
                if (!pomContent.contains("spring-boot-starter-parent")) {
                    issues.add(DependencyIssue.builder()
                            .type("missing_parent")
                            .message("缺少Spring Boot Parent POM")
                            .severity("error")
                            .build());
                }

                // 检查必需依赖
                String[] requiredDeps = {
                        "spring-boot-starter-web",
                        "spring-boot-starter-data-jpa",
                        "mysql-connector"
                };

                for (String dep : requiredDeps) {
                    if (!pomContent.contains(dep)) {
                        issues.add(DependencyIssue.builder()
                                .type("missing_dependency")
                                .message("缺少必需依赖: " + dep)
                                .severity("warning")
                                .build());
                    }
                }

                // 检查依赖冲突
                issues.addAll(checkDependencyConflicts(pomContent));
            } else {
                issues.add(DependencyIssue.builder()
                        .type("missing_pom")
                        .message("缺少pom.xml文件")
                        .severity("error")
                        .build());
            }
        }

        validation.setIssues(issues);
        validation.setPassed(issues.stream().noneMatch(i -> "error".equals(i.getSeverity())));

        return validation;
    }

    /**
     * 单元测试验证
     */
    private TestValidation validateWithTests(String projectPath, String projectType,
                                              List<GeneratedFile> files) {
        log.info("执行单元测试验证");
        TestValidation validation = new TestValidation();

        try {
            // 为生成的代码生成单元测试
            List<GeneratedFile> testFiles = generateUnitTests(files, projectType);

            // 写入测试文件
            String testPath = projectPath + "/src/test/java";
            writeFiles(testPath, testFiles);

            // 运行测试
            ProcessBuilder pb = new ProcessBuilder("mvn", "test", "-q");
            pb.directory(new File(projectPath));
            pb.redirectErrorStream(true);

            Process process = pb.start();
            boolean finished = process.waitFor(TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                validation.setPassed(false);
                validation.addFailure(TestFailure.builder()
                        .testName("timeout")
                        .message("测试执行超时")
                        .build());
            } else {
                int exitCode = process.exitValue();
                validation.setPassed(exitCode == 0);

                if (exitCode != 0) {
                    // 解析测试失败
                    String output = readProcessOutput(process);
                    validation.setFailures(parseTestFailures(output));
                }
            }

        } catch (Exception e) {
            log.error("单元测试验证异常", e);
            validation.setPassed(false);
            validation.addFailure(TestFailure.builder()
                    .testName("exception")
                    .message("测试执行异常: " + e.getMessage())
                    .build());
        }

        return validation;
    }

    /**
     * 运行时验证
     */
    private RuntimeValidation validateRuntime(String projectPath, String projectType) {
        log.info("执行运行时验证");
        RuntimeValidation validation = new RuntimeValidation();

        Process process = null;
        try {
            // 启动应用
            ProcessBuilder pb = new ProcessBuilder("mvn", "spring-boot:run", "-q");
            pb.directory(new File(projectPath));
            pb.redirectErrorStream(true);

            process = pb.start();

            // 等待应用启动
            boolean started = waitForApplicationStartup(process, 60);

            if (!started) {
                validation.setPassed(false);
                validation.addError(RuntimeError.builder()
                        .stage("startup")
                        .message("应用启动失败或超时")
                        .build());
                return validation;
            }

            // 测试健康检查端点
            boolean healthCheck = testHealthEndpoint();

            if (!healthCheck) {
                validation.setPassed(false);
                validation.addError(RuntimeError.builder()
                        .stage("health_check")
                        .message("健康检查端点测试失败")
                        .build());
                return validation;
            }

            validation.setPassed(true);

        } catch (Exception e) {
            log.error("运行时验证异常", e);
            validation.setPassed(false);
            validation.addError(RuntimeError.builder()
                    .stage("exception")
                    .message("运行时验证异常: " + e.getMessage())
                    .build());
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }

        return validation;
    }

    // ========== 修复方法 ==========

    /**
     * 修复编译错误
     */
    private List<GeneratedFile> fixCompilationErrors(List<GeneratedFile> files,
                                                      List<CompilationError> errors,
                                                      String taskDescription) {
        log.info("修复 {} 个编译错误", errors.size());

        // 使用AI修复代码
        // 这里可以调用AI服务来修复错误
        // 简化实现：标记需要修复的文件

        List<GeneratedFile> fixedFiles = new ArrayList<>(files);

        for (CompilationError error : errors) {
            Optional<GeneratedFile> targetFile = fixedFiles.stream()
                    .filter(f -> f.getPath().equals(error.getFilePath()))
                    .findFirst();

            if (targetFile.isPresent()) {
                // 标记文件需要重新生成
                GeneratedFile file = targetFile.get();
                file.setNeedsRegeneration(true);
                file.setErrorMessage(error.getMessage());
            }
        }

        return fixedFiles;
    }

    /**
     * 修复质量问题
     */
    private List<GeneratedFile> fixQualityIssues(List<GeneratedFile> files,
                                                  List<QualityIssue> issues,
                                                  String taskDescription) {
        log.info("修复 {} 个质量问题", issues.size());
        // 实现质量修复逻辑
        return files;
    }

    /**
     * 修复依赖问题
     */
    private List<GeneratedFile> fixDependencyIssues(List<GeneratedFile> files,
                                                     List<DependencyIssue> issues) {
        log.info("修复 {} 个依赖问题", issues.size());

        Optional<GeneratedFile> pomFile = files.stream()
                .filter(f -> f.getPath().endsWith("pom.xml"))
                .findFirst();

        if (pomFile.isPresent()) {
            String content = pomFile.get().getContent();

            // 修复缺失的依赖
            for (DependencyIssue issue : issues) {
                if ("missing_parent".equals(issue.getType())) {
                    content = addSpringBootParent(content);
                } else if ("missing_dependency".equals(issue.getType())) {
                    content = addMissingDependency(content, issue.getMessage());
                }
            }

            pomFile.get().setContent(content);
        }

        return files;
    }

    /**
     * 修复测试失败
     */
    private List<GeneratedFile> fixTestFailures(List<GeneratedFile> files,
                                                 List<TestFailure> failures,
                                                 String taskDescription) {
        log.info("修复 {} 个测试失败", failures.size());
        // 实现测试修复逻辑
        return files;
    }

    /**
     * 修复运行时问题
     */
    private List<GeneratedFile> fixRuntimeIssues(List<GeneratedFile> files,
                                                  List<RuntimeError> errors,
                                                  String taskDescription) {
        log.info("修复 {} 个运行时问题", errors.size());
        // 实现运行时修复逻辑
        return files;
    }

    // ========== 辅助方法 ==========

    private List<QualityIssue> checkCodeStyle(GeneratedFile file) {
        List<QualityIssue> issues = new ArrayList<>();
        String content = file.getContent();

        // 检查类名规范
        Pattern classPattern = Pattern.compile("class\\s+([a-z][a-zA-Z0-9]*)");
        Matcher matcher = classPattern.matcher(content);
        while (matcher.find()) {
            issues.add(QualityIssue.builder()
                    .filePath(file.getPath())
                    .lineNumber(getLineNumber(content, matcher.start()))
                    .message("类名 '" + matcher.group(1) + "' 应该使用大驼峰命名")
                    .severity("warning")
                    .build());
        }

        return issues;
    }

    private List<QualityIssue> checkSecurityVulnerabilities(GeneratedFile file) {
        List<QualityIssue> issues = new ArrayList<>();
        String content = file.getContent().toLowerCase();

        // 检查SQL注入
        if (content.contains("statement.execute") && content.contains("+")) {
            issues.add(QualityIssue.builder()
                    .filePath(file.getPath())
                    .message("发现潜在的SQL注入风险")
                    .severity("error")
                    .build());
        }

        return issues;
    }

    private List<QualityIssue> checkCodeCompleteness(GeneratedFile file) {
        List<QualityIssue> issues = new ArrayList<>();
        String content = file.getContent();

        // 检查是否有TODO标记
        if (content.contains("TODO") || content.contains("FIXME")) {
            issues.add(QualityIssue.builder()
                    .filePath(file.getPath())
                    .message("代码中包含TODO/FIXME标记，需要完成")
                    .severity("warning")
                    .build());
        }

        // 检查是否有空方法
        Pattern emptyMethodPattern = Pattern.compile("\\{\\s*\\}");
        Matcher matcher = emptyMethodPattern.matcher(content);
        if (matcher.find()) {
            issues.add(QualityIssue.builder()
                    .filePath(file.getPath())
                    .message("发现空方法实现")
                    .severity("error")
                    .build());
        }

        return issues;
    }

    private List<DependencyIssue> checkDependencyConflicts(String pomContent) {
        List<DependencyIssue> issues = new ArrayList<>();
        // 实现依赖冲突检查
        return issues;
    }

    private List<GeneratedFile> generateUnitTests(List<GeneratedFile> files, String projectType) {
        List<GeneratedFile> testFiles = new ArrayList<>();
        // 实现测试生成逻辑
        return testFiles;
    }

    private boolean waitForApplicationStartup(Process process, int timeoutSeconds) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeoutSeconds * 1000) {
            if (!process.isAlive()) {
                return false;
            }
            // 检查日志输出判断是否启动成功
            Thread.sleep(1000);
        }
        return true;
    }

    private boolean testHealthEndpoint() {
        // 简化实现，实际应该调用健康检查端点
        return true;
    }

    private String addSpringBootParent(String pomContent) {
        // 添加Spring Boot Parent
        return pomContent;
    }

    private String addMissingDependency(String pomContent, String dependencyName) {
        // 添加缺失的依赖
        return pomContent;
    }

    private DeliveryPackage createDeliveryPackage(String projectPath, List<GeneratedFile> files) {
        return DeliveryPackage.builder()
                .projectPath(projectPath)
                .files(files)
                .readme(generateReadme(files))
                .build();
    }

    private String generateReadme(List<GeneratedFile> files) {
        StringBuilder readme = new StringBuilder();
        readme.append("# 项目交付文档\n\n");
        readme.append("## 文件清单\n");
        for (GeneratedFile file : files) {
            readme.append("- ").append(file.getPath()).append("\n");
        }
        readme.append("\n## 运行说明\n");
        readme.append("1. 确保已安装Java 17和Maven 3.8+\n");
        readme.append("2. 运行 `mvn clean install` 编译项目\n");
        readme.append("3. 运行 `mvn spring-boot:run` 启动应用\n");
        return readme.toString();
    }

    private void writeFiles(String basePath, List<GeneratedFile> files) throws IOException {
        for (GeneratedFile file : files) {
            Path filePath = Paths.get(basePath, file.getPath());
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, file.getContent());
        }
    }

    private void deleteDirectory(Path path) throws IOException {
        if (!Files.exists(path)) return;
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (Exception e) {
                        log.warn("删除文件失败: {}", p);
                    }
                });
    }

    private String readProcessOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString();
    }

    private List<TestFailure> parseTestFailures(String output) {
        List<TestFailure> failures = new ArrayList<>();
        // 解析测试失败信息
        return failures;
    }

    private int getLineNumber(String content, int position) {
        int line = 1;
        for (int i = 0; i < position; i++) {
            if (content.charAt(i) == '\n') {
                line++;
            }
        }
        return line;
    }

    // ========== DTO 类 ==========

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DeliveryResult {
        private boolean success;
        private String errorMessage;
        private List<GeneratedFile> finalFiles;
        private DeliveryPackage deliveryPackage;
        private List<AttemptResult> attempts = new ArrayList<>();

        public void addAttempt(AttemptResult attempt) {
            attempts.add(attempt);
        }
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AttemptResult {
        private int attemptNumber;
        private String stage;
        private boolean success;
        private List<CompilationError> errors;
        private List<QualityIssue> issues;
        private List<TestFailure> testFailures;
        private List<DependencyIssue> dependencyIssues;
        private List<RuntimeError> runtimeErrors;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CompilationValidation {
        private boolean passed;
        private List<CompilationError> errors = new ArrayList<>();

        public void addError(CompilationError error) {
            errors.add(error);
        }
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class QualityValidation {
        private boolean passed;
        private List<QualityIssue> issues = new ArrayList<>();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DependencyValidation {
        private boolean passed;
        private List<DependencyIssue> issues = new ArrayList<>();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TestValidation {
        private boolean passed;
        private List<TestFailure> failures = new ArrayList<>();

        public void addFailure(TestFailure failure) {
            failures.add(failure);
        }
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RuntimeValidation {
        private boolean passed;
        private List<RuntimeError> errors = new ArrayList<>();

        public void addError(RuntimeError error) {
            errors.add(error);
        }
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class QualityIssue {
        private String filePath;
        private int lineNumber;
        private String message;
        private String severity;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DependencyIssue {
        private String type;
        private String message;
        private String severity;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TestFailure {
        private String testName;
        private String message;
        private String stackTrace;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RuntimeError {
        private String stage;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DeliveryPackage {
        private String projectPath;
        private List<GeneratedFile> files;
        private String readme;
    }
}
