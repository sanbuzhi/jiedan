package com.jiedan.service.ai.code;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 项目结构模板
 * 定义9种项目类型的标准目录结构和必需文件
 */
@Slf4j
@Component
public class ProjectStructureTemplate {

    /**
     * 项目类型枚举
     */
    public enum ProjectType {
        SPRINGBOOT_BACKEND,
        SPRINGCLOUD_BACKEND,
        PYTHON_BACKEND,
        DESKTOP_APP,
        WEB_FRONTEND,
        WECHAT_MINIPROGRAM,
        DOUYIN_MINIPROGRAM,
        WINDOWS_SCRIPT,
        LINUX_SCRIPT
    }

    /**
     * 获取项目结构模板
     */
    public ProjectStructure getTemplate(ProjectType type) {
        return switch (type) {
            case SPRINGBOOT_BACKEND -> getSpringBootStructure();
            case SPRINGCLOUD_BACKEND -> getSpringCloudStructure();
            case PYTHON_BACKEND -> getPythonStructure();
            case DESKTOP_APP -> getDesktopStructure();
            case WEB_FRONTEND -> getWebFrontendStructure();
            case WECHAT_MINIPROGRAM -> getWechatMiniprogramStructure();
            case DOUYIN_MINIPROGRAM -> getDouyinMiniprogramStructure();
            case WINDOWS_SCRIPT -> getWindowsScriptStructure();
            case LINUX_SCRIPT -> getLinuxScriptStructure();
        };
    }

    /**
     * SpringBoot后端项目结构
     */
    private ProjectStructure getSpringBootStructure() {
        return ProjectStructure.builder()
                .type(ProjectType.SPRINGBOOT_BACKEND)
                .name("SpringBoot后端项目")
                .description("标准的SpringBoot单体应用项目结构")
                .requiredDirectories(Arrays.asList(
                        "src/main/java/com/example",
                        "src/main/java/com/example/controller",
                        "src/main/java/com/example/service",
                        "src/main/java/com/example/repository",
                        "src/main/java/com/example/entity",
                        "src/main/java/com/example/config",
                        "src/main/java/com/example/dto",
                        "src/main/java/com/example/exception",
                        "src/main/resources",
                        "src/main/resources/db/migration",
                        "src/test/java"
                ))
                .requiredFiles(Arrays.asList(
                        RequiredFile.builder()
                                .path("pom.xml")
                                .description("Maven依赖配置")
                                .checkPattern("<artifactId>spring-boot-starter-parent</artifactId>")
                                .build(),
                        RequiredFile.builder()
                                .path("src/main/resources/application.yml")
                                .description("主配置文件")
                                .build(),
                        RequiredFile.builder()
                                .path("src/main/java/com/example/Application.java")
                                .description("启动类")
                                .checkPattern("@SpringBootApplication")
                                .build()
                ))
                .optionalFiles(Arrays.asList(
                        "README.md",
                        ".gitignore",
                        "Dockerfile"
                ))
                .build();
    }

    /**
     * SpringCloud后端项目结构
     */
    private ProjectStructure getSpringCloudStructure() {
        return ProjectStructure.builder()
                .type(ProjectType.SPRINGCLOUD_BACKEND)
                .name("SpringCloud微服务项目")
                .description("SpringCloud微服务架构项目结构")
                .requiredDirectories(Arrays.asList(
                        "eureka-server/src/main/java",
                        "gateway/src/main/java",
                        "common/src/main/java",
                        "service-a/src/main/java",
                        "service-b/src/main/java"
                ))
                .requiredFiles(Arrays.asList(
                        RequiredFile.builder()
                                .path("pom.xml")
                                .description("父POM配置")
                                .build(),
                        RequiredFile.builder()
                                .path("eureka-server/src/main/java/EurekaServerApplication.java")
                                .description("注册中心启动类")
                                .checkPattern("@EnableEurekaServer")
                                .build(),
                        RequiredFile.builder()
                                .path("gateway/src/main/java/GatewayApplication.java")
                                .description("网关启动类")
                                .checkPattern("@EnableGateway")
                                .build()
                ))
                .optionalFiles(Arrays.asList(
                        "docker-compose.yml",
                        "README.md"
                ))
                .build();
    }

    /**
     * Python后端项目结构
     */
    private ProjectStructure getPythonStructure() {
        return ProjectStructure.builder()
                .type(ProjectType.PYTHON_BACKEND)
                .name("Python后端项目")
                .description("Python Flask/FastAPI后端项目结构")
                .requiredDirectories(Arrays.asList(
                        "app",
                        "app/models",
                        "app/routes",
                        "app/services",
                        "app/utils",
                        "tests",
                        "docs"
                ))
                .requiredFiles(Arrays.asList(
                        RequiredFile.builder()
                                .path("requirements.txt")
                                .description("Python依赖配置")
                                .build(),
                        RequiredFile.builder()
                                .path("app/__init__.py")
                                .description("应用包初始化")
                                .build(),
                        RequiredFile.builder()
                                .path("app.py")
                                .description("应用入口")
                                .build(),
                        RequiredFile.builder()
                                .path("config.py")
                                .description("配置文件")
                                .build()
                ))
                .optionalFiles(Arrays.asList(
                        "README.md",
                        ".env",
                        "Dockerfile",
                        "docker-compose.yml"
                ))
                .build();
    }

    /**
     * 桌面端项目结构
     */
    private ProjectStructure getDesktopStructure() {
        return ProjectStructure.builder()
                .type(ProjectType.DESKTOP_APP)
                .name("桌面端应用项目")
                .description("Electron桌面应用项目结构")
                .requiredDirectories(Arrays.asList(
                        "src",
                        "src/main",
                        "src/renderer",
                        "src/preload",
                        "assets",
                        "build"
                ))
                .requiredFiles(Arrays.asList(
                        RequiredFile.builder()
                                .path("package.json")
                                .description("Node.js依赖配置")
                                .build(),
                        RequiredFile.builder()
                                .path("src/main/main.js")
                                .description("主进程入口")
                                .build(),
                        RequiredFile.builder()
                                .path("src/renderer/index.html")
                                .description("渲染页面")
                                .build()
                ))
                .optionalFiles(Arrays.asList(
                        "README.md",
                        ".gitignore",
                        "electron-builder.yml"
                ))
                .build();
    }

    /**
     * 网页前端项目结构
     */
    private ProjectStructure getWebFrontendStructure() {
        return ProjectStructure.builder()
                .type(ProjectType.WEB_FRONTEND)
                .name("网页前端项目")
                .description("Vue/React前端项目结构")
                .requiredDirectories(Arrays.asList(
                        "src",
                        "src/components",
                        "src/views",
                        "src/router",
                        "src/store",
                        "src/utils",
                        "src/assets",
                        "public"
                ))
                .requiredFiles(Arrays.asList(
                        RequiredFile.builder()
                                .path("package.json")
                                .description("Node.js依赖配置")
                                .build(),
                        RequiredFile.builder()
                                .path("src/main.js")
                                .description("应用入口")
                                .build(),
                        RequiredFile.builder()
                                .path("src/App.vue")
                                .description("根组件")
                                .build(),
                        RequiredFile.builder()
                                .path("index.html")
                                .description("HTML入口")
                                .build()
                ))
                .optionalFiles(Arrays.asList(
                        "README.md",
                        ".gitignore",
                        "vite.config.js",
                        "vue.config.js"
                ))
                .build();
    }

    /**
     * 微信小程序项目结构
     */
    private ProjectStructure getWechatMiniprogramStructure() {
        return ProjectStructure.builder()
                .type(ProjectType.WECHAT_MINIPROGRAM)
                .name("微信小程序项目")
                .description("微信小程序标准项目结构")
                .requiredDirectories(Arrays.asList(
                        "pages",
                        "pages/index",
                        "components",
                        "utils",
                        "images"
                ))
                .requiredFiles(Arrays.asList(
                        RequiredFile.builder()
                                .path("app.js")
                                .description("小程序入口")
                                .build(),
                        RequiredFile.builder()
                                .path("app.json")
                                .description("全局配置")
                                .build(),
                        RequiredFile.builder()
                                .path("app.wxss")
                                .description("全局样式")
                                .build(),
                        RequiredFile.builder()
                                .path("pages/index/index.js")
                                .description("首页逻辑")
                                .build(),
                        RequiredFile.builder()
                                .path("pages/index/index.json")
                                .description("首页配置")
                                .build(),
                        RequiredFile.builder()
                                .path("pages/index/index.wxml")
                                .description("首页结构")
                                .build(),
                        RequiredFile.builder()
                                .path("pages/index/index.wxss")
                                .description("首页样式")
                                .build()
                ))
                .optionalFiles(Arrays.asList(
                        "README.md",
                        "project.config.json",
                        "sitemap.json"
                ))
                .build();
    }

    /**
     * 抖音小程序项目结构
     */
    private ProjectStructure getDouyinMiniprogramStructure() {
        return ProjectStructure.builder()
                .type(ProjectType.DOUYIN_MINIPROGRAM)
                .name("抖音小程序项目")
                .description("抖音小程序标准项目结构")
                .requiredDirectories(Arrays.asList(
                        "pages",
                        "pages/index",
                        "components",
                        "utils",
                        "images"
                ))
                .requiredFiles(Arrays.asList(
                        RequiredFile.builder()
                                .path("app.js")
                                .description("小程序入口")
                                .build(),
                        RequiredFile.builder()
                                .path("app.json")
                                .description("全局配置")
                                .build(),
                        RequiredFile.builder()
                                .path("app.ttss")
                                .description("全局样式")
                                .build(),
                        RequiredFile.builder()
                                .path("pages/index/index.js")
                                .description("首页逻辑")
                                .build(),
                        RequiredFile.builder()
                                .path("pages/index/index.json")
                                .description("首页配置")
                                .build(),
                        RequiredFile.builder()
                                .path("pages/index/index.ttml")
                                .description("首页结构")
                                .build(),
                        RequiredFile.builder()
                                .path("pages/index/index.ttss")
                                .description("首页样式")
                                .build()
                ))
                .optionalFiles(Arrays.asList(
                        "README.md",
                        "project.config.json"
                ))
                .build();
    }

    /**
     * Windows脚本项目结构
     */
    private ProjectStructure getWindowsScriptStructure() {
        return ProjectStructure.builder()
                .type(ProjectType.WINDOWS_SCRIPT)
                .name("Windows脚本项目")
                .description("Windows批处理/PowerShell脚本项目")
                .requiredDirectories(Arrays.asList(
                        "scripts",
                        "config",
                        "logs"
                ))
                .requiredFiles(Arrays.asList(
                        RequiredFile.builder()
                                .path("main.bat")
                                .description("主批处理脚本")
                                .build(),
                        RequiredFile.builder()
                                .path("config/config.ini")
                                .description("配置文件")
                                .build()
                ))
                .optionalFiles(Arrays.asList(
                        "README.md",
                        "setup.ps1",
                        "utils.ps1"
                ))
                .build();
    }

    /**
     * Linux脚本项目结构
     */
    private ProjectStructure getLinuxScriptStructure() {
        return ProjectStructure.builder()
                .type(ProjectType.LINUX_SCRIPT)
                .name("Linux脚本项目")
                .description("Linux Shell脚本项目")
                .requiredDirectories(Arrays.asList(
                        "scripts",
                        "config",
                        "logs",
                        "lib"
                ))
                .requiredFiles(Arrays.asList(
                        RequiredFile.builder()
                                .path("main.sh")
                                .description("主Shell脚本")
                                .build(),
                        RequiredFile.builder()
                                .path("config/config.conf")
                                .description("配置文件")
                                .build(),
                        RequiredFile.builder()
                                .path("lib/common.sh")
                                .description("公共函数库")
                                .build()
                ))
                .optionalFiles(Arrays.asList(
                        "README.md",
                        "Makefile",
                        "install.sh"
                ))
                .build();
    }

    /**
     * 验证项目结构
     */
    public StructureValidationResult validateStructure(String projectPath, ProjectType type) {
        ProjectStructure template = getTemplate(type);
        StructureValidationResult result = new StructureValidationResult();
        result.setProjectType(type);
        result.setProjectPath(projectPath);

        List<StructureIssue> issues = new ArrayList<>();

        // 验证必需目录
        for (String dir : template.getRequiredDirectories()) {
            java.nio.file.Path dirPath = java.nio.file.Paths.get(projectPath, dir);
            if (!java.nio.file.Files.exists(dirPath)) {
                issues.add(StructureIssue.builder()
                        .type(StructureIssue.IssueType.MISSING_DIRECTORY)
                        .path(dir)
                        .message("缺少必需目录: " + dir)
                        .severity(StructureIssue.Severity.ERROR)
                        .build());
            }
        }

        // 验证必需文件
        for (RequiredFile file : template.getRequiredFiles()) {
            java.nio.file.Path filePath = java.nio.file.Paths.get(projectPath, file.getPath());
            if (!java.nio.file.Files.exists(filePath)) {
                issues.add(StructureIssue.builder()
                        .type(StructureIssue.IssueType.MISSING_FILE)
                        .path(file.getPath())
                        .message("缺少必需文件: " + file.getPath() + " (" + file.getDescription() + ")")
                        .severity(StructureIssue.Severity.ERROR)
                        .build());
            } else if (file.getCheckPattern() != null) {
                // 检查文件内容模式
                try {
                    String content = java.nio.file.Files.readString(filePath);
                    if (!content.contains(file.getCheckPattern())) {
                        issues.add(StructureIssue.builder()
                                .type(StructureIssue.IssueType.CONTENT_MISMATCH)
                                .path(file.getPath())
                                .message("文件内容不符合要求，缺少: " + file.getCheckPattern())
                                .severity(StructureIssue.Severity.WARNING)
                                .build());
                    }
                } catch (Exception e) {
                    log.warn("读取文件失败: {}", filePath);
                }
            }
        }

        result.setIssues(issues);
        result.setValid(issues.stream().noneMatch(i -> i.getSeverity() == StructureIssue.Severity.ERROR));

        return result;
    }

    /**
     * 生成结构检查报告
     */
    public String generateStructureReport(StructureValidationResult result) {
        StringBuilder report = new StringBuilder();
        report.append("# 项目结构检查报告\n\n");
        report.append("项目类型: ").append(result.getProjectType()).append("\n");
        report.append("项目路径: ").append(result.getProjectPath()).append("\n");
        report.append("检查结果: ").append(result.isValid() ? "✅ 通过" : "❌ 未通过").append("\n\n");

        if (result.getIssues().isEmpty()) {
            report.append("未发现结构问题。\n");
        } else {
            report.append("## 发现的问题\n\n");
            for (StructureIssue issue : result.getIssues()) {
                report.append("- **[").append(issue.getSeverity()).append("]** ")
                        .append(issue.getType()).append(": ")
                        .append(issue.getMessage()).append("\n");
            }
        }

        return report.toString();
    }

    // ========== 内部类定义 ==========

    @Data
    @Builder
    public static class ProjectStructure {
        private ProjectType type;
        private String name;
        private String description;
        private List<String> requiredDirectories;
        private List<RequiredFile> requiredFiles;
        private List<String> optionalFiles;
    }

    @Data
    @Builder
    public static class RequiredFile {
        private String path;
        private String description;
        private String checkPattern; // 可选：检查文件内容是否包含特定模式
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StructureValidationResult {
        private ProjectType projectType;
        private String projectPath;
        private boolean valid;
        private List<StructureIssue> issues;
    }

    @Data
    @Builder
    public static class StructureIssue {
        private IssueType type;
        private String path;
        private String message;
        private Severity severity;

        public enum IssueType {
            MISSING_DIRECTORY,
            MISSING_FILE,
            CONTENT_MISMATCH,
            PERMISSION_ERROR
        }

        public enum Severity {
            ERROR,
            WARNING,
            INFO
        }
    }
}
