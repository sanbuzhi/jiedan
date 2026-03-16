package com.jiedan.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 火山引擎API Demo测试
 * 用于验证火山引擎Coding Plan API的调用方式
 */
@Slf4j
public class HuoshanApiDemoTest {

    private static final String API_KEY = "e492b8fd-34cf-4016-a7bb-f3bb055135bc";
    private static final String BASE_URL = "https://ark.cn-beijing.volces.com/api/coding/v3";
    private static final String MODEL = "doubao-seed-2.0-code";

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
    }

    /**
     * 测试1: 基本的聊天补全API调用
     * 验证OpenAI兼容接口是否可用
     */
    @Test
    void testBasicChatCompletion() throws Exception {
        log.info("开始测试火山引擎API基本调用...");

        // 构建请求URL
        String url = BASE_URL + "/chat/completions";
        log.info("请求URL: {}", url);

        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(API_KEY);

        // 构建请求体（OpenAI兼容格式）
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", MODEL);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", "你是一个专业的软件开发助手，可以帮助用户分析需求、拆分任务、编写代码和进行测试。");
        messages.add(systemMsg);

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", "请简单介绍一下你自己");
        messages.add(userMsg);

        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 500);

        log.info("请求体: {}", objectMapper.writeValueAsString(requestBody));

        // 发送请求
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            log.info("响应状态码: {}", response.getStatusCode());
            log.info("响应体: {}", response.getBody());

            // 验证响应
            assertEquals(200, response.getStatusCode().value(), "API调用应该返回200状态码");
            assertNotNull(response.getBody(), "响应体不应为空");

            // 解析响应JSON
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            assertTrue(jsonResponse.has("choices"), "响应应包含choices字段");
            assertTrue(jsonResponse.get("choices").size() > 0, "choices不应为空");

            JsonNode firstChoice = jsonResponse.get("choices").get(0);
            assertTrue(firstChoice.has("message"), "choice应包含message字段");
            assertTrue(firstChoice.get("message").has("content"), "message应包含content字段");

            String content = firstChoice.get("message").get("content").asText();
            log.info("AI回复内容: {}", content);

            assertNotNull(content, "AI回复内容不应为空");
            assertFalse(content.isEmpty(), "AI回复内容不应为空字符串");

            log.info("✅ 基本聊天补全API测试通过!");

        } catch (Exception e) {
            log.error("❌ API调用失败: {}", e.getMessage(), e);
            fail("火山引擎API调用失败: " + e.getMessage());
        }
    }

    /**
     * 测试2: AI明确需求场景
     * 模拟"AI明确需求"流程节点的调用
     */
    @Test
    void testClarifyRequirement() throws Exception {
        log.info("开始测试AI明确需求场景...");

        String url = BASE_URL + "/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(API_KEY);

        // 构建明确需求的prompt
        String requirementDescription = "我想做一个酒店管理系统，需要管理客房、订单和员工";

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", "你是一位专业的需求分析师，擅长将用户的初步需求转化为详细、完整的需求文档。" +
                "请分析用户的需求描述，输出以下内容：\n" +
                "1. 需求概述\n" +
                "2. 功能模块清单\n" +
                "3. 用户角色定义\n" +
                "4. 核心业务流程\n" +
                "5. 非功能性需求建议");
        messages.add(systemMsg);

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", "用户需求：" + requirementDescription);
        messages.add(userMsg);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", MODEL);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2000);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            assertEquals(200, response.getStatusCode().value());
            assertNotNull(response.getBody());

            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            String content = jsonResponse.get("choices").get(0).get("message").get("content").asText();

            log.info("AI明确需求结果:\n{}", content);

            // 验证返回内容包含关键部分
            assertTrue(content.contains("需求") || content.contains("功能") || content.contains("模块"),
                    "返回内容应包含需求分析相关内容");

            log.info("✅ AI明确需求场景测试通过!");

        } catch (Exception e) {
            log.error("❌ AI明确需求测试失败: {}", e.getMessage(), e);
            fail("AI明确需求测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试3: AI拆分任务场景
     * 模拟"AI拆分任务"流程节点的调用
     */
    @Test
    void testSplitTasks() throws Exception {
        log.info("开始测试AI拆分任务场景...");

        String url = BASE_URL + "/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(API_KEY);

        String requirement = "酒店管理系统：包含客房管理、订单管理、员工管理三个核心模块";

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", "你是一位专业的项目经理，擅长将需求拆分为可执行的具体任务。" +
                "请将以下需求拆分为详细的开发任务列表，每个任务包含：\n" +
                "- 任务名称\n" +
                "- 任务描述\n" +
                "- 预估工时（小时）\n" +
                "- 优先级（高/中/低）\n" +
                "- 依赖任务（如有）");
        messages.add(systemMsg);

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", "需求：" + requirement);
        messages.add(userMsg);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", MODEL);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2000);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            assertEquals(200, response.getStatusCode().value());

            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            String content = jsonResponse.get("choices").get(0).get("message").get("content").asText();

            log.info("AI拆分任务结果:\n{}", content);

            assertTrue(content.contains("任务") || content.contains("工时") || content.contains("优先级"),
                    "返回内容应包含任务拆分相关信息");

            log.info("✅ AI拆分任务场景测试通过!");

        } catch (Exception e) {
            log.error("❌ AI拆分任务测试失败: {}", e.getMessage(), e);
            fail("AI拆分任务测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试4: AI开发场景
     * 模拟"AI开发"流程节点的调用
     */
    @Test
    void testGenerateCode() throws Exception {
        log.info("开始测试AI开发场景...");

        String url = BASE_URL + "/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(API_KEY);

        String task = "创建一个Java类HotelRoom，包含房间号、房型、价格、状态字段，以及相应的getter/setter方法";

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", "你是一位资深的Java开发工程师，擅长编写高质量、规范的代码。" +
                "请根据需求生成完整的Java代码，包含：\n" +
                "1. 完整的类定义\n" +
                "2. 必要的字段和注解\n" +
                "3. 构造方法\n" +
                "4. Getter/Setter方法\n" +
                "5. 必要的业务方法\n" +
                "6. 代码注释");
        messages.add(systemMsg);

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", "开发任务：" + task);
        messages.add(userMsg);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", MODEL);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.3); // 代码生成使用较低temperature
        requestBody.put("max_tokens", 2000);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            assertEquals(200, response.getStatusCode().value());

            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            String content = jsonResponse.get("choices").get(0).get("message").get("content").asText();

            log.info("AI生成代码结果:\n{}", content);

            assertTrue(content.contains("class") || content.contains("public"),
                    "返回内容应包含Java类定义");

            log.info("✅ AI开发场景测试通过!");

        } catch (Exception e) {
            log.error("❌ AI开发测试失败: {}", e.getMessage(), e);
            fail("AI开发测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试5: AI功能测试场景
     * 模拟"AI功能测试"流程节点的调用
     */
    @Test
    void testFunctionalTest() throws Exception {
        log.info("开始测试AI功能测试场景...");

        String url = BASE_URL + "/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(API_KEY);

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

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", "你是一位专业的测试工程师，擅长编写全面的功能测试用例。" +
                "请为提供的代码生成JUnit测试用例，包含：\n" +
                "1. 正常场景测试\n" +
                "2. 边界条件测试\n" +
                "3. 异常场景测试\n" +
                "4. 测试覆盖率分析");
        messages.add(systemMsg);

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", "请为以下代码生成功能测试用例：\n```java\n" + code + "\n```");
        messages.add(userMsg);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", MODEL);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.5);
        requestBody.put("max_tokens", 2000);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            assertEquals(200, response.getStatusCode().value());

            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            String content = jsonResponse.get("choices").get(0).get("message").get("content").asText();

            log.info("AI功能测试结果:\n{}", content);

            assertTrue(content.contains("test") || content.contains("Test") || content.contains("测试"),
                    "返回内容应包含测试相关内容");

            log.info("✅ AI功能测试场景测试通过!");

        } catch (Exception e) {
            log.error("❌ AI功能测试失败: {}", e.getMessage(), e);
            fail("AI功能测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试6: AI安全测试场景
     * 模拟"AI安全测试"流程节点的调用
     */
    @Test
    void testSecurityTest() throws Exception {
        log.info("开始测试AI安全测试场景...");

        String url = BASE_URL + "/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(API_KEY);

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
                    
                    @PostMapping("/users")
                    public User createUser(@RequestBody User user) {
                        return userRepository.save(user);
                    }
                }
                """;

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", "你是一位专业的安全工程师，擅长发现代码中的安全漏洞。" +
                "请对提供的代码进行安全扫描，输出：\n" +
                "1. 发现的漏洞列表（包含漏洞类型、严重程度、位置）\n" +
                "2. 漏洞详细描述\n" +
                "3. 修复建议\n" +
                "4. 安全最佳实践建议");
        messages.add(systemMsg);

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", "请对以下代码进行安全测试：\n```java\n" + code + "\n```");
        messages.add(userMsg);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", MODEL);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.5);
        requestBody.put("max_tokens", 2000);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            assertEquals(200, response.getStatusCode().value());

            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            String content = jsonResponse.get("choices").get(0).get("message").get("content").asText();

            log.info("AI安全测试结果:\n{}", content);

            assertTrue(content.contains("安全") || content.contains("漏洞") || content.contains("风险"),
                    "返回内容应包含安全测试相关内容");

            log.info("✅ AI安全测试场景测试通过!");

        } catch (Exception e) {
            log.error("❌ AI安全测试失败: {}", e.getMessage(), e);
            fail("AI安全测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试7: 不同模型的兼容性测试
     * 测试kimi-k2.5模型
     */
    @Test
    void testDifferentModels() throws Exception {
        log.info("开始测试不同模型...");

        String[] models = {"doubao-seed-2.0-code", "kimi-k2.5"};

        for (String model : models) {
            log.info("测试模型: {}", model);

            String url = BASE_URL + "/chat/completions";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(API_KEY);

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", "你好，请回复'模型测试成功'");
            messages.add(userMsg);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 100);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            try {
                ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
                assertEquals(200, response.getStatusCode().value(),
                        "模型 " + model + " 应该返回200状态码");

                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                String content = jsonResponse.get("choices").get(0).get("message").get("content").asText();

                log.info("模型 {} 响应: {}", model, content);

            } catch (Exception e) {
                log.warn("模型 {} 测试失败: {}", model, e.getMessage());
                // 不失败，只是记录，因为某些模型可能不可用
            }
        }

        log.info("✅ 不同模型兼容性测试完成!");
    }
}
