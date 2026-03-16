package com.jiedan.service.ai.code;

import com.jiedan.dto.ai.AiChatRequest;
import com.jiedan.dto.ai.AiChatResponse;
import com.jiedan.dto.ai.code.GenerateCodeResponse;
import com.jiedan.dto.ai.code.GeneratedFile;
import com.jiedan.dto.ai.code.ScaffoldConfig;
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
 * 脚手架生成器
 * 负责生成项目脚手架（目录结构、配置文件、基础类）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScaffoldGenerator {

    private final AiStrategyFactory strategyFactory;

    /**
     * 生成项目脚手架
     */
    public GenerateCodeResponse generateScaffold(String projectId, String projectType, ScaffoldConfig config) {
        log.info("开始生成项目脚手架, projectId: {}, projectType: {}", projectId, projectType);

        // 参数校验
        if (projectId == null || projectId.trim().isEmpty()) {
            return GenerateCodeResponse.builder()
                    .success(false)
                    .errorMessage("projectId不能为空")
                    .build();
        }
        if (projectType == null || projectType.trim().isEmpty()) {
            return GenerateCodeResponse.builder()
                    .success(false)
                    .errorMessage("projectType不能为空")
                    .build();
        }

        try {
            // 1. 构建Prompt
            String prompt = buildScaffoldPrompt(projectType, config);

            // 2. 调用AI生成
            AIProviderStrategy strategy = strategyFactory.getStrategy(null);
            AiChatRequest chatRequest = AiChatRequest.builder()
                    .model(null)
                    .temperature(0.2)
                    .maxTokens(4000)
                    .build()
                    .addSystemMessage(prompt);

            AiChatResponse chatResponse = strategy.chatCompletion(chatRequest);

            if (!Boolean.TRUE.equals(chatResponse.getSuccess())) {
                log.error("脚手架生成失败: {}", chatResponse.getErrorMessage());
                return GenerateCodeResponse.builder()
                        .success(false)
                        .errorMessage("脚手架生成失败: " + chatResponse.getErrorMessage())
                        .build();
            }

            // 3. 解析生成的代码
            List<GeneratedFile> files = parseGeneratedFiles(chatResponse.getContent());

            // 4. 保存到项目目录
            String projectPath = "projects/" + projectId;
            saveFiles(projectPath, files);

            // 5. 返回结果
            return GenerateCodeResponse.builder()
                    .success(true)
                    .files(files)
                    .usage(chatResponse.getUsage())
                    .responseTimeMs(chatResponse.getResponseTimeMs())
                    .build();

        } catch (Exception e) {
            log.error("生成脚手架异常", e);
            return GenerateCodeResponse.builder()
                    .success(false)
                    .errorMessage("生成脚手架异常: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 构建脚手架生成Prompt
     */
    private String buildScaffoldPrompt(String projectType, ScaffoldConfig config) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("你是一位资深架构师。请生成").append(projectType).append("项目的脚手架代码。\n\n");

        prompt.append("【脚手架要求】\n");
        prompt.append("1. 标准的目录结构\n");
        prompt.append("2. 基础依赖配置\n");
        prompt.append("3. 基础配置文件\n");
        prompt.append("4. 通用的工具类和基础类\n");
        prompt.append("5. 不包含业务逻辑代码\n\n");

        // 根据项目类型添加特定要求
        switch (projectType.toLowerCase()) {
            case "springboot":
            case "springcloud":
                prompt.append(buildSpringBootScaffoldRequirements(config));
                break;
            case "wechat-miniprogram":
            case "douyin-miniprogram":
                prompt.append(buildMiniprogramScaffoldRequirements());
                break;
            case "python":
                prompt.append(buildPythonScaffoldRequirements(config));
                break;
            default:
                prompt.append("【项目类型】").append(projectType).append("\n\n");
        }

        prompt.append("【输出格式】\n");
        prompt.append("```\n");
        prompt.append("## 文件列表\n\n");
        prompt.append("### {文件路径}\n");
        prompt.append("```{语言}\n");
        prompt.append("{代码内容}\n");
        prompt.append("```\n");
        prompt.append("**说明**: {文件说明}\n");
        prompt.append("```\n\n");

        prompt.append("请输出完整的脚手架文件列表。");

        return prompt.toString();
    }

    /**
     * SpringBoot脚手架要求
     */
    private String buildSpringBootScaffoldRequirements(ScaffoldConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("【SpringBoot脚手架要求】\n");
        sb.append("1. pom.xml - Maven依赖配置\n");
        sb.append("   - Spring Boot ").append(config.getSpringBootVersion() != null ? config.getSpringBootVersion() : "3.2.0").append("\n");
        sb.append("   - Java ").append(config.getJavaVersion() != null ? config.getJavaVersion() : "17").append("\n");
        sb.append("   - 常用依赖：web, data-jpa, mysql, lombok, validation\n");
        sb.append("2. src/main/java/com/example/\n");
        sb.append("   - controller/ - 控制器目录\n");
        sb.append("   - service/ - 服务层目录\n");
        sb.append("   - repository/ - 数据访问层目录\n");
        sb.append("   - entity/ - 实体类目录\n");
        sb.append("   - config/ - 配置类目录\n");
        sb.append("   - dto/ - DTO目录\n");
        sb.append("   - exception/ - 异常处理目录\n");
        sb.append("   - Application.java - 启动类\n");
        sb.append("3. src/main/resources/\n");
        sb.append("   - application.yml - 主配置文件\n");
        sb.append("   - application-dev.yml - 开发环境配置\n");
        sb.append("   - db/migration/ - 数据库迁移脚本目录\n");
        sb.append("4. 基础类\n");
        sb.append("   - Result.java - 统一返回结果\n");
        sb.append("   - GlobalExceptionHandler.java - 全局异常处理\n");
        sb.append("   - BusinessException.java - 业务异常\n");
        sb.append("   - PageResult.java - 分页结果\n\n");
        return sb.toString();
    }

    /**
     * 小程序脚手架要求
     */
    private String buildMiniprogramScaffoldRequirements() {
        StringBuilder sb = new StringBuilder();
        sb.append("【小程序脚手架要求】\n");
        sb.append("1. app.js - 小程序入口\n");
        sb.append("2. app.json - 全局配置\n");
        sb.append("3. app.wxss - 全局样式\n");
        sb.append("4. pages/\n");
        sb.append("   - index/ - 首页\n");
        sb.append("     - index.js\n");
        sb.append("     - index.json\n");
        sb.append("     - index.wxml\n");
        sb.append("     - index.wxss\n");
        sb.append("5. utils/\n");
        sb.append("   - util.js - 工具函数\n");
        sb.append("   - request.js - 请求封装\n");
        sb.append("   - storage.js - 存储封装\n");
        sb.append("6. components/ - 组件目录\n");
        sb.append("7. config/\n");
        sb.append("   - api.js - API配置\n");
        sb.append("   - constants.js - 常量配置\n\n");
        return sb.toString();
    }

    /**
     * Python脚手架要求
     */
    private String buildPythonScaffoldRequirements(ScaffoldConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("【Python脚手架要求】\n");
        sb.append("1. requirements.txt - 依赖配置\n");
        sb.append("2. app/\n");
        sb.append("   - __init__.py\n");
        sb.append("   - models.py - 数据模型\n");
        sb.append("   - routes.py - 路由\n");
        sb.append("   - services.py - 服务层\n");
        sb.append("   - utils.py - 工具函数\n");
        sb.append("   - config.py - 配置\n");
        sb.append("3. tests/\n");
        sb.append("   - __init__.py\n");
        sb.append("   - test_*.py\n");
        sb.append("4. app.py - 应用入口\n");
        sb.append("5. README.md\n\n");
        return sb.toString();
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

        int end = section.lastIndexOf("```");
        if (end == -1 || end <= start) return "";

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
        // 取第一行
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
        if (filePath.endsWith(".json")) return "json";
        if (filePath.endsWith(".wxml")) return "wxml";
        if (filePath.endsWith(".wxss")) return "css";
        if (filePath.endsWith(".py")) return "python";
        if (filePath.endsWith(".md")) return "markdown";
        return "text";
    }

    /**
     * 保存文件到项目目录
     */
    private void saveFiles(String projectPath, List<GeneratedFile> files) throws IOException {
        for (GeneratedFile file : files) {
            Path filePath = Paths.get(projectPath, file.getPath());
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, file.getContent());
            log.info("保存文件: {}", filePath);
        }
    }
}
