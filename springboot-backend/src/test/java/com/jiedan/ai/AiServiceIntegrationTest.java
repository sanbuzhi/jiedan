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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AI服务完整流程集成测试
 * 测试5个AI接口的衔接，记录响应时间和token消耗
 * 【更新】适配简化后的DTO结构
 */
@Slf4j
public class AiServiceIntegrationTest {

    private static AiService aiService;
    private static long startTime;
    private static int totalTokens = 0;

    @BeforeAll
    static void setUp() {
        HuoshanAiProperties properties = new HuoshanAiProperties();
        properties.setApiKey("e492b8fd-34cf-4016-a7bb-f3bb055135bc");
        properties.setBaseUrl("https://ark.cn-beijing.volces.com/api/coding/v3");
        properties.setDefaultModel("doubao-seed-2.0-code");
        properties.setTimeout(30000);

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        HuoshanAiStrategy huoshanStrategy = new HuoshanAiStrategy(properties, restTemplate, objectMapper);
        AiRateLimiterService rateLimiterService = new AiRateLimiterService();
        AiStrategyFactory factory = new AiStrategyFactory(Collections.singletonList(huoshanStrategy), rateLimiterService);
        GitVersionControlService gitService = new GitVersionControlService();
        FeedbackShadowService feedbackShadowService = Mockito.mock(FeedbackShadowService.class);
        AiRetryService aiRetryService = new AiRetryService(feedbackShadowService);
        aiService = new AiService(factory, gitService, feedbackShadowService, aiRetryService);

        log.info("AI服务初始化完成");
    }

    @Test
    void testCompleteAiWorkflow() {
        log.info("========================================");
        log.info("开始AI完整流程测试");
        log.info("========================================");

        // 步骤1: AI明确需求
        String requirementDoc = testClarifyRequirement();

        // 步骤2: AI拆分任务（使用需求文档作为输入）
        String taskList = testSplitTasks(requirementDoc);

        // 步骤3: AI开发（使用任务列表中的核心任务作为输入）
        String generatedCode = testGenerateCode(taskList);

        // 步骤4: AI功能测试（使用生成的代码作为输入）
        String testCases = testFunctionalTest(generatedCode);

        // 步骤5: AI安全测试（使用生成的代码作为输入）
        String securityReport = testSecurityTest(generatedCode);

        // 汇总报告
        log.info("========================================");
        log.info("AI完整流程测试完成");
        log.info("总Token消耗: {}", totalTokens);
        log.info("========================================");
    }

    /**
     * 步骤1: AI明确需求
     */
    private String testClarifyRequirement() {
        log.info("\n【步骤1】AI明确需求");
        log.info("输入: 原始需求描述");
        startTimer();

        // 【更新】使用简化后的DTO
        ClarifyRequirementRequest request = ClarifyRequirementRequest.builder()
                .projectId("test-project-001")
                .requirementDescription("我想做一个酒店管理系统，需要管理客房、订单和员工")
                .build();

        ClarifyRequirementResponse response = aiService.clarifyRequirement(request);

        long duration = stopTimer();
        int tokens = extractTokens(response.getDocumentContent());
        totalTokens += tokens;

        assertNotNull(response.getDocumentContent());
        assertFalse(response.getDocumentContent().isEmpty());

        log.info("✅ 通过 | 耗时: {}ms | Token: {}", duration, tokens);
        log.info("输出预览: {}...", response.getDocumentContent().substring(0,
                Math.min(200, response.getDocumentContent().length())));

        return response.getDocumentContent();
    }

    /**
     * 步骤2: AI拆分任务
     * 输入: 步骤1的需求文档
     */
    private String testSplitTasks(String requirementDoc) {
        log.info("\n【步骤2】AI拆分任务");
        log.info("输入: 需求文档（长度: {}）", requirementDoc.length());
        startTimer();

        // 【更新】使用简化后的DTO，直接传入需求文档
        SplitTasksRequest request = SplitTasksRequest.builder()
                .projectId("test-project-001")
                .requirementDoc(requirementDoc)
                .build();

        SplitTasksResponse response = aiService.splitTasks(request);

        long duration = stopTimer();
        int tokens = extractTokens(response.getDocumentContent());
        totalTokens += tokens;

        assertNotNull(response.getDocumentContent());
        assertFalse(response.getDocumentContent().isEmpty());

        log.info("✅ 通过 | 耗时: {}ms | Token: {}", duration, tokens);
        log.info("输出预览: {}...", response.getDocumentContent().substring(0,
                Math.min(200, response.getDocumentContent().length())));

        return response.getDocumentContent();
    }

    /**
     * 步骤3: AI开发
     * 输入: 步骤2的任务列表中提取的核心开发任务
     */
    private String testGenerateCode(String taskList) {
        log.info("\n【步骤3】AI开发");

        // 从任务列表中提取核心开发任务描述
        String coreTask = extractCoreDevelopmentTask(taskList);
        log.info("输入: 核心开发任务 - {}", coreTask);

        startTimer();

        GenerateCodeRequest request = GenerateCodeRequest.builder()
                .projectId("test-project-001")
                .taskDescription(coreTask)
                .language("Java")
                .framework("Spring Boot")
                .build();

        GenerateCodeResponse response = aiService.generateCode(request);

        long duration = stopTimer();
        int tokens = extractTokens(response.getCode());
        totalTokens += tokens;

        assertNotNull(response.getCode());
        assertFalse(response.getCode().isEmpty());
        assertTrue(response.getCode().contains("class") || response.getCode().contains("public"));

        log.info("✅ 通过 | 耗时: {}ms | Token: {}", duration, tokens);
        log.info("输出代码长度: {} 字符", response.getCode().length());

        return response.getCode();
    }

    /**
     * 步骤4: AI功能测试
     * 输入: 步骤3生成的代码
     */
    private String testFunctionalTest(String generatedCode) {
        log.info("\n【步骤4】AI功能测试");
        log.info("输入: 生成的代码（长度: {}）", generatedCode.length());
        startTimer();

        // 提取代码中的核心类和方法描述
        String codeSummary = extractCodeSummary(generatedCode);

        FunctionalTestRequest request = FunctionalTestRequest.builder()
                .projectId("test-project-001")
                .code(generatedCode)
                .language("Java")
                .functionDescription(codeSummary)
                .build();

        FunctionalTestResponse response = aiService.functionalTest(request);

        long duration = stopTimer();
        int tokens = extractTokens(response.getTestCode());
        totalTokens += tokens;

        assertNotNull(response.getTestCode());
        assertFalse(response.getTestCode().isEmpty());

        log.info("✅ 通过 | 耗时: {}ms | Token: {}", duration, tokens);
        log.info("输出测试代码长度: {} 字符", response.getTestCode().length());

        return response.getTestCode();
    }

    /**
     * 步骤5: AI安全测试
     * 输入: 步骤3生成的代码
     */
    private String testSecurityTest(String generatedCode) {
        log.info("\n【步骤5】AI安全测试");
        log.info("输入: 生成的代码（长度: {}）", generatedCode.length());
        startTimer();

        SecurityTestRequest request = SecurityTestRequest.builder()
                .projectId("test-project-001")
                .code(generatedCode)
                .language("Java")
                .applicationType("Web")
                .build();

        SecurityTestResponse response = aiService.securityTest(request);

        long duration = stopTimer();
        int tokens = extractTokens(response.getRawResponse());
        totalTokens += tokens;

        assertNotNull(response.getRawResponse());
        assertFalse(response.getRawResponse().isEmpty());

        log.info("✅ 通过 | 耗时: {}ms | Token: {}", duration, tokens);
        log.info("安全报告长度: {} 字符", response.getRawResponse().length());

        return response.getRawResponse();
    }

    // ==================== 辅助方法 ====================

    private void startTimer() {
        startTime = System.currentTimeMillis();
    }

    private long stopTimer() {
        return System.currentTimeMillis() - startTime;
    }

    private int extractTokens(String text) {
        if (text == null) return 0;
        // 估算token数：中文字符 + 英文单词
        int chineseChars = text.replaceAll("[^\\u4e00-\\u9fa5]", "").length();
        int englishWords = text.split("\\s+").length;
        return chineseChars + englishWords;
    }

    /**
     * 从任务列表中提取核心开发任务
     */
    private String extractCoreDevelopmentTask(String taskList) {
        // 查找包含"实体类"、"Controller"、"Service"等关键词的任务
        Pattern pattern = Pattern.compile("(创建|开发|实现).*?(实体类|Controller|Service|DAO|Repository|Java类)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(taskList);

        if (matcher.find()) {
            return matcher.group(0) + " - 使用Spring Boot框架，包含完整的字段定义、注解、业务方法";
        }

        // 默认返回一个通用的开发任务
        return "创建酒店管理系统核心实体类HotelRoom，包含房间号、房型、价格、状态字段，以及完整的CRUD操作和业务方法";
    }

    /**
     * 从代码中提取摘要描述
     */
    private String extractCodeSummary(String code) {
        // 提取类名
        Pattern classPattern = Pattern.compile("class\\s+(\\w+)");
        Matcher classMatcher = classPattern.matcher(code);
        String className = classMatcher.find() ? classMatcher.group(1) : "HotelRoom";

        // 提取主要方法
        Pattern methodPattern = Pattern.compile("(public|private)\\s+\\w+\\s+(\\w+)\\s*\\(");
        Matcher methodMatcher = methodPattern.matcher(code);
        StringBuilder methods = new StringBuilder();
        int count = 0;
        while (methodMatcher.find() && count < 5) {
            methods.append(methodMatcher.group(2)).append(", ");
            count++;
        }

        return className + "类，包含方法：" + methods.toString();
    }
}
