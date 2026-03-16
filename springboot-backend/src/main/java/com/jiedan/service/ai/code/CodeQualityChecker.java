package com.jiedan.service.ai.code;

import com.jiedan.dto.ai.code.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代码质量检查器
 * 负责代码质量门禁检查：编译检查、代码规范检查、安全检查
 */
@Slf4j
@Service
public class CodeQualityChecker {

    /**
     * 获取代码规范规则列表
     */
    private List<CodeStyleRule> getStyleRules() {
        return Arrays.asList(
                new CodeStyleRule("naming_convention", "命名规范检查", this::checkNamingConvention),
                new CodeStyleRule("line_length", "行长度检查", this::checkLineLength),
                new CodeStyleRule("import_organization", "导入组织检查", this::checkImportOrganization),
                new CodeStyleRule("method_length", "方法长度检查", this::checkMethodLength),
                new CodeStyleRule("comment_ratio", "注释比例检查", this::checkCommentRatio)
        );
    }

    /**
     * 获取安全检查规则列表
     */
    private List<SecurityRule> getSecurityRules() {
        return Arrays.asList(
                new SecurityRule("sql_injection", "SQL注入检查", this::checkSqlInjection),
                new SecurityRule("xss", "XSS攻击检查", this::checkXss),
                new SecurityRule("hardcoded_secret", "硬编码密钥检查", this::checkHardcodedSecrets),
                new SecurityRule("unsafe_deserialization", "不安全反序列化检查", this::checkUnsafeDeserialization),
                new SecurityRule("path_traversal", "路径遍历检查", this::checkPathTraversal)
        );
    }

    /**
     * 执行完整的质量检查
     * 包括：编译检查 + 代码规范检查 + 安全检查
     */
    public QualityCheckResult performQualityCheck(String projectPath, String projectType,
                                                   List<GeneratedFile> files) {
        log.info("开始代码质量检查, projectPath: {}", projectPath);

        QualityCheckResult result = new QualityCheckResult();

        // 1. 编译检查（P0最高优先级）
        CompilationCheckResult compileResult = performCompilationCheck(projectPath, projectType, files);
        result.setCompilationCheck(compileResult);

        // 如果编译失败，直接返回，不进行后续检查
        if (!compileResult.isPassed()) {
            result.setPassed(false);
            result.setOverallMessage("编译检查未通过，请修复编译错误");
            return result;
        }

        // 2. 代码规范检查
        List<StyleCheckResult> styleResults = performStyleCheck(files);
        result.setStyleChecks(styleResults);

        // 3. 安全检查
        List<SecurityCheckResult> securityResults = performSecurityCheck(files);
        result.setSecurityChecks(securityResults);

        // 综合判断
        boolean stylePassed = styleResults.stream().allMatch(StyleCheckResult::isPassed);
        boolean securityPassed = securityResults.stream().allMatch(SecurityCheckResult::isPassed);

        result.setPassed(stylePassed && securityPassed);
        result.setOverallMessage(buildOverallMessage(stylePassed, securityPassed, styleResults, securityResults));

        return result;
    }

    /**
     * 编译检查
     */
    private CompilationCheckResult performCompilationCheck(String projectPath, String projectType,
                                                            List<GeneratedFile> files) {
        CompilationCheckResult result = new CompilationCheckResult();
        List<CompilationError> errors = new ArrayList<>();

        // 参数校验
        if (projectType == null || projectType.trim().isEmpty()) {
            result.setPassed(false);
            errors.add(CompilationError.builder()
                    .filePath("unknown")
                    .message("projectType不能为空")
                    .severity("error")
                    .build());
            result.setErrors(errors);
            return result;
        }

        try {
            switch (projectType.toLowerCase()) {
                case "springboot":
                case "springcloud":
                    errors = compileJavaProject(projectPath, files);
                    break;
                case "python":
                    errors = compilePythonFiles(files);
                    break;
                case "wechat-miniprogram":
                case "douyin-miniprogram":
                    errors = validateMiniprogramFiles(files);
                    break;
                default:
                    log.warn("未知的项目类型: {}", projectType);
            }

            result.setPassed(errors.isEmpty());
            result.setErrors(errors);

        } catch (Exception e) {
            log.error("编译检查异常", e);
            result.setPassed(false);
            errors.add(CompilationError.builder()
                    .filePath("unknown")
                    .message("编译检查异常: " + e.getMessage())
                    .severity("error")
                    .build());
            result.setErrors(errors);
        }

        return result;
    }

    /**
     * 编译Java项目
     */
    private List<CompilationError> compileJavaProject(String projectPath, List<GeneratedFile> files) {
        List<CompilationError> errors = new ArrayList<>();

        try {
            // 1. 将文件写入临时目录
            String tempPath = projectPath + "/temp-compile";
            writeFilesToTemp(tempPath, files);

            // 2. 执行Maven编译
            ProcessBuilder pb = new ProcessBuilder("mvn", "compile", "-q");
            pb.directory(new File(projectPath));
            pb.redirectErrorStream(true);

            Process process = pb.start();
            boolean finished = process.waitFor(120, java.util.concurrent.TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                errors.add(CompilationError.builder()
                        .filePath("pom.xml")
                        .message("编译超时")
                        .severity("error")
                        .build());
                return errors;
            }

            // 3. 解析编译输出
            if (process.exitValue() != 0) {
                errors.addAll(parseMavenErrors(process, files));
            }

            // 4. 清理临时文件
            deleteDirectory(Paths.get(tempPath));

        } catch (Exception e) {
            log.error("Java编译失败", e);
            errors.add(CompilationError.builder()
                    .filePath("unknown")
                    .message("编译失败: " + e.getMessage())
                    .severity("error")
                    .build());
        }

        return errors;
    }

    /**
     * 编译Python文件
     */
    private List<CompilationError> compilePythonFiles(List<GeneratedFile> files) {
        List<CompilationError> errors = new ArrayList<>();

        for (GeneratedFile file : files) {
            if (!"python".equals(file.getLanguage())) {
                continue;
            }

            try {
                // 使用Python的py_compile检查语法
                ProcessBuilder pb = new ProcessBuilder("python", "-m", "py_compile", "-");
                Process process = pb.start();

                process.getOutputStream().write(file.getContent().getBytes());
                process.getOutputStream().close();

                boolean finished = process.waitFor(10, java.util.concurrent.TimeUnit.SECONDS);

                if (!finished) {
                    process.destroyForcibly();
                    errors.add(CompilationError.builder()
                            .filePath(file.getPath())
                            .message("语法检查超时")
                            .severity("error")
                            .build());
                } else if (process.exitValue() != 0) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getErrorStream()));
                    String errorMsg = reader.lines().collect(java.util.stream.Collectors.joining("\n"));

                    errors.add(CompilationError.builder()
                            .filePath(file.getPath())
                            .message(errorMsg)
                            .severity("error")
                            .build());
                }

            } catch (Exception e) {
                log.error("Python语法检查失败: {}", file.getPath(), e);
                errors.add(CompilationError.builder()
                        .filePath(file.getPath())
                        .message("语法检查失败: " + e.getMessage())
                        .severity("error")
                        .build());
            }
        }

        return errors;
    }

    /**
     * 验证小程序文件
     */
    private List<CompilationError> validateMiniprogramFiles(List<GeneratedFile> files) {
        List<CompilationError> errors = new ArrayList<>();

        for (GeneratedFile file : files) {
            String path = file.getPath();

            // 检查JSON文件格式
            if (path.endsWith(".json")) {
                try {
                    new com.fasterxml.jackson.databind.ObjectMapper().readTree(file.getContent());
                } catch (Exception e) {
                    errors.add(CompilationError.builder()
                            .filePath(path)
                            .message("JSON格式错误: " + e.getMessage())
                            .severity("error")
                            .build());
                }
            }

            // 检查WXML文件基本结构
            if (path.endsWith(".wxml")) {
                String content = file.getContent();
                if (!content.contains("<") || !content.contains(">")) {
                    errors.add(CompilationError.builder()
                            .filePath(path)
                            .message("WXML文件结构异常")
                            .severity("error")
                            .build());
                }
            }
        }

        return errors;
    }

    /**
     * 代码规范检查
     */
    private List<StyleCheckResult> performStyleCheck(List<GeneratedFile> files) {
        List<StyleCheckResult> results = new ArrayList<>();

        for (GeneratedFile file : files) {
            for (CodeStyleRule rule : getStyleRules()) {
                StyleCheckResult result = rule.check(file);
                if (!result.isPassed()) {
                    results.add(result);
                }
            }
        }

        return results;
    }

    /**
     * 安全检查
     */
    private List<SecurityCheckResult> performSecurityCheck(List<GeneratedFile> files) {
        List<SecurityCheckResult> results = new ArrayList<>();

        for (GeneratedFile file : files) {
            for (SecurityRule rule : getSecurityRules()) {
                SecurityCheckResult result = rule.check(file);
                if (!result.isPassed()) {
                    results.add(result);
                }
            }
        }

        return results;
    }

    // ========== 代码规范检查规则实现 ==========

    private StyleCheckResult checkNamingConvention(GeneratedFile file) {
        StyleCheckResult result = new StyleCheckResult();
        result.setRuleName("naming_convention");
        result.setFilePath(file.getPath());

        List<String> issues = new ArrayList<>();
        String content = file.getContent();
        String language = file.getLanguage();

        if ("java".equals(language)) {
            // 检查类名（应该使用大驼峰）
            Pattern classPattern = Pattern.compile("class\\s+([a-z][a-zA-Z0-9]*)");
            Matcher matcher = classPattern.matcher(content);
            while (matcher.find()) {
                issues.add("类名 '" + matcher.group(1) + "' 应该使用大驼峰命名");
            }

            // 检查常量（应该使用全大写下划线）
            Pattern constPattern = Pattern.compile("static\\s+final\\s+\\w+\\s+([a-z][a-zA-Z0-9]*)");
            matcher = constPattern.matcher(content);
            while (matcher.find()) {
                issues.add("常量 '" + matcher.group(1) + "' 应该使用全大写下划线命名");
            }
        }

        result.setPassed(issues.isEmpty());
        result.setIssues(issues);
        return result;
    }

    private StyleCheckResult checkLineLength(GeneratedFile file) {
        StyleCheckResult result = new StyleCheckResult();
        result.setRuleName("line_length");
        result.setFilePath(file.getPath());

        List<String> issues = new ArrayList<>();
        String[] lines = file.getContent().split("\n");

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].length() > 120) {
                issues.add("第 " + (i + 1) + " 行超过120字符限制 (" + lines[i].length() + " 字符)");
            }
        }

        result.setPassed(issues.isEmpty());
        result.setIssues(issues);
        return result;
    }

    private StyleCheckResult checkImportOrganization(GeneratedFile file) {
        StyleCheckResult result = new StyleCheckResult();
        result.setRuleName("import_organization");
        result.setFilePath(file.getPath());

        List<String> issues = new ArrayList<>();
        String content = file.getContent();

        // 检查是否有通配符导入
        if (content.contains("import .*\\*")) {
            issues.add("不应该使用通配符导入 (import xxx.*)");
        }

        // 检查是否有未使用的导入（简化检查）
        Pattern importPattern = Pattern.compile("import\\s+([\\w.]+);");
        Matcher matcher = importPattern.matcher(content);

        while (matcher.find()) {
            String importClass = matcher.group(1);
            String simpleName = importClass.substring(importClass.lastIndexOf('.') + 1);

            // 简单检查：如果类名在代码中没有出现（除了import语句）
            String codeWithoutImports = content.replaceAll("import\\s+[\\w.]+;", "");
            if (!codeWithoutImports.contains(simpleName)) {
                issues.add("可能未使用的导入: " + importClass);
            }
        }

        result.setPassed(issues.isEmpty());
        result.setIssues(issues);
        return result;
    }

    private StyleCheckResult checkMethodLength(GeneratedFile file) {
        StyleCheckResult result = new StyleCheckResult();
        result.setRuleName("method_length");
        result.setFilePath(file.getPath());

        List<String> issues = new ArrayList<>();
        String content = file.getContent();

        // 简单的方法长度检查（按大括号匹配）
        Pattern methodPattern = Pattern.compile(
                "(public|private|protected)\\s+[\\w<>\\[\\]]+\\s+(\\w+)\\s*\\([^)]*\\)\\s*\\{"
        );
        Matcher matcher = methodPattern.matcher(content);

        while (matcher.find()) {
            String methodName = matcher.group(2);
            int start = matcher.end() - 1;
            int end = findMatchingBrace(content, start);

            if (end > start) {
                String methodBody = content.substring(start, end);
                int lineCount = methodBody.split("\n").length;

                if (lineCount > 50) {
                    issues.add("方法 '" + methodName + "' 过长 (" + lineCount + " 行)，建议重构");
                }
            }
        }

        result.setPassed(issues.isEmpty());
        result.setIssues(issues);
        return result;
    }

    private StyleCheckResult checkCommentRatio(GeneratedFile file) {
        StyleCheckResult result = new StyleCheckResult();
        result.setRuleName("comment_ratio");
        result.setFilePath(file.getPath());

        List<String> issues = new ArrayList<>();
        String content = file.getContent();

        // 计算代码行数和注释行数
        String[] lines = content.split("\n");
        int totalLines = 0;
        int commentLines = 0;
        boolean inBlockComment = false;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            totalLines++;

            if (inBlockComment) {
                commentLines++;
                if (trimmed.endsWith("*/")) {
                    inBlockComment = false;
                }
            } else if (trimmed.startsWith("//")) {
                commentLines++;
            } else if (trimmed.startsWith("/*")) {
                commentLines++;
                if (!trimmed.endsWith("*/")) {
                    inBlockComment = true;
                }
            }
        }

        if (totalLines > 0) {
            double ratio = (double) commentLines / totalLines;
            if (ratio < 0.1 && totalLines > 20) {
                issues.add(String.format("注释比例过低 (%.1f%%)，建议添加更多注释", ratio * 100));
            }
        }

        result.setPassed(issues.isEmpty());
        result.setIssues(issues);
        return result;
    }

    // ========== 安全检查规则实现 ==========

    private SecurityCheckResult checkSqlInjection(GeneratedFile file) {
        SecurityCheckResult result = new SecurityCheckResult();
        result.setRuleName("sql_injection");
        result.setFilePath(file.getPath());
        result.setSeverity("high");

        List<String> issues = new ArrayList<>();
        String content = file.getContent().toLowerCase();

        // 检查SQL拼接
        Pattern[] dangerousPatterns = {
                Pattern.compile("select\\s+.*from\\s+.*\\+"),
                Pattern.compile("insert\\s+into\\s+.*\\+"),
                Pattern.compile("update\\s+.*set\\s+.*\\+"),
                Pattern.compile("delete\\s+from\\s+.*\\+"),
                Pattern.compile("statement\\.execute\\s*\\(.*\\+"),
                Pattern.compile("createquery\\s*\\(.*\\+.*\\)")
        };

        for (Pattern pattern : dangerousPatterns) {
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                issues.add("发现潜在的SQL注入风险: " + matcher.group());
            }
        }

        result.setPassed(issues.isEmpty());
        result.setIssues(issues);
        return result;
    }

    private SecurityCheckResult checkXss(GeneratedFile file) {
        SecurityCheckResult result = new SecurityCheckResult();
        result.setRuleName("xss");
        result.setFilePath(file.getPath());
        result.setSeverity("high");

        List<String> issues = new ArrayList<>();
        String content = file.getContent();

        // 检查直接输出用户输入
        Pattern[] xssPatterns = {
                Pattern.compile("innerHTML\\s*=\\s*.*user|input|param"),
                Pattern.compile("document\\.write\\s*\\(.*user|input|param"),
                Pattern.compile("\\.html\\s*\\(.*user|input|param")
        };

        for (Pattern pattern : xssPatterns) {
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                issues.add("发现潜在的XSS风险: " + matcher.group());
            }
        }

        result.setPassed(issues.isEmpty());
        result.setIssues(issues);
        return result;
    }

    private SecurityCheckResult checkHardcodedSecrets(GeneratedFile file) {
        SecurityCheckResult result = new SecurityCheckResult();
        result.setRuleName("hardcoded_secret");
        result.setFilePath(file.getPath());
        result.setSeverity("critical");

        List<String> issues = new ArrayList<>();
        String content = file.getContent();

        // 检查硬编码密钥
        Pattern[] secretPatterns = {
                Pattern.compile("(password|passwd|pwd)\\s*=\\s*[\"'][^\"']{8,}[\"']", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(api[_-]?key|apikey)\\s*=\\s*[\"'][^\"']{10,}[\"']", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(secret[_-]?key|secretkey)\\s*=\\s*[\"'][^\"']{10,}[\"']", Pattern.CASE_INSENSITIVE),
                Pattern.compile("(access[_-]?token|accesstoken)\\s*=\\s*[\"'][^\"']{10,}[\"']", Pattern.CASE_INSENSITIVE),
                Pattern.compile("-----BEGIN\\s+(RSA\\s+)?PRIVATE\\s+KEY-----")
        };

        for (Pattern pattern : secretPatterns) {
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                issues.add("发现硬编码密钥/密码: " + matcher.group().substring(0, Math.min(50, matcher.group().length())));
            }
        }

        result.setPassed(issues.isEmpty());
        result.setIssues(issues);
        return result;
    }

    private SecurityCheckResult checkUnsafeDeserialization(GeneratedFile file) {
        SecurityCheckResult result = new SecurityCheckResult();
        result.setRuleName("unsafe_deserialization");
        result.setFilePath(file.getPath());
        result.setSeverity("high");

        List<String> issues = new ArrayList<>();
        String content = file.getContent();

        // 检查Java反序列化
        if (content.contains("ObjectInputStream") && content.contains("readObject")) {
            issues.add("发现不安全的反序列化操作，请确保对输入进行验证");
        }

        // 检查Python反序列化
        if (content.contains("pickle.loads") || content.contains("yaml.load")) {
            issues.add("发现潜在的不安全反序列化操作");
        }

        result.setPassed(issues.isEmpty());
        result.setIssues(issues);
        return result;
    }

    private SecurityCheckResult checkPathTraversal(GeneratedFile file) {
        SecurityCheckResult result = new SecurityCheckResult();
        result.setRuleName("path_traversal");
        result.setFilePath(file.getPath());
        result.setSeverity("high");

        List<String> issues = new ArrayList<>();
        String content = file.getContent();

        // 检查路径遍历
        Pattern[] pathPatterns = {
                Pattern.compile("new\\s+File\\s*\\([^)]*\\+"),
                Pattern.compile("FileInputStream\\s*\\([^)]*\\+"),
                Pattern.compile("FileOutputStream\\s*\\([^)]*\\+"),
                Pattern.compile("Paths\\.get\\s*\\([^)]*\\+")
        };

        for (Pattern pattern : pathPatterns) {
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                issues.add("发现潜在的路径遍历风险: " + matcher.group());
            }
        }

        result.setPassed(issues.isEmpty());
        result.setIssues(issues);
        return result;
    }

    // ========== 工具方法 ==========

    private void writeFilesToTemp(String tempPath, List<GeneratedFile> files) throws Exception {
        for (GeneratedFile file : files) {
            Path filePath = Paths.get(tempPath, file.getPath());
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, file.getContent());
        }
    }

    private List<CompilationError> parseMavenErrors(Process process, List<GeneratedFile> files) throws Exception {
        List<CompilationError> errors = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            // 解析Maven编译错误格式
            if (line.contains("error:") || line.contains("ERROR")) {
                CompilationError error = parseErrorLine(line, files);
                if (error != null) {
                    errors.add(error);
                }
            }
        }

        return errors;
    }

    private CompilationError parseErrorLine(String line, List<GeneratedFile> files) {
        // 简单解析，实际项目中可能需要更复杂的解析逻辑
        for (GeneratedFile file : files) {
            if (line.contains(file.getPath())) {
                return CompilationError.builder()
                        .filePath(file.getPath())
                        .message(line)
                        .severity("error")
                        .build();
            }
        }
        return null;
    }

    private int findMatchingBrace(String content, int start) {
        int count = 1;
        int i = start + 1;

        while (i < content.length() && count > 0) {
            char c = content.charAt(i);
            if (c == '{') count++;
            else if (c == '}') count--;
            i++;
        }

        return count == 0 ? i : -1;
    }

    private void deleteDirectory(Path path) throws Exception {
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

    private String buildOverallMessage(boolean stylePassed, boolean securityPassed,
                                        List<StyleCheckResult> styleResults,
                                        List<SecurityCheckResult> securityResults) {
        StringBuilder message = new StringBuilder();

        if (stylePassed && securityPassed) {
            message.append("所有质量检查通过");
        } else {
            if (!stylePassed) {
                long styleIssues = styleResults.stream()
                        .flatMap(r -> r.getIssues().stream())
                        .count();
                message.append("代码规范检查发现 ").append(styleIssues).append(" 个问题; ");
            }
            if (!securityPassed) {
                long securityIssues = securityResults.stream()
                        .flatMap(r -> r.getIssues().stream())
                        .count();
                message.append("安全检查发现 ").append(securityIssues).append(" 个问题");
            }
        }

        return message.toString();
    }

    // ========== 内部类定义 ==========

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class QualityCheckResult {
        private boolean passed;
        private String overallMessage;
        private CompilationCheckResult compilationCheck;
        private List<StyleCheckResult> styleChecks;
        private List<SecurityCheckResult> securityChecks;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CompilationCheckResult {
        private boolean passed;
        private List<CompilationError> errors;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class StyleCheckResult {
        private String ruleName;
        private String filePath;
        private boolean passed;
        private List<String> issues;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SecurityCheckResult {
        private String ruleName;
        private String filePath;
        private boolean passed;
        private String severity;
        private List<String> issues;
    }

    @FunctionalInterface
    private interface StyleRuleChecker {
        StyleCheckResult check(GeneratedFile file);
    }

    @FunctionalInterface
    private interface SecurityRuleChecker {
        SecurityCheckResult check(GeneratedFile file);
    }

    @lombok.AllArgsConstructor
    private static class CodeStyleRule {
        private final String name;
        private final String description;
        private final StyleRuleChecker checker;

        public StyleCheckResult check(GeneratedFile file) {
            return checker.check(file);
        }
    }

    @lombok.AllArgsConstructor
    private static class SecurityRule {
        private final String name;
        private final String description;
        private final SecurityRuleChecker checker;

        public SecurityCheckResult check(GeneratedFile file) {
            return checker.check(file);
        }
    }
}
