package com.jiedan.service.ai;

import com.jiedan.dto.ai.*;
import com.jiedan.dto.ai.feedback.FeedbackShadowValidateRequest;
import com.jiedan.dto.ai.feedback.FeedbackShadowValidateResponse;
import com.jiedan.dto.ai.feedback.ValidationDecision;
import com.jiedan.service.ai.code.GitVersionControlService;
import com.jiedan.service.ai.feedback.FeedbackShadowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

        // 最终放行后保存最终交付版到req目录
        if (response.isSuccess() && response.getDocumentContent() != null) {
            try {
                saveFinalDocument(request.getProjectId(), "clarify-requirement", response.getDocumentContent());
                log.info("需求文档最终版已保存到req目录, projectId: {}", request.getProjectId());
            } catch (Exception e) {
                log.error("保存需求文档最终版失败, projectId: {}", request.getProjectId(), e);
            }
        }

        return response;
    }

    /**
     * 单次执行AI明确需求
     * 【简化】直接生成Markdown文档，不解析具体字段
     * @param previousIssues 之前验证的问题列表（重试时携带）
     */
    private ClarifyRequirementResponse executeClarifyRequirementOnce(ClarifyRequirementRequest request,
                                                                     List<String> previousIssues) {
        // 使用默认策略（传入null获取默认策略）
        AIProviderStrategy strategy = strategyFactory.getStrategy(null);

        // 【简化】直接要求生成Markdown文档
        StringBuilder systemPrompt = new StringBuilder("""
                你是拥有10年软件行业经验的资深需求分析师。请根据用户需求，生成一份完整的需求文档。
                请确保用户需求无遗漏，以及以下要求均满足，再交付。

                要求：
                1. 使用Markdown格式
                2. 包含以下内容：
                   - 需求概述
                   - 系统用户角色（角色名、定位、职责、权限范围）
                   - 功能模块清单（按系统用户角色分组，每个角色下含「模块→子模块→功能」）
                3. 文档要详细、完整、可实施
                4. 直接输出Markdown文档，不需要JSON格式
                5. 【重要】必须生成完整的文档，不能截断，确保所有模块都描述完整
                """);

        // 【重试时】携带之前的反馈建议
        if (previousIssues != null && !previousIssues.isEmpty()) {
            systemPrompt.append("\n\n【重要】之前生成的文档存在以下问题，请务必修正：\n");
            for (int i = 0; i < previousIssues.size(); i++) {
                systemPrompt.append(i + 1).append(". ").append(previousIssues.get(i)).append("\n");
            }
            systemPrompt.append("\n请根据以上问题重新生成完整的需求文档，确保所有问题都已解决。");
        }

        // 【简化】只使用需求描述
        String userPrompt = "用户需求：" + request.getRequirementDescription();

        // 【修复】使用续传机制，避免文档被截断
        String fullContent = chatWithContinuation(strategy, systemPrompt.toString(), userPrompt, 8000, 5);

        // 【简化】直接返回文档内容
        return ClarifyRequirementResponse.builder()
                .documentContent(fullContent)
                .build();
    }

    /**
     * AI拆分任务
     * 生成任务拆分文档并保存到项目目录，自动提交Git
     * 【启用重试机制】携带Feedback Shadow反馈建议进行重试
     * 【文档保存】每次执行保存一版，最终放行后保存最终交付版到task目录
     */
    public SplitTasksResponse splitTasks(SplitTasksRequest request) {
        log.info("开始AI拆分任务（带重试）, projectId: {}", request.getProjectId());

        // 初始化版本计数器
        documentVersionCounter.putIfAbsent(request.getProjectId(), new ConcurrentHashMap<>());
        Map<String, Integer> apiVersions = documentVersionCounter.get(request.getProjectId());

        // 使用重试服务执行AI任务
        SplitTasksResponse response = aiRetryService.executeWithRetry(
                (previousIssues) -> {
                    // 执行AI任务
                    SplitTasksResponse result = executeSplitTasksOnce(request, previousIssues);

                    // 每次执行成功都保存一版文档（放到feedback目录）
                    if (result.getDocumentContent() != null) {
                        int version = apiVersions.getOrDefault("split-tasks", 0) + 1;
                        apiVersions.put("split-tasks", version);
                        try {
                            saveDocumentVersion(request.getProjectId(), "split-tasks",
                                    result.getDocumentContent(), version, false);
                        } catch (Exception e) {
                            log.error("保存任务文档版本失败, projectId: {}, version: {}", request.getProjectId(), version, e);
                        }
                    }

                    return result;
                },
                request.getProjectId(),
                "split-tasks"
        );

        // 最终放行后保存最终交付版到task目录
        if (response.isSuccess() && response.getDocumentContent() != null) {
            try {
                saveFinalDocument(request.getProjectId(), "split-tasks", response.getDocumentContent());
                log.info("任务文档最终版已保存到task目录, projectId: {}", request.getProjectId());
            } catch (Exception e) {
                log.error("保存任务文档最终版失败, projectId: {}", request.getProjectId(), e);
            }
        }

        return response;
    }

    /**
     * 单次执行AI拆分任务
     * 【优化】生成程序员可用的详细技术任务书，包含前后端具体开发任务
     * @param previousIssues 之前验证的问题列表（重试时携带）
     */
    private SplitTasksResponse executeSplitTasksOnce(SplitTasksRequest request, List<String> previousIssues) {
        // 使用默认策略（传入null获取默认策略）
        AIProviderStrategy strategy = strategyFactory.getStrategy(null);

        // 【优化】生成AI自动开发可用的详细技术任务书
        StringBuilder systemPrompt = new StringBuilder("""
                你是一位20年资历的AI技术架构师，需生成供AI代码生成接口直接执行使用的详细技术任务书。

                【任务书要求】
                使用Markdown格式
                
                ## 核心输出要求
                1. 格式：纯Markdown，无解释性备注、无重复内容、无未完成截断内容；
                2. 粒度：所有指令需可直接映射为代码逻辑，每章节内容仅保留AI生成代码必需的核心信息；
                3. 完整性：覆盖全流程可运行代码生成所需的技术规格、接口、数据库、业务规则，不截断；
                4. 适配性：按AI单次生成能力拆分逻辑（但文档结构完整），避免冗余token占用。
                
                ## 任务书必须包含的章节（仅保留以下核心内容）

                ### 1. 项目技术规格
                - 技术栈确定：明确版本号（前端：微信小程序/VUE/html+css+js；后端：Spring Boot+MyBatis/Node.js；数据库：MySQL；接口：RESTful API、UTF-8）；
                - 项目结构规范：仅保留前端/后端核心代码目录（删test目录、废弃目录、非核心配置文件）；
                - 代码生成规则：仅保留可落地的约束（依赖版本固定、输入双重校验、敏感信息放配置、接口统一Result返回、JWT登录校验）。

                ### 2. 前端页面开发清单
                按模块（首页/商品/购物车/订单/用户）列出页面，每个页面仅包含：
                - 页面名称+路径（如pages/index/index）；
                - 核心功能描述（无冗余修饰）；
                - 必用组件（仅列实际代码引用的组件名）；
                - 接口调用清单（URL+请求方式）；
                - 页面跳转关系（仅列核心跳转路径+参数）。
                
                ### 3. 后端接口开发清单
                按模块（首页/商品/购物车/订单/用户）列出接口，每个接口仅包含：
                - 接口URL+请求方式（如GET /api/v1/banners）；
                - 请求参数（字段名+类型+是否必填+校验规则）；
                - 响应数据格式（字段名+类型+统一Result包装）；
                - 核心业务逻辑（仅保留代码可实现的逻辑，无冗余描述）；
                - 关联数据库表名。
                
                ### 4. 数据库表结构设计
                按业务模块设计表，每个表仅包含：
                - 表名+核心用途；
                - 字段定义（字段名+数据类型+约束+默认值+注释）；
                - 主键/外键/索引（仅核心索引）；
                - 表间关联关系（简洁描述）。
                
                ### 5. 业务逻辑规则
                仅保留可代码化的核心规则：
                - 核心业务流程（如“登录→加购→下单→支付→自提”）；
                - 数据校验规则（前端表单+后端参数校验的具体规则）；
                - 状态流转规则（订单状态：待付款/待自提/已完成的触发条件）；
                - 异常处理规则（接口异常、登录失效、参数错误的返回逻辑）。
                
                ### 6. 开发执行顺序
                按依赖优先级拆分，仅列核心阶段：
                - 阶段1：数据库表创建（按依赖顺序）；
                - 阶段2：后端接口开发（按“基础通用→首页→商品→购物车→订单→用户”）；
                - 阶段3：前端页面开发（同接口依赖顺序）；
                - 阶段4：接口联调（核心联调规则）。
                
                ### 7. 代码生成规范
                仅保留可落地的命名/结构/注释规则：
                - 命名规范：类名（大驼峰）、方法名（小驼峰）、变量名（小驼峰）、表名（下划线+小写）、字段名（下划线+小写）；
                - 代码结构：前端请求封装/存储封装规范、后端Controller/Service/Mapper分层规范；
                - 注释要求：接口/核心方法必须加注释（参数/返回值/业务说明）；
                - 错误码定义：统一错误码枚举（如SUCCESS=200、LOGIN_EXPIRE=401、PARAM_ERROR=400）。
                
                ## 禁止项
                1. 不添加解释性备注（如“供AI测试验证”“增强体验”等）；
                2. 不重复描述规则（如“可运行代码”仅提1次）；
                3. 不包含非核心目录/文件（如test、README.md、废弃目录）；
                4. 不出现未完成截断内容；
                5. 不使用冗余修饰词，所有描述需直接指向代码实现。
                
                ## 输出要求
                直接输出完整Markdown任务书，无前置说明、无JSON格式、无截断，确保AI可直接按文档分模块生成100%可运行代码。
                """);

        // 【重试时】携带之前的反馈建议
        if (previousIssues != null && !previousIssues.isEmpty()) {
            systemPrompt.append("\n\n【重要】之前的任务拆分存在以下问题，请务必修正：\n");
            for (int i = 0; i < previousIssues.size(); i++) {
                systemPrompt.append(i + 1).append(". ").append(previousIssues.get(i)).append("\n");
            }
            systemPrompt.append("\n请根据以上问题重新生成完整的任务拆分，确保所有问题都已解决。");
        }

        // 【简化】直接使用需求文档内容
        String userPrompt = "需求文档：\n" + request.getRequirementDoc();

        // 【修复】使用续传机制，避免文档被截断
        String fullContent = chatWithContinuation(strategy, systemPrompt.toString(), userPrompt, 8000, 5);

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
            case "generate-code":
                targetDir = "code";
                fileName = "CODE.md";
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
     */
    public GenerateCodeResponse generateCode(GenerateCodeRequest request) {
        log.info("开始AI生成代码（带重试）, 任务: {}", request.getTaskDescription());

        // 使用重试服务执行AI任务
        return aiRetryService.executeWithRetry(
                (previousIssues) -> executeGenerateCodeOnce(request, previousIssues),
                request.getProjectId(),
                "generate-code"
        );
    }

    /**
     * 单次执行AI生成代码
     * @param previousIssues 之前验证的问题列表（重试时携带）
     */
    private GenerateCodeResponse executeGenerateCodeOnce(GenerateCodeRequest request, List<String> previousIssues) {
        AIProviderStrategy strategy = strategyFactory.getStrategy(request.getModel());

        // 构建系统提示词
        StringBuilder systemPrompt = new StringBuilder("""
                你是一位资深的Java开发工程师，擅长编写高质量、规范的代码。
                请根据需求生成完整的Java代码，包含：
                1. 完整的类定义
                2. 必要的字段和注解
                3. 构造方法
                4. Getter/Setter方法
                5. 必要的业务方法
                6. 代码注释
                """);

        // 【重试时】携带之前的反馈建议
        if (previousIssues != null && !previousIssues.isEmpty()) {
            systemPrompt.append("\n\n【重要】之前生成的代码存在以下问题，请务必修正：\n");
            for (int i = 0; i < previousIssues.size(); i++) {
                systemPrompt.append(i + 1).append(". ").append(previousIssues.get(i)).append("\n");
            }
            systemPrompt.append("\n请根据以上问题重新生成完整的代码，确保所有问题都已解决。");
        }

        // 构建用户提示词
        StringBuilder userPrompt = new StringBuilder("开发任务：" + request.getTaskDescription());
        if (request.getLanguage() != null) {
            userPrompt.append("\n编程语言：").append(request.getLanguage());
        }
        if (request.getFramework() != null) {
            userPrompt.append("\n框架/技术栈：").append(request.getFramework());
        }
        if (request.getContextCode() != null) {
            userPrompt.append("\n相关代码上下文：\n").append(request.getContextCode());
        }
        if (request.getRequirements() != null && !request.getRequirements().isEmpty()) {
            userPrompt.append("\n特殊要求：").append(String.join(", ", request.getRequirements()));
        }

        // 【修复】使用续传机制，避免代码被截断
        String fullContent = chatWithContinuation(strategy, systemPrompt.toString(), userPrompt.toString(), 8000, 5);

        // 构建响应（暂不设置success，由重试服务设置）
        return GenerateCodeResponse.builder()
                .code(fullContent)
                .explanation("代码已生成，请查看代码注释了解详细说明")
                .rawResponse(fullContent)
                .model(request.getModel())
                .build();
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

        while (!isComplete && continuationCount <= maxContinuation) {
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
                fullContent.append(content);
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
                messages.add(AiMessage.user("请继续生成剩余内容，从上次中断的地方开始，不要重复已生成的内容。"));
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
}
