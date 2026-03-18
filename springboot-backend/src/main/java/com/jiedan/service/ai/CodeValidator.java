package com.jiedan.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 【步骤3】代码验证器
 * 对AI生成的代码进行基础语法和结构验证
 */
@Slf4j
@Component
public class CodeValidator {

    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private boolean valid;
        private List<String> errors;
        private List<String> warnings;
        private int score; // 0-100

        public ValidationResult() {
            this.valid = true;
            this.errors = new ArrayList<>();
            this.warnings = new ArrayList<>();
            this.score = 100;
        }

        public void addError(String error) {
            this.errors.add(error);
            this.valid = false;
            this.score -= 10; // 每个错误扣10分
        }

        public void addWarning(String warning) {
            this.warnings.add(warning);
            this.score -= 2; // 每个警告扣2分
        }

        // Getters
        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        public int getScore() { return Math.max(0, score); }
    }

    /**
     * 验证项目代码
     * @param files 文件路径到内容的映射
     * @return 验证结果
     */
    public ValidationResult validateProject(Map<String, String> files) {
        ValidationResult result = new ValidationResult();

        if (files == null || files.isEmpty()) {
            result.addError("没有生成任何文件");
            return result;
        }

        log.info("开始验证项目代码，共 {} 个文件", files.size());

        // 1. 检查必需文件
        checkRequiredFiles(files, result);

        // 2. 验证每个文件
        for (Map.Entry<String, String> entry : files.entrySet()) {
            String filePath = entry.getKey();
            String content = entry.getValue();

            validateFile(filePath, content, result);
        }

        // 3. 检查文件间一致性
        checkCrossFileConsistency(files, result);

        log.info("代码验证完成，得分: {}, 错误: {}, 警告: {}",
                result.getScore(), result.getErrors().size(), result.getWarnings().size());

        return result;
    }

    /**
     * 检查必需文件
     */
    private void checkRequiredFiles(Map<String, String> files, ValidationResult result) {
        boolean hasJavaFile = files.keySet().stream().anyMatch(f -> f.endsWith(".java"));
        boolean hasPomOrGradle = files.keySet().stream()
                .anyMatch(f -> f.endsWith("pom.xml") || f.endsWith("build.gradle"));

        if (!hasJavaFile) {
            result.addWarning("未找到Java文件，可能缺少后端代码");
        }

        if (!hasPomOrGradle) {
            result.addWarning("未找到pom.xml或build.gradle，可能缺少构建配置");
        }

        // 检查是否有实体类
        boolean hasEntity = files.keySet().stream()
                .anyMatch(f -> f.contains("/entity/") && f.endsWith(".java"));
        if (!hasEntity) {
            result.addWarning("未找到实体类（Entity），可能缺少数据模型");
        }
    }

    /**
     * 验证单个文件
     */
    private void validateFile(String filePath, String content, ValidationResult result) {
        if (content == null || content.trim().isEmpty()) {
            result.addError("文件内容为空: " + filePath);
            return;
        }

        // 根据文件类型进行验证
        if (filePath.endsWith(".java")) {
            validateJavaFile(filePath, content, result);
        } else if (filePath.endsWith(".xml")) {
            validateXmlFile(filePath, content, result);
        } else if (filePath.endsWith(".yml") || filePath.endsWith(".yaml")) {
            validateYamlFile(filePath, content, result);
        } else if (filePath.endsWith(".vue")) {
            validateVueFile(filePath, content, result);
        } else if (filePath.endsWith(".js")) {
            validateJsFile(filePath, content, result);
        }
    }

    /**
     * 验证Java文件
     */
    private void validateJavaFile(String filePath, String content, ValidationResult result) {
        // 1. 检查基本结构
        if (!content.contains("class ") && !content.contains("interface ") && !content.contains("enum ")) {
            result.addError("Java文件缺少类/接口/枚举定义: " + filePath);
        }

        // 2. 检查括号匹配
        int openBraces = countOccurrences(content, '{');
        int closeBraces = countOccurrences(content, '}');
        if (openBraces != closeBraces) {
            result.addError("Java文件括号不匹配: " + filePath + " (左:" + openBraces + ", 右:" + closeBraces + ")");
        }

        int openParens = countOccurrences(content, '(');
        int closeParens = countOccurrences(content, ')');
        if (openParens != closeParens) {
            result.addError("Java文件圆括号不匹配: " + filePath + " (左:" + openParens + ", 右:" + closeParens + ")");
        }

        // 3. 检查包声明
        if (filePath.contains("/java/")) {
            String expectedPackage = extractPackageFromPath(filePath);
            if (!content.contains("package " + expectedPackage)) {
                result.addWarning("Java文件包声明可能与路径不匹配: " + filePath);
            }
        }

        // 4. 检查导入语句
        if (content.contains("import ")) {
            // 检查是否有未使用的导入（简单检查）
            Pattern importPattern = Pattern.compile("import ([\\w.]+);");
            Matcher matcher = importPattern.matcher(content);
            while (matcher.find()) {
                String importClass = matcher.group(1);
                String className = importClass.substring(importClass.lastIndexOf('.') + 1);
                // 简单检查：如果类名在代码中没有出现（除了import语句），可能是未使用的导入
                String contentWithoutImports = content.replaceAll("import [\\w.]+;", "");
                if (!contentWithoutImports.contains(className)) {
                    result.addWarning("可能有未使用的导入: " + importClass + " in " + filePath);
                }
            }
        }

        // 5. 检查类名与文件名匹配
        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1).replace(".java", "");
        Pattern classPattern = Pattern.compile("(class|interface|enum)\\s+(\\w+)");
        Matcher classMatcher = classPattern.matcher(content);
        boolean foundClass = false;
        while (classMatcher.find()) {
            foundClass = true;
            String className = classMatcher.group(2);
            if (className.equals(fileName)) {
                return; // 找到匹配的类名
            }
        }
        if (foundClass) {
            result.addWarning("类名与文件名可能不匹配: " + filePath);
        }
    }

    /**
     * 验证XML文件
     */
    private void validateXmlFile(String filePath, String content, ValidationResult result) {
        // 简单检查XML标签匹配
        int openTags = countOccurrences(content, '<');
        int closeTags = countOccurrences(content, '>') + countOccurrences(content, "/>");
        if (openTags != closeTags) {
            result.addWarning("XML标签可能不匹配: " + filePath);
        }

        // 检查是否有根元素
        if (!content.trim().startsWith("<?xml") && !content.trim().startsWith("<")) {
            result.addError("XML文件缺少根元素: " + filePath);
        }
    }

    /**
     * 验证YAML文件
     */
    private void validateYamlFile(String filePath, String content, ValidationResult result) {
        // 简单检查YAML格式
        String[] lines = content.split("\n");
        int indentLevel = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                continue;
            }

            int currentIndent = getIndentLevel(line);
            if (currentIndent % 2 != 0) {
                result.addWarning("YAML文件缩进可能不规范（应为2的倍数）: " + filePath + " 第" + (i+1) + "行");
            }

            // 检查键值对格式
            if (line.contains(":") && !line.contains(": ") && !line.trim().endsWith(":")) {
                result.addWarning("YAML文件键值对格式可能不正确: " + filePath + " 第" + (i+1) + "行");
            }
        }
    }

    /**
     * 验证Vue文件
     */
    private void validateVueFile(String filePath, String content, ValidationResult result) {
        // 检查Vue文件基本结构
        if (!content.contains("<template>")) {
            result.addWarning("Vue文件缺少template标签: " + filePath);
        }
        if (!content.contains("<script>")) {
            result.addWarning("Vue文件缺少script标签: " + filePath);
        }

        // 检查template标签匹配
        int templateOpen = countOccurrences(content, "<template>");
        int templateClose = countOccurrences(content, "</template>");
        if (templateOpen != templateClose) {
            result.addError("Vue文件template标签不匹配: " + filePath);
        }
    }

    /**
     * 验证JS文件
     */
    private void validateJsFile(String filePath, String content, ValidationResult result) {
        // 检查括号匹配
        int openBraces = countOccurrences(content, '{');
        int closeBraces = countOccurrences(content, '}');
        if (openBraces != closeBraces) {
            result.addWarning("JS文件括号可能不匹配: " + filePath);
        }
    }

    /**
     * 检查文件间一致性
     */
    private void checkCrossFileConsistency(Map<String, String> files, ValidationResult result) {
        // 1. 检查实体类与Mapper的对应关系
        Set<String> entities = new HashSet<>();
        Set<String> mappers = new HashSet<>();

        for (String filePath : files.keySet()) {
            if (filePath.contains("/entity/") && filePath.endsWith(".java")) {
                String entityName = filePath.substring(filePath.lastIndexOf('/') + 1).replace(".java", "");
                entities.add(entityName);
            } else if (filePath.contains("/mapper/") && filePath.endsWith(".java")) {
                String mapperName = filePath.substring(filePath.lastIndexOf('/') + 1).replace(".java", "");
                if (mapperName.endsWith("Mapper")) {
                    mappers.add(mapperName.replace("Mapper", ""));
                }
            }
        }

        // 检查是否有实体类没有对应的Mapper
        for (String entity : entities) {
            if (!mappers.contains(entity)) {
                result.addWarning("实体类 " + entity + " 可能缺少对应的Mapper");
            }
        }
    }

    /**
     * 从文件路径提取包名
     */
    private String extractPackageFromPath(String filePath) {
        // 从 /java/com/example/entity/User.java 提取 com.example.entity
        int javaIndex = filePath.indexOf("/java/");
        if (javaIndex == -1) return "";

        String packagePath = filePath.substring(javaIndex + 6);
        packagePath = packagePath.substring(0, packagePath.lastIndexOf('/'));
        return packagePath.replace('/', '.');
    }

    /**
     * 获取缩进级别
     */
    private int getIndentLevel(String line) {
        int level = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') {
                level++;
            } else if (c == '\t') {
                level += 2; // 一个tab算2个空格
            } else {
                break;
            }
        }
        return level;
    }

    /**
     * 统计字符出现次数
     */
    private int countOccurrences(String str, char target) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == target) {
                count++;
            }
        }
        return count;
    }

    /**
     * 统计字符串出现次数
     */
    private int countOccurrences(String str, String target) {
        int count = 0;
        int index = 0;
        while ((index = str.indexOf(target, index)) != -1) {
            count++;
            index += target.length();
        }
        return count;
    }
}
