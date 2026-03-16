package com.jiedan.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiedan.config.HuoshanAiProperties;
import com.jiedan.dto.ai.*;
import com.jiedan.service.ai.AiRateLimiterService;
import com.jiedan.service.ai.AiRetryService;
import com.jiedan.service.ai.AiService;
import com.jiedan.service.ai.AiStrategyFactory;
import com.jiedan.service.ai.HuoshanAiStrategy;
import com.jiedan.service.ai.code.GitVersionControlService;
import com.jiedan.service.ai.feedback.FeedbackShadowService;
import org.mockito.Mockito;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AI服务手动测试
 * 不使用SpringBootTest，手动创建依赖
 * 【更新】适配简化后的DTO结构
 */
@Slf4j
public class AiServiceManualTest {

    private static AiService aiService;

    @BeforeAll
    static void setUp() {
        // 手动创建配置
        HuoshanAiProperties properties = new HuoshanAiProperties();
        properties.setApiKey("e492b8fd-34cf-4016-a7bb-f3bb055135bc");
        properties.setBaseUrl("https://ark.cn-beijing.volces.com/api/coding/v3");
        properties.setDefaultModel("doubao-seed-2.0-code");
        properties.setTimeout(30000);

        // 手动创建依赖
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        // 创建策略
        HuoshanAiStrategy huoshanStrategy = new HuoshanAiStrategy(properties, restTemplate, objectMapper);

        // 创建限流服务
        AiRateLimiterService rateLimiterService = new AiRateLimiterService();

        // 创建工厂
        AiStrategyFactory factory = new AiStrategyFactory(Collections.singletonList(huoshanStrategy), rateLimiterService);

        // 创建Git版本控制服务（mock）
        GitVersionControlService gitService = new GitVersionControlService();

        // 创建FeedbackShadowService（mock）
        FeedbackShadowService feedbackShadowService = Mockito.mock(FeedbackShadowService.class);

        // 创建AiRetryService
        AiRetryService aiRetryService = new AiRetryService(feedbackShadowService);

        // 创建服务
        aiService = new AiService(factory, gitService, feedbackShadowService, aiRetryService);

        log.info("AI服务初始化完成");
    }

    /**
     * 测试1: AI明确需求
     */
    @Test
    void testClarifyRequirement() {
        log.info("开始测试AI明确需求...");

        // 【简化】只使用必需的字段
        ClarifyRequirementRequest request = ClarifyRequirementRequest.builder()
                .projectId("test-project-001")
                .requirementDescription("我想做一个酒店管理系统，需要管理客房、订单和员工")
                .build();

        ClarifyRequirementResponse response = aiService.clarifyRequirement(request);

        assertNotNull(response);
        // 【简化】只验证documentContent
        assertNotNull(response.getDocumentContent());
        assertFalse(response.getDocumentContent().isEmpty());

        log.info("AI明确需求成功");
        log.info("需求文档长度: {}", response.getDocumentContent().length());
        log.info("需求文档预览:\n{}", response.getDocumentContent().substring(0,
                Math.min(500, response.getDocumentContent().length())));

        log.info("✅ AI明确需求测试通过!");
    }

    /**
     * 测试2: AI拆分任务
     */
    @Test
    void testSplitTasks() {
        log.info("开始测试AI拆分任务...");

        // 【简化】只使用必需的字段
        SplitTasksRequest request = SplitTasksRequest.builder()
                .projectId("test-project-001")
                .requirementDoc("# 酒店管理系统需求\n\n包含客房管理、订单管理、员工管理三个核心模块")
                .build();

        SplitTasksResponse response = aiService.splitTasks(request);

        assertNotNull(response);
        // 【简化】只验证documentContent
        assertNotNull(response.getDocumentContent());
        assertFalse(response.getDocumentContent().isEmpty());

        log.info("AI拆分任务成功");
        log.info("任务文档长度: {}", response.getDocumentContent().length());
        log.info("任务拆分预览:\n{}", response.getDocumentContent().substring(0,
                Math.min(500, response.getDocumentContent().length())));

        log.info("✅ AI拆分任务测试通过!");
    }

    /**
     * 测试3: AI生成代码
     */
    @Test
    void testGenerateCode() {
        log.info("开始测试AI生成代码...");

        GenerateCodeRequest request = GenerateCodeRequest.builder()
                .projectId("test-project-001")
                .taskDescription("创建一个Java类HotelRoom，包含房间号、房型、价格、状态字段，以及相应的getter/setter方法")
                .language("Java")
                .framework("Spring Boot")
                .build();

        GenerateCodeResponse response = aiService.generateCode(request);

        assertNotNull(response);
        assertNotNull(response.getCode());
        assertFalse(response.getCode().isEmpty());
        assertTrue(response.getCode().contains("class") || response.getCode().contains("public"));

        log.info("AI生成代码成功");
        log.info("代码长度: {}", response.getCode().length());
        log.info("生成的代码:\n{}", response.getCode());

        log.info("✅ AI生成代码测试通过!");
    }

    /**
     * 测试4: AI功能测试
     */
    @Test
    void testFunctionalTest() {
        log.info("开始测试AI功能测试...");

        String code = """
                public class Calculator {
                    public int add(int a, int b) {
                        return a + b;
                    }
                    
                    public int divide(int a, int b) {
                        return a / b;
                    }
                }
                """;

        FunctionalTestRequest request = FunctionalTestRequest.builder()
                .projectId("test-project-001")
                .code(code)
                .language("Java")
                .functionDescription("简单的计算器类，包含加法和除法")
                .build();

        FunctionalTestResponse response = aiService.functionalTest(request);

        assertNotNull(response);
        assertNotNull(response.getTestCode());
        assertFalse(response.getTestCode().isEmpty());

        log.info("AI功能测试成功");
        log.info("测试代码长度: {}", response.getTestCode().length());
        log.info("测试代码:\n{}", response.getTestCode());

        log.info("✅ AI功能测试测试通过!");
    }

    /**
     * 测试5: AI安全测试
     */
    @Test
    void testSecurityTest() {
        log.info("开始测试AI安全测试...");

        String code = """
                @RestController
                public class UserController {
                    
                    @Autowired
                    private UserRepository userRepository;
                    
                    @GetMapping("/users")
                    public List<User> getUsers(@RequestParam String name) {
                        String sql = "SELECT * FROM users WHERE name = '" + name + "'";
                        return userRepository.queryBySql(sql);
                    }
                }
                """;

        SecurityTestRequest request = SecurityTestRequest.builder()
                .projectId("test-project-001")
                .code(code)
                .language("Java")
                .applicationType("Web")
                .build();

        SecurityTestResponse response = aiService.securityTest(request);

        assertNotNull(response);
        assertNotNull(response.getRawResponse());
        assertFalse(response.getRawResponse().isEmpty());

        log.info("AI安全测试成功");
        log.info("响应长度: {}", response.getRawResponse().length());
        log.info("安全测试结果:\n{}", response.getRawResponse());

        log.info("✅ AI安全测试测试通过!");
    }

    /**
     * 测试6: 验证重试机制
     */
    @Test
    void testRetryMechanism() {
        log.info("开始测试重试机制...");

        // 使用一个可能导致验证失败的需求描述来测试重试
        ClarifyRequirementRequest request = ClarifyRequirementRequest.builder()
                .projectId("test-retry-001")
                .requirementDescription("做一个简单的待办事项应用")
                .build();

        ClarifyRequirementResponse response = aiService.clarifyRequirement(request);

        assertNotNull(response);
        assertNotNull(response.getDocumentContent());
        // 验证结果可能是成功或失败，但都应有documentContent
        assertFalse(response.getDocumentContent().isEmpty());

        log.info("重试机制测试完成，success={}", response.isSuccess());
        log.info("验证决策: {}", response.getValidationDecision());
        if (response.getValidationIssues() != null && !response.getValidationIssues().isEmpty()) {
            log.info("验证问题: {}", response.getValidationIssues());
        }

        log.info("✅ 重试机制测试完成!");
    }
}
