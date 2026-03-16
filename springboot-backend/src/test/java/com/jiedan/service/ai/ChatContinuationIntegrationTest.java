package com.jiedan.service.ai;

import com.jiedan.dto.ai.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 续传机制集成测试
 * 以"美妆店后台管理系统"为例，完整验证整个AI流程
 */
@Slf4j
@SpringBootTest
public class ChatContinuationIntegrationTest {

    @Autowired
    private AiService aiService;

    private static final String TEST_PROJECT_ID = "test-beauty-shop-001";

    /**
     * 完整流程测试：从需求明确到任务拆分
     * 验证续传机制是否能生成完整的文档
     */
    @Test
    public void testFullAIFlowWithContinuation() throws IOException {
        log.info("========== 开始续传机制集成测试 ==========");

        // 步骤1: AI明确需求
        String requirementDoc = testClarifyRequirement();

        // 步骤2: AI拆分任务
        String taskDoc = testSplitTasks(requirementDoc);

        // 步骤3: 验证文档完整性
        validateDocumentCompleteness(requirementDoc, taskDoc);

        log.info("========== 续传机制集成测试完成 ==========");
    }

    /**
     * 测试AI明确需求
     */
    private String testClarifyRequirement() throws IOException {
        log.info("\n【步骤1】测试AI明确需求...");

        ClarifyRequirementRequest request = new ClarifyRequirementRequest();
        request.setProjectId(TEST_PROJECT_ID);
        request.setRequirementDescription("""
                做一个美妆店后台管理系统，需要包含以下功能：
                
                1. 商品管理
                   - 商品CRUD（名称、品牌、类别、价格、库存、图片、描述）
                   - 商品分类管理（护肤品、彩妆、香水、工具等）
                   - 库存预警（低于阈值自动提醒）
                   - 商品上下架
                
                2. 订单管理
                   - 订单列表（待付款、待发货、已发货、已完成、已取消）
                   - 订单详情（商品信息、收货地址、支付信息、物流信息）
                   - 发货处理（填写物流单号、选择物流公司）
                   - 退款/售后处理
                
                3. 会员管理
                   - 会员列表（等级、积分、消费记录）
                   - 会员等级设置（普通、银卡、金卡、钻石）
                   - 积分规则设置
                   - 会员标签管理
                
                4. 营销管理
                   - 优惠券管理（满减券、折扣券、无门槛券）
                   - 满减活动
                   - 限时秒杀
                   - 拼团活动
                
                5. 数据统计
                   - 销售报表（日/周/月销售额、订单量）
                   - 商品报表（热销榜、库存报表）
                   - 会员报表（新增会员、活跃会员、消费分析）
                   - 财务报表（收入、支出、利润）
                
                6. 系统设置
                   - 店铺信息设置
                   - 轮播图管理
                   - 公告管理
                   - 员工账号管理（角色、权限）
                
                技术栈：Spring Boot + Vue3 + MySQL + Redis
                """);

        long startTime = System.currentTimeMillis();
        ClarifyRequirementResponse response = aiService.clarifyRequirement(request);
        long endTime = System.currentTimeMillis();

        assertNotNull(response, "响应不应为空");
        assertNotNull(response.getDocumentContent(), "需求文档不应为空");
        assertFalse(response.getDocumentContent().isEmpty(), "需求文档不应为空字符串");

        String doc = response.getDocumentContent();
        log.info("✓ AI明确需求完成，耗时: {}ms", endTime - startTime);
        log.info("✓ 需求文档长度: {} 字符", doc.length());
        log.info("✓ 包含'需求概述': {}", doc.contains("需求概述"));
        log.info("✓ 包含'功能模块': {}", doc.contains("功能模块"));
        log.info("✓ 包含'数据库设计': {}", doc.contains("数据库"));

        // 保存文档用于检查
        saveDocument("requirement", doc);

        return doc;
    }

    /**
     * 测试AI拆分任务
     */
    private String testSplitTasks(String requirementDoc) throws IOException {
        log.info("\n【步骤2】测试AI拆分任务...");

        SplitTasksRequest request = new SplitTasksRequest();
        request.setProjectId(TEST_PROJECT_ID);
        request.setRequirementDoc(requirementDoc);

        long startTime = System.currentTimeMillis();
        SplitTasksResponse response = aiService.splitTasks(request);
        long endTime = System.currentTimeMillis();

        assertNotNull(response, "响应不应为空");
        assertNotNull(response.getDocumentContent(), "任务文档不应为空");
        assertFalse(response.getDocumentContent().isEmpty(), "任务文档不应为空字符串");

        String doc = response.getDocumentContent();
        log.info("✓ AI拆分任务完成，耗时: {}ms", endTime - startTime);
        log.info("✓ 任务文档长度: {} 字符", doc.length());
        log.info("✓ 包含'前端页面': {}", doc.contains("前端") || doc.contains("页面"));
        log.info("✓ 包含'后端接口': {}", doc.contains("后端") || doc.contains("接口"));
        log.info("✓ 包含'数据库': {}", doc.contains("数据库"));

        // 保存文档用于检查
        saveDocument("tasks", doc);

        return doc;
    }

    /**
     * 验证文档完整性
     */
    private void validateDocumentCompleteness(String requirementDoc, String taskDoc) {
        log.info("\n【步骤3】验证文档完整性...");

        // 验证需求文档
        assertTrue(requirementDoc.length() > 2000,
                "需求文档应该足够详细（至少2000字符），实际: " + requirementDoc.length());
        assertTrue(requirementDoc.contains("商品管理") || requirementDoc.contains("订单管理"),
                "需求文档应该包含核心业务模块");

        // 验证任务文档
        assertTrue(taskDoc.length() > 3000,
                "任务文档应该足够详细（至少3000字符），实际: " + taskDoc.length());

        // 检查任务文档是否被截断（以不完整的句子结尾）
        String lastLine = getLastNonEmptyLine(taskDoc);
        assertFalse(isIncompleteLine(lastLine),
                "任务文档不应该以不完整的句子结尾，最后一句: " + lastLine);

        log.info("✓ 需求文档完整性检查通过");
        log.info("✓ 任务文档完整性检查通过");
        log.info("✓ 文档未被截断");
    }

    /**
     * 保存文档到测试目录
     */
    private void saveDocument(String type, String content) throws IOException {
        Path testDir = Paths.get("src/test/resources/continuation-test", TEST_PROJECT_ID);
        Files.createDirectories(testDir);

        Path filePath = testDir.resolve(type + ".md");
        Files.writeString(filePath, content);
        log.info("✓ 文档已保存: {}", filePath.toAbsolutePath());
    }

    /**
     * 获取最后一行非空内容
     */
    private String getLastNonEmptyLine(String content) {
        String[] lines = content.split("\n");
        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i].trim();
            if (!line.isEmpty()) {
                return line;
            }
        }
        return "";
    }

    /**
     * 检查行是否不完整（以标点符号结尾视为完整）
     */
    private boolean isIncompleteLine(String line) {
        if (line.isEmpty()) return false;
        // 如果以这些符号结尾，视为完整
        char lastChar = line.charAt(line.length() - 1);
        return !(lastChar == '。' || lastChar == '；' || lastChar == '！' ||
                lastChar == '？' || lastChar == '.' || lastChar == ';' ||
                lastChar == '!' || lastChar == '?' || lastChar == '`' ||
                lastChar == ')' || lastChar == ']' || lastChar == '}');
    }

    /**
     * 单独测试续传机制
     * 验证当内容被截断时能否正确续传
     */
    @Test
    public void testContinuationMechanism() {
        log.info("\n========== 测试续传机制 ==========");

        // 创建一个超长的需求描述，强制触发续传
        StringBuilder longRequirement = new StringBuilder();
        longRequirement.append("创建一个超大型电商平台，包含以下模块：\n\n");

        // 添加大量模块描述，确保内容足够长
        for (int i = 1; i <= 20; i++) {
            longRequirement.append("模块").append(i).append("：\n");
            longRequirement.append("- 功能点1：详细描述功能点1的业务逻辑和实现要求\n");
            longRequirement.append("- 功能点2：详细描述功能点2的业务逻辑和实现要求\n");
            longRequirement.append("- 功能点3：详细描述功能点3的业务逻辑和实现要求\n");
            longRequirement.append("- 数据库表：表名、字段、索引、关联关系\n");
            longRequirement.append("- 接口设计：URL、请求参数、响应格式\n\n");
        }

        ClarifyRequirementRequest request = new ClarifyRequirementRequest();
        request.setProjectId(TEST_PROJECT_ID + "-long");
        request.setRequirementDescription(longRequirement.toString());

        long startTime = System.currentTimeMillis();
        ClarifyRequirementResponse response = aiService.clarifyRequirement(request);
        long endTime = System.currentTimeMillis();

        assertNotNull(response);
        assertNotNull(response.getDocumentContent());

        String doc = response.getDocumentContent();
        log.info("✓ 超长需求处理完成，耗时: {}ms", endTime - startTime);
        log.info("✓ 生成文档长度: {} 字符", doc.length());

        // 验证文档没有被截断
        assertTrue(doc.length() > 5000, "超长需求的文档应该很长");

        log.info("✓ 续传机制工作正常");
    }
}
