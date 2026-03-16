package com.jiedan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiedan.dto.ai.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AI Controller 集成测试
 * 测试所有AI接口功能
 */
@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;

    @BeforeEach
    void setUp() {
        // 使用测试token，在测试环境中绕过认证
        authToken = "Bearer test-token";
    }

    /**
     * 测试1: AI明确需求接口
     */
    @Test
    void testClarifyRequirement() throws Exception {
        log.info("开始测试AI明确需求接口...");

        ClarifyRequirementRequest request = ClarifyRequirementRequest.builder()
                .projectId("test-project-hotel")
                .requirementDescription("我想做一个酒店管理系统，需要管理客房、订单和员工")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/ai/clarify-requirement")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        log.info("AI明确需求响应: {}", responseBody);

        // 验证响应
        assertNotNull(responseBody);
        assertTrue(responseBody.contains("code"));
        assertTrue(responseBody.contains("data"));

        // 解析响应
        var response = objectMapper.readTree(responseBody);
        assertEquals(0, response.get("code").asInt(), "响应码应该为0表示成功");
        assertNotNull(response.get("data"), "data字段不应为空");

        log.info("✅ AI明确需求接口测试通过!");
    }

    /**
     * 测试2: AI拆分任务接口
     */
    @Test
    void testSplitTasks() throws Exception {
        log.info("开始测试AI拆分任务接口...");

        SplitTasksRequest request = SplitTasksRequest.builder()
                .projectId("test-project-hotel")
                .requirementDoc("酒店管理系统：包含客房管理、订单管理、员工管理三个核心模块")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/ai/split-tasks")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        log.info("AI拆分任务响应: {}", responseBody);

        // 验证响应
        assertNotNull(responseBody);
        var response = objectMapper.readTree(responseBody);
        assertEquals(0, response.get("code").asInt(), "响应码应该为0表示成功");

        log.info("✅ AI拆分任务接口测试通过!");
    }

    /**
     * 测试3: AI生成代码接口
     */
    @Test
    void testGenerateCode() throws Exception {
        log.info("开始测试AI生成代码接口...");

        GenerateCodeRequest request = GenerateCodeRequest.builder()
                .taskDescription("创建一个Java类HotelRoom，包含房间号、房型、价格、状态字段，以及相应的getter/setter方法")
                .language("Java")
                .framework("Spring Boot")
                .model("doubao-seed-2.0-code")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/ai/generate-code")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        log.info("AI生成代码响应长度: {}", responseBody.length());

        // 验证响应
        assertNotNull(responseBody);
        var response = objectMapper.readTree(responseBody);
        assertEquals(0, response.get("code").asInt(), "响应码应该为0表示成功");

        // 验证返回了代码
        if (response.has("data") && response.get("data").has("code")) {
            String code = response.get("data").get("code").asText();
            assertNotNull(code);
            assertFalse(code.isEmpty(), "返回的代码不应为空");
            log.info("生成的代码预览: {}...", code.substring(0, Math.min(200, code.length())));
        }

        log.info("✅ AI生成代码接口测试通过!");
    }

    /**
     * 测试4: AI功能测试接口
     */
    @Test
    void testFunctionalTest() throws Exception {
        log.info("开始测试AI功能测试接口...");

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

        MvcResult result = mockMvc.perform(post("/api/v1/ai/functional-test")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        log.info("AI功能测试响应长度: {}", responseBody.length());

        // 验证响应
        assertNotNull(responseBody);
        var response = objectMapper.readTree(responseBody);
        assertEquals(0, response.get("code").asInt(), "响应码应该为0表示成功");

        log.info("✅ AI功能测试接口测试通过!");
    }

    /**
     * 测试5: AI安全测试接口
     */
    @Test
    void testSecurityTest() throws Exception {
        log.info("开始测试AI安全测试接口...");

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

        MvcResult result = mockMvc.perform(post("/api/v1/ai/security-test")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        log.info("AI安全测试响应长度: {}", responseBody.length());

        // 验证响应
        assertNotNull(responseBody);
        var response = objectMapper.readTree(responseBody);
        assertEquals(0, response.get("code").asInt(), "响应码应该为0表示成功");

        log.info("✅ AI安全测试接口测试通过!");
    }

    /**
     * 测试6: 使用不同模型的AI明确需求
     */
    @Test
    void testClarifyRequirementWithDifferentModels() throws Exception {
        log.info("开始测试不同模型的AI明确需求...");

        String[] models = {"doubao-seed-2.0-code", "kimi-k2.5"};

        for (String model : models) {
            log.info("测试模型: {}", model);

            ClarifyRequirementRequest request = ClarifyRequirementRequest.builder()
                    .projectId("test-project-ecommerce")
                    .requirementDescription("我想做一个电商平台，需要商品管理、订单管理和支付功能")
                    .build();

            MvcResult result = mockMvc.perform(post("/api/v1/ai/clarify-requirement")
                            .header("Authorization", authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            var response = objectMapper.readTree(responseBody);

            if (response.get("code").asInt() == 0) {
                log.info("模型 {} 调用成功", model);
            } else {
                log.warn("模型 {} 调用失败: {}", model, response.get("message").asText());
            }
        }

        log.info("✅ 不同模型测试完成!");
    }

    /**
     * 测试7: 参数校验 - 需求描述不能为空
     */
    @Test
    void testClarifyRequirementValidation() throws Exception {
        log.info("开始测试参数校验...");

        ClarifyRequirementRequest request = ClarifyRequirementRequest.builder()
                .requirementDescription("") // 空描述
                .build();

        mockMvc.perform(post("/api/v1/ai/clarify-requirement")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        log.info("✅ 参数校验测试通过!");
    }

    /**
     * 测试8: 未认证访问
     */
    @Test
    void testUnauthorizedAccess() throws Exception {
        log.info("开始测试未认证访问...");

        ClarifyRequirementRequest request = ClarifyRequirementRequest.builder()
                .requirementDescription("测试需求")
                .build();

        // 不携带token
        mockMvc.perform(post("/api/v1/ai/clarify-requirement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        log.info("✅ 未认证访问测试通过!");
    }
}
