package com.jiedan.service.ai;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

/**
 * 【步骤4】任务书解析服务
 * 解析Markdown格式的任务书，提取模块信息和生成顺序
 */
@Slf4j
@Component
public class TaskDocumentParser {

    /**
     * 模块信息
     */
    @Data
    public static class ModuleInfo {
        private String name;                    // 模块名称
        private int order;                      // 生成顺序
        private List<String> files;             // 文件清单
        private List<String> dependencies;      // 依赖模块
        private String type;                    // 类型：backend/frontend/database/config
        private String description;             // 模块描述
    }

    /**
     * 解析结果
     */
    @Data
    public static class ParseResult {
        private List<ModuleInfo> modules;       // 模块列表
        private Map<String, String> techStack;  // 技术栈信息
        private String projectType;             // 项目类型
        private boolean success;                // 是否解析成功
        private String errorMessage;            // 错误信息

        public ParseResult() {
            this.modules = new ArrayList<>();
            this.techStack = new HashMap<>();
            this.success = true;
        }
    }

    /**
     * 解析任务书
     * @param taskDoc Markdown格式的任务书
     * @return 解析结果
     */
    public ParseResult parse(String taskDoc) {
        return parse(taskDoc, null);
    }

    /**
     * 解析任务书（带项目ID，用于保存章节文件）
     * @param taskDoc Markdown格式的任务书
     * @param projectId 项目ID（可选，用于保存章节文件）
     * @return 解析结果
     */
    public ParseResult parse(String taskDoc, String projectId) {
        ParseResult result = new ParseResult();

        if (taskDoc == null || taskDoc.trim().isEmpty()) {
            result.setSuccess(false);
            result.setErrorMessage("任务书内容为空");
            return result;
        }

        log.info("开始解析任务书，长度: {} 字符", taskDoc.length());

        try {
            // 【方案C新增】分割并保存7个章节文件（不影响现有功能）
            if (projectId != null && !projectId.isEmpty()) {
                log.debug("步骤0: 分割保存章节文件");
                Map<String, String> chapters = splitIntoChapters(taskDoc);
                saveChaptersToFiles(projectId, chapters);
            }

            // 1. 提取技术栈信息
            log.debug("步骤1: 提取技术栈信息");
            extractTechStack(taskDoc, result);

            // 2. 提取后端模块
            log.debug("步骤2: 提取后端模块");
            extractBackendModules(taskDoc, result);

            // 3. 提取前端模块
            log.debug("步骤3: 提取前端模块");
            extractFrontendModules(taskDoc, result);

            // 4. 提取数据库模块
            log.debug("步骤4: 提取数据库模块");
            extractDatabaseModules(taskDoc, result);

            // 5. 提取配置模块
            log.debug("步骤5: 提取配置模块");
            extractConfigModules(taskDoc, result);

            // 6. 排序并设置依赖关系
            log.debug("步骤6: 排序模块，当前模块数: {}", result.getModules().size());
            sortModulesAndSetDependencies(result);

            // 7. 检查是否提取到模块
            if (result.getModules().isEmpty()) {
                log.warn("未从任务书中提取到任何模块，创建默认模块");
                createDefaultModules(result);
            }

            log.info("任务书解析完成，共 {} 个模块", result.getModules().size());

        } catch (Exception e) {
            log.error("解析任务书失败: {}", e.getMessage(), e);
            result.setSuccess(false);
            result.setErrorMessage("解析失败: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }

        return result;
    }

    /**
     * 【方案C新增】将任务书分割成7个章节
     * @param taskDoc 完整任务书内容
     * @return 章节名称到内容的映射
     */
    public Map<String, String> splitIntoChapters(String taskDoc) {
        Map<String, String> chapters = new LinkedHashMap<>();

        // 7个标准章节标题（支持多种格式）
        String[][] chapterPatterns = {
            {"1", "项目技术规格"},
            {"2", "前端页面开发清单"},
            {"3", "后端接口开发清单"},
            {"4", "数据库表结构设计"},
            {"5", "业务逻辑规则"},
            {"6", "开发执行顺序"},
            {"7", "代码生成规范"}
        };

        // 查找每个章节的起始位置
        List<ChapterPosition> positions = new ArrayList<>();

        for (String[] chapter : chapterPatterns) {
            String number = chapter[0];
            String title = chapter[1];

            // 构建宽松的正则：支持 ## 1. 标题 或 ## 1.标题 或 ### 1. 标题 等
            String regex = "#{1,3}\\s*" + number + "\\.?\\s*" + title;
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(taskDoc);

            if (matcher.find()) {
                positions.add(new ChapterPosition(number, title, matcher.start(), matcher.end()));
                log.debug("找到章节 [{}] 在位置 {}", title, matcher.start());
            } else {
                log.warn("未找到章节: {}. {}", number, title);
            }
        }

        // 按位置排序
        positions.sort(Comparator.comparingInt(ChapterPosition::getStart));

        // 分割章节内容
        for (int i = 0; i < positions.size(); i++) {
            ChapterPosition current = positions.get(i);
            int contentStart = current.getContentStart();
            int contentEnd;

            if (i < positions.size() - 1) {
                // 到下一个章节的开始
                contentEnd = positions.get(i + 1).getStart();
            } else {
                // 最后一个章节到文档末尾
                contentEnd = taskDoc.length();
            }

            String chapterContent = taskDoc.substring(contentStart, contentEnd).trim();
            chapters.put(current.getNumber() + "-" + current.getTitle(), chapterContent);
            log.debug("章节 [{}] 内容长度: {} 字符", current.getTitle(), chapterContent.length());
        }

        log.info("成功分割出 {} 个章节", chapters.size());
        return chapters;
    }

    /**
     * 【方案C新增】保存章节到文件
     * @param projectId 项目ID
     * @param chapters 章节内容映射
     */
    private void saveChaptersToFiles(String projectId, Map<String, String> chapters) {
        if (chapters.isEmpty()) {
            return;
        }

        try {
            // 创建章节目录: projects/{projectId}/chapters/
            String projectPath = "projects/" + projectId;
            Path chaptersDir = Paths.get(projectPath, "chapters");
            Files.createDirectories(chaptersDir);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

            for (Map.Entry<String, String> entry : chapters.entrySet()) {
                String chapterKey = entry.getKey(); // 如 "1-项目技术规格"
                String chapterContent = entry.getValue();

                // 文件名: 1-项目技术规格.md
                String fileName = chapterKey + ".md";
                Path filePath = chaptersDir.resolve(fileName);

                // 写入文件（添加标题）
                String fullContent = "## " + chapterKey.replace("-", ". ") + "\n\n" + chapterContent;
                Files.writeString(filePath, fullContent);

                log.debug("章节文件已保存: {}", filePath);
            }

            log.info("已保存 {} 个章节文件到 {}", chapters.size(), chaptersDir);

        } catch (IOException e) {
            log.error("保存章节文件失败: {}", e.getMessage(), e);
            // 不影响主流程，仅记录错误
        }
    }

    /**
     * 【方案C新增】章节位置内部类
     */
    @Data
    private static class ChapterPosition {
        private final String number;
        private final String title;
        private final int start;
        private final int contentStart;

        public ChapterPosition(String number, String title, int start, int contentStart) {
            this.number = number;
            this.title = title;
            this.start = start;
            this.contentStart = contentStart;
        }
    }

    /**
     * 提取技术栈信息
     */
    private void extractTechStack(String taskDoc, ParseResult result) {
        // 提取技术栈部分
        Pattern techStackPattern = Pattern.compile(
                "##?\\s*1\\.\\s*项目技术规格.*?(?=##?\\s*2\\.|$)",
                Pattern.DOTALL);
        Matcher matcher = techStackPattern.matcher(taskDoc);

        if (matcher.find()) {
            String techSection = matcher.group();

            // 提取前端技术栈
            if (techSection.contains("微信小程序")) {
                result.getTechStack().put("frontend", "miniprogram");
            } else if (techSection.contains("Vue") || techSection.contains("vue")) {
                result.getTechStack().put("frontend", "vue");
            } else {
                result.getTechStack().put("frontend", "none");
            }

            // 提取后端技术栈
            if (techSection.contains("Spring Boot") || techSection.contains("springboot")) {
                result.getTechStack().put("backend", "springboot");
            } else {
                result.getTechStack().put("backend", "unknown");
            }

            // 提取数据库
            if (techSection.contains("MySQL") || techSection.contains("mysql")) {
                result.getTechStack().put("database", "mysql");
            } else {
                result.getTechStack().put("database", "unknown");
            }
        }

        // 确定项目类型
        if ("miniprogram".equals(result.getTechStack().get("frontend")) &&
            "springboot".equals(result.getTechStack().get("backend"))) {
            result.setProjectType("miniprogram+springboot");
        } else if ("vue".equals(result.getTechStack().get("frontend")) &&
                   "springboot".equals(result.getTechStack().get("backend"))) {
            result.setProjectType("vue+springboot");
        } else if ("springboot".equals(result.getTechStack().get("backend"))) {
            result.setProjectType("springboot-only");
        } else {
            result.setProjectType("unknown");
        }
    }

    /**
     * 提取后端模块
     */
    private void extractBackendModules(String taskDoc, ParseResult result) {
        // 尝试多种可能的后端部分标题
        String[] backendPatterns = {
            "##?\\s*3\\.\\s*后端接口开发清单.*?(?=##?\\s*4\\.|$)",
            "##?\\s*后端.*?(?=##?\\s*\\d+\\.|##?\\s*数据库|##?\\s*前端|$)",
            "##?\\s*API.*?(?=##?\\s*\\d+\\.|##?\\s*数据库|##?\\s*前端|$)"
        };

        String backendSection = null;
        for (String pattern : backendPatterns) {
            Pattern backendPattern = Pattern.compile(pattern, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher matcher = backendPattern.matcher(taskDoc);
            if (matcher.find()) {
                backendSection = matcher.group();
                break;
            }
        }

        if (backendSection == null) {
            log.warn("未找到后端接口开发清单，尝试从整个文档提取后端相关内容");
            // 如果没有明确的后端部分，尝试从整个文档提取Controller/Service等关键词
            extractBackendModulesFromContent(taskDoc, result);
            return;
        }

        // 按模块分割（### 开头）
        Pattern modulePattern = Pattern.compile(
                "###\\s*(\\d+\\.\\d+)\\s*(.+?)(?=###\\s*\\d+\\.\\d+|##?\\s*\\d+\\.|$)",
                Pattern.DOTALL);
        Matcher moduleMatcher = modulePattern.matcher(backendSection);

        int order = 10; // 后端模块从10开始
        int count = 0;
        while (moduleMatcher.find()) {
            String moduleNumber = moduleMatcher.group(1);
            String moduleContent = moduleMatcher.group(2);

            ModuleInfo module = new ModuleInfo();
            module.setName(extractModuleName(moduleContent));
            module.setDescription(extractDescription(moduleContent));
            module.setFiles(extractFiles(moduleContent, "backend"));
            module.setType("backend");
            module.setOrder(order++);
            module.setDependencies(new ArrayList<>());

            // 推断依赖（第一个模块无依赖，后续模块依赖前面的模块）
            if (order > 11) {
                module.getDependencies().add("entity-module");
            }

            result.getModules().add(module);
            log.debug("提取后端模块: {}", module.getName());
            count++;
        }

        // 如果没有提取到模块，尝试备用方法
        if (count == 0) {
            extractBackendModulesFromContent(backendSection, result);
        }
    }

    /**
     * 从内容中提取后端模块（备用方法）
     */
    private void extractBackendModulesFromContent(String content, ParseResult result) {
        // 查找Controller、Service等关键词
        Pattern controllerPattern = Pattern.compile(
            "([A-Z][a-zA-Z]*)Controller",
            Pattern.CASE_INSENSITIVE);
        Matcher matcher = controllerPattern.matcher(content);

        int order = 10;
        Set<String> addedControllers = new HashSet<>();

        while (matcher.find()) {
            String controllerName = matcher.group(1);
            if (addedControllers.contains(controllerName)) {
                continue;
            }
            addedControllers.add(controllerName);

            ModuleInfo module = new ModuleInfo();
            module.setName(controllerName + "模块");
            module.setDescription(controllerName + "相关的Controller、Service、Mapper");
            module.setFiles(Arrays.asList(
                "src/main/java/com/jiedan/controller/" + controllerName + "Controller.java",
                "src/main/java/com/jiedan/service/" + controllerName + "Service.java",
                "src/main/java/com/jiedan/mapper/" + controllerName + "Mapper.java"
            ));
            module.setType("backend");
            module.setOrder(order++);
            module.setDependencies(Arrays.asList("database"));

            result.getModules().add(module);
            log.debug("从内容提取后端模块: {}", module.getName());
        }

        // 如果没有找到Controller，创建一个通用的后端模块
        if (addedControllers.isEmpty()) {
            ModuleInfo module = new ModuleInfo();
            module.setName("后端核心模块");
            module.setDescription("包含Controller、Service、Mapper的后端核心代码");
            module.setFiles(Arrays.asList(
                "src/main/java/com/jiedan/controller/",
                "src/main/java/com/jiedan/service/",
                "src/main/java/com/jiedan/mapper/"
            ));
            module.setType("backend");
            module.setOrder(order);
            module.setDependencies(Arrays.asList("database"));
            result.getModules().add(module);
            log.debug("创建通用后端模块");
        }
    }

    /**
     * 提取前端模块
     */
    private void extractFrontendModules(String taskDoc, ParseResult result) {
        // 尝试多种可能的前端部分标题
        String[] frontendPatterns = {
            "##?\\s*2\\.\\s*前端页面开发清单.*?(?=##?\\s*3\\.|$)",
            "##?\\s*前端.*?(?=##?\\s*\\d+\\.|##?\\s*后端|##?\\s*数据库|$)",
            "##?\\s*页面.*?(?=##?\\s*\\d+\\.|##?\\s*后端|##?\\s*数据库|$)"
        };

        String frontendSection = null;
        for (String pattern : frontendPatterns) {
            Pattern frontendPattern = Pattern.compile(pattern, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher matcher = frontendPattern.matcher(taskDoc);
            if (matcher.find()) {
                frontendSection = matcher.group();
                break;
            }
        }

        if (frontendSection == null) {
            log.warn("未找到前端页面开发清单，尝试从项目结构提取");
            extractFrontendModulesFromProjectStructure(taskDoc, result);
            return;
        }

        // 按模块分割
        Pattern modulePattern = Pattern.compile(
                "###\\s*(\\d+\\.\\d+)\\s*(.+?)(?=###\\s*\\d+\\.\\d+|##?\\s*\\d+\\.|$)",
                Pattern.DOTALL);
        Matcher moduleMatcher = modulePattern.matcher(frontendSection);

        int order = 30; // 前端模块从30开始（在数据库和后端之后）
        int count = 0;
        while (moduleMatcher.find()) {
            String moduleContent = moduleMatcher.group(2);

            ModuleInfo module = new ModuleInfo();
            module.setName(extractModuleName(moduleContent));
            module.setDescription(extractDescription(moduleContent));
            module.setFiles(extractFiles(moduleContent, "frontend"));
            module.setType("frontend");
            module.setOrder(order++);
            module.setDependencies(Arrays.asList("backend-modules"));

            result.getModules().add(module);
            log.debug("提取前端模块: {}", module.getName());
            count++;
        }

        // 如果没有提取到模块，尝试备用方法
        if (count == 0) {
            extractFrontendModulesFromProjectStructure(frontendSection, result);
        }
    }

    /**
     * 从项目结构提取前端模块（备用方法）
     */
    private void extractFrontendModulesFromProjectStructure(String content, ParseResult result) {
        // 查找pages目录下的页面
        Pattern pagePattern = Pattern.compile(
            "pages/(\\w+)(?:/(\\w+))?",
            Pattern.CASE_INSENSITIVE);
        Matcher matcher = pagePattern.matcher(content);

        int order = 30;
        Set<String> addedPages = new HashSet<>();

        while (matcher.find()) {
            String pageName = matcher.group(1);
            String subPage = matcher.group(2);

            String fullPageName = subPage != null ? pageName + "_" + subPage : pageName;
            if (addedPages.contains(fullPageName)) {
                continue;
            }
            addedPages.add(fullPageName);

            ModuleInfo module = new ModuleInfo();
            module.setName(fullPageName + "页面");
            module.setDescription(fullPageName + "前端页面及相关组件");
            module.setFiles(Arrays.asList(
                "pages/" + pageName + "/" + (subPage != null ? subPage : pageName) + ".wxml",
                "pages/" + pageName + "/" + (subPage != null ? subPage : pageName) + ".js",
                "pages/" + pageName + "/" + (subPage != null ? subPage : pageName) + ".wxss"
            ));
            module.setType("frontend");
            module.setOrder(order++);
            module.setDependencies(Arrays.asList("backend-modules"));

            result.getModules().add(module);
            log.debug("从项目结构提取前端模块: {}", module.getName());
        }

        // 如果没有找到页面，创建一个通用的前端模块
        if (addedPages.isEmpty()) {
            ModuleInfo module = new ModuleInfo();
            module.setName("前端页面模块");
            module.setDescription("包含所有前端页面和组件");
            module.setFiles(Arrays.asList(
                "pages/",
                "components/",
                "utils/"
            ));
            module.setType("frontend");
            module.setOrder(order);
            module.setDependencies(Arrays.asList("backend-modules"));
            result.getModules().add(module);
            log.debug("创建通用前端模块");
        }
    }

    /**
     * 提取数据库模块
     */
    private void extractDatabaseModules(String taskDoc, ParseResult result) {
        // 尝试多种可能的数据库部分标题
        String[] dbPatterns = {
            "##?\\s*4\\.\\s*数据库.*?(?=##?\\s*5\\.|$)",
            "##?\\s*数据库.*?(?=##?\\s*\\d+\\.|##?\\s*后端|##?\\s*前端|$)",
            "##?\\s*表结构.*?(?=##?\\s*\\d+\\.|##?\\s*后端|##?\\s*前端|$)"
        };

        String dbSection = null;
        for (String pattern : dbPatterns) {
            Pattern dbPattern = Pattern.compile(pattern, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher matcher = dbPattern.matcher(taskDoc);
            if (matcher.find()) {
                dbSection = matcher.group();
                break;
            }
        }

        // 创建数据库模块（优先级最高）
        ModuleInfo module = new ModuleInfo();
        module.setName("数据库和实体类");
        module.setDescription("数据库表结构和Java实体类");

        if (dbSection != null) {
            module.setFiles(extractFiles(dbSection, "database"));
        } else {
            // 如果没有找到数据库部分，创建默认的数据库模块
            module.setFiles(Arrays.asList(
                "src/main/java/com/jiedan/entity/",
                "sql/"
            ));
            log.warn("未找到数据库设计部分，创建默认数据库模块");
        }

        module.setType("database");
        module.setOrder(1); // 数据库最先生成
        module.setDependencies(new ArrayList<>());

        result.getModules().add(module);
        log.debug("提取数据库模块");
    }

    /**
     * 提取配置模块
     */
    private void extractConfigModules(String taskDoc, ParseResult result) {
        // 查找配置部分
        Pattern configPattern = Pattern.compile(
                "##?\\s*1\\.\\s*项目技术规格.*?(?=##?\\s*2\\.|$)",
                Pattern.DOTALL);
        Matcher matcher = configPattern.matcher(taskDoc);

        if (!matcher.find()) {
            return;
        }

        String configSection = matcher.group();

        // 创建配置模块
        ModuleInfo module = new ModuleInfo();
        module.setName("项目配置");
        module.setDescription("pom.xml、application.yml等配置文件");
        module.setFiles(Arrays.asList(
                "pom.xml",
                "src/main/resources/application.yml",
                "src/main/resources/application-dev.yml"
        ));
        module.setType("config");
        module.setOrder(5); // 配置在数据库之后
        module.setDependencies(Arrays.asList("database"));

        result.getModules().add(module);
        log.debug("提取配置模块");
    }

    /**
     * 排序模块并设置依赖关系
     */
    private void sortModulesAndSetDependencies(ParseResult result) {
        // 按order排序
        result.getModules().sort(Comparator.comparingInt(ModuleInfo::getOrder));

        // 设置模块间的依赖关系
        for (int i = 0; i < result.getModules().size(); i++) {
            ModuleInfo current = result.getModules().get(i);

            // 数据库模块无依赖
            if ("database".equals(current.getType())) {
                continue;
            }

            // 配置模块依赖数据库
            if ("config".equals(current.getType())) {
                current.getDependencies().add("database");
                continue;
            }

            // 后端模块依赖数据库和配置
            if ("backend".equals(current.getType())) {
                if (!current.getDependencies().contains("database")) {
                    current.getDependencies().add("database");
                }
            }

            // 前端模块依赖后端
            if ("frontend".equals(current.getType())) {
                // 添加所有后端模块为依赖
                for (ModuleInfo module : result.getModules()) {
                    if ("backend".equals(module.getType()) &&
                        !current.getDependencies().contains(module.getName())) {
                        current.getDependencies().add(module.getName());
                    }
                }
            }
        }
    }

    /**
     * 提取模块名称
     */
    private String extractModuleName(String content) {
        // 第一行通常是模块名称
        String firstLine = content.split("\n")[0].trim();
        // 去掉Markdown标记
        firstLine = firstLine.replaceAll("^#+\\s*", "");
        firstLine = firstLine.replaceAll("\\*\\*", "");
        return firstLine.length() > 50 ? firstLine.substring(0, 50) : firstLine;
    }

    /**
     * 提取模块描述
     */
    private String extractDescription(String content) {
        // 查找功能描述部分
        Pattern descPattern = Pattern.compile(
                "-?\\s*\\*?功能描述\\*?[:：]\\s*(.+?)(?=\\n\\s*-|\\n\\s*\\*|$)",
                Pattern.DOTALL);
        Matcher matcher = descPattern.matcher(content);

        if (matcher.find()) {
            String desc = matcher.group(1).trim();
            return desc.length() > 200 ? desc.substring(0, 200) + "..." : desc;
        }

        // 如果没有明确的功能描述，取前100字符
        String firstParagraph = content.replaceAll("\\s+", " ").trim();
        return firstParagraph.length() > 100 ?
                firstParagraph.substring(0, 100) + "..." : firstParagraph;
    }

    /**
     * 提取文件清单
     */
    private List<String> extractFiles(String content, String type) {
        List<String> files = new ArrayList<>();

        if ("database".equals(type)) {
            // 数据库模块：提取SQL建表语句和实体类
            Pattern entityPattern = Pattern.compile(
                    "###\\s*([\\w]+)表.*?```sql(.+?)```",
                    Pattern.DOTALL);
            Matcher matcher = entityPattern.matcher(content);
            while (matcher.find()) {
                String tableName = matcher.group(1);
                files.add("src/main/java/com/jiedan/entity/" + capitalize(tableName) + ".java");
                files.add("sql/" + tableName.toLowerCase() + ".sql");
            }
        } else if ("backend".equals(type)) {
            // 后端模块：提取接口定义中的文件
            Pattern filePattern = Pattern.compile(
                    "-?\\s*([\\w]+)\\.java",
                    Pattern.CASE_INSENSITIVE);
            Matcher matcher = filePattern.matcher(content);
            while (matcher.find()) {
                String fileName = matcher.group(1);
                // 根据文件名推断路径
                if (fileName.endsWith("Controller")) {
                    files.add("src/main/java/com/jiedan/controller/" + fileName + ".java");
                } else if (fileName.endsWith("Service")) {
                    files.add("src/main/java/com/jiedan/service/" + fileName + ".java");
                } else if (fileName.endsWith("Mapper")) {
                    files.add("src/main/java/com/jiedan/mapper/" + fileName + ".java");
                    files.add("src/main/resources/mapper/" + fileName + ".xml");
                }
            }
        } else if ("frontend".equals(type)) {
            // 前端模块：提取页面和组件
            Pattern pagePattern = Pattern.compile(
                    "-?\\s*页面[:：]\\s*(.+?)(?=\\n|$)",
                    Pattern.CASE_INSENSITIVE);
            Matcher matcher = pagePattern.matcher(content);
            while (matcher.find()) {
                String pageName = matcher.group(1).trim();
                // 根据项目类型确定路径
                files.add("pages/" + pageName + "/" + pageName + ".vue");
                files.add("pages/" + pageName + "/" + pageName + ".js");
            }
        }

        return files;
    }

    /**
     * 创建默认模块（当无法从任务书提取时使用）
     */
    private void createDefaultModules(ParseResult result) {
        // 1. 数据库模块
        ModuleInfo dbModule = new ModuleInfo();
        dbModule.setName("数据库和实体类");
        dbModule.setDescription("数据库表结构和Java实体类");
        dbModule.setFiles(Arrays.asList(
            "src/main/java/com/jiedan/entity/",
            "sql/"
        ));
        dbModule.setType("database");
        dbModule.setOrder(1);
        dbModule.setDependencies(new ArrayList<>());
        result.getModules().add(dbModule);

        // 2. 配置模块
        ModuleInfo configModule = new ModuleInfo();
        configModule.setName("项目配置");
        configModule.setDescription("pom.xml、application.yml等配置文件");
        configModule.setFiles(Arrays.asList(
            "pom.xml",
            "src/main/resources/application.yml",
            "src/main/resources/application-dev.yml"
        ));
        configModule.setType("config");
        configModule.setOrder(5);
        configModule.setDependencies(Arrays.asList("database"));
        result.getModules().add(configModule);

        // 3. 后端核心模块
        ModuleInfo backendModule = new ModuleInfo();
        backendModule.setName("后端核心模块");
        backendModule.setDescription("Controller、Service、Mapper等后端代码");
        backendModule.setFiles(Arrays.asList(
            "src/main/java/com/jiedan/controller/",
            "src/main/java/com/jiedan/service/",
            "src/main/java/com/jiedan/mapper/"
        ));
        backendModule.setType("backend");
        backendModule.setOrder(10);
        backendModule.setDependencies(Arrays.asList("database", "config"));
        result.getModules().add(backendModule);

        // 4. 前端模块
        ModuleInfo frontendModule = new ModuleInfo();
        frontendModule.setName("前端页面模块");
        frontendModule.setDescription("前端页面、组件、工具类");
        frontendModule.setFiles(Arrays.asList(
            "pages/",
            "components/",
            "utils/"
        ));
        frontendModule.setType("frontend");
        frontendModule.setOrder(30);
        frontendModule.setDependencies(Arrays.asList("backend"));
        result.getModules().add(frontendModule);

        log.info("创建了 {} 个默认模块", result.getModules().size());
    }

    /**
     * 首字母大写
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
