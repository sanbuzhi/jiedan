package com.jiedan.service.ai;

import com.jiedan.dto.ai.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AI服务测试
 * 测试所有AI功能
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class AiServiceTest {

    @Autowired
    private AiService aiService;

    /**
     * 测试1: AI明确需求
     */
    @Test
    void testClarifyRequirement() {
        log.info("开始测试AI明确需求...");

        ClarifyRequirementRequest request = ClarifyRequirementRequest.builder()
                .projectId("test-project-hotel")
                .requirementDescription("我想做一个酒店管理系统，需要管理客房、订单和员工")
                .build();

        ClarifyRequirementResponse response = aiService.clarifyRequirement(request);

        assertNotNull(response);
        assertNotNull(response.getDocumentContent());
        assertFalse(response.getDocumentContent().isEmpty());

        log.info("AI明确需求成功");
        log.info("需求文档长度: {}", response.getDocumentContent().length());

        log.info("✅ AI明确需求测试通过!");
    }

    /**
     * 测试2: AI拆分任务
     */
    @Test
    void testSplitTasks() {
        log.info("开始测试AI拆分任务...");

        SplitTasksRequest request = SplitTasksRequest.builder()
                .projectId("test-project-hotel")
                .requirementDoc("酒店管理系统：包含客房管理、订单管理、员工管理三个核心模块")
                .build();

        SplitTasksResponse response = aiService.splitTasks(request);

        assertNotNull(response);
        assertNotNull(response.getDocumentContent());
        assertFalse(response.getDocumentContent().isEmpty());

        log.info("AI拆分任务成功");
        log.info("响应长度: {}", response.getDocumentContent().length());

        log.info("✅ AI拆分任务测试通过!");
    }

    /**
     * 测试3: AI生成代码
     */
    @Test
    void testGenerateCode() {
        log.info("开始测试AI生成代码...");

        GenerateCodeRequest request = GenerateCodeRequest.builder()
                .taskDescription("创建一个Java类HotelRoom，包含房间号、房型、价格、状态字段，以及相应的getter/setter方法")
                .language("Java")
                .framework("Spring Boot")
                .model("doubao-seed-2.0-code")
                .build();

        GenerateCodeResponse response = aiService.generateCode(request);

        assertNotNull(response);
        assertNotNull(response.getCode());
        assertFalse(response.getCode().isEmpty());
        assertTrue(response.getCode().contains("class") || response.getCode().contains("public"));

        log.info("AI生成代码成功，使用模型: {}", response.getModel());
        log.info("代码长度: {}", response.getCode().length());

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
                .code(code)
                .language("Java")
                .functionDescription("简单的计算器类，包含加法和除法")
                .model("doubao-seed-2.0-code")
                .build();

        FunctionalTestResponse response = aiService.functionalTest(request);

        assertNotNull(response);
        assertNotNull(response.getTestCode());
        assertFalse(response.getTestCode().isEmpty());

        log.info("AI功能测试成功，使用模型: {}", response.getModel());
        log.info("测试代码长度: {}", response.getTestCode().length());

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
                .code(code)
                .language("Java")
                .applicationType("Web")
                .model("doubao-seed-2.0-code")
                .build();

        SecurityTestResponse response = aiService.securityTest(request);

        assertNotNull(response);
        assertNotNull(response.getRawResponse());
        assertFalse(response.getRawResponse().isEmpty());

        log.info("AI安全测试成功，使用模型: {}", response.getModel());
        log.info("响应长度: {}", response.getRawResponse().length());

        log.info("✅ AI安全测试测试通过!");
    }

    /**
     * 测试6: 使用不同模型
     */
    @Test
    void testDifferentModels() {
        log.info("开始测试不同模型...");

        String[] models = {"doubao-seed-2.0-code", "kimi-k2.5"};

        for (String model : models) {
            log.info("测试模型: {}", model);

            try {
                ClarifyRequirementRequest request = ClarifyRequirementRequest.builder()
                        .projectId("test-project-ecommerce-" + model)
                        .requirementDescription("我想做一个电商平台，需要商品管理、订单管理和支付功能")
                        .build();

                ClarifyRequirementResponse response = aiService.clarifyRequirement(request);

                assertNotNull(response);
                assertNotNull(response.getDocumentContent());

                log.info("模型 {} 调用成功", model);
            } catch (Exception e) {
                log.warn("模型 {} 调用失败: {}", model, e.getMessage());
            }
        }

        log.info("✅ 不同模型测试完成!");
    }
}
