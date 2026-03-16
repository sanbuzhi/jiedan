package com.jiedan.service.ai;

import com.jiedan.dto.ai.ClarifyRequirementRequest;
import com.jiedan.dto.ai.ClarifyRequirementResponse;
import com.jiedan.dto.ai.SplitTasksRequest;
import com.jiedan.dto.ai.SplitTasksResponse;
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
 * 续传机制真实AI测试
 * 以"美妆店后台管理系统"为例，使用真实AI服务验证续传机制的可行性
 * 
 * 运行前请确保：
 * 1. AI服务配置正确（application.yml中的火山引擎配置）
 * 2. 网络连接正常
 * 3. 有足够的API调用额度
 */
@Slf4j
@SpringBootTest
public class ChatContinuationRealTest {

    @Autowired
    private AiService aiService;

    private static final String TEST_PROJECT_ID = "test-beauty-shop-" + System.currentTimeMillis();
    private static final String TEST_OUTPUT_DIR = "src/test/resources/continuation-test";

    /**
     * 完整流程测试：从需求明确到任务拆分
     * 验证续传机制是否能生成完整的文档
     */
    @Test
    public void testFullAIFlowWithContinuation() throws IOException {
        log.info("========== 开始续传机制真实AI测试 ==========");
        log.info("测试项目ID: {}", TEST_PROJECT_ID);

        // 步骤1: AI明确需求
        String requirementDoc = testClarifyRequirement();

        // 步骤2: AI拆分任务
        String taskDoc = testSplitTasks(requirementDoc);

        // 步骤3: 验证文档完整性
        validateDocumentCompleteness(requirementDoc, taskDoc);

        log.info("========== 续传机制真实AI测试完成 ==========");
        log.info("测试结果保存在: {}", Paths.get(TEST_OUTPUT_DIR, TEST_PROJECT_ID).toAbsolutePath());
    }

    /**
     * 测试AI明确需求
     * 输入：美妆店后台管理系统的详细需求
     * 预期：生成完整的需求文档，不会被截断
     */
    private String testClarifyRequirement() throws IOException {
        log.info("\n【步骤1】AI明确需求...");

        ClarifyRequirementRequest request = new ClarifyRequirementRequest();
        request.setProjectId(TEST_PROJECT_ID);
        request.setRequirementDescription("""
                做一个美妆店后台管理系统，需要包含以下功能：
                
                1. 商品管理
                   - 商品CRUD（名称、品牌、类别、价格、库存、图片、描述）
                   - 商品分类管理（护肤品、彩妆、香水、工具等）
                   - 库存预警（低于阈值自动提醒）
                   - 商品上下架
                   - SKU管理（规格、颜色、尺码等）
                   - 商品标签（新品、热销、推荐）
                
                2. 订单管理
                   - 订单列表（待付款、待发货、已发货、已完成、已取消）
                   - 订单详情（商品信息、收货地址、支付信息、物流信息）
                   - 发货处理（填写物流单号、选择物流公司）
                   - 退款/售后处理
                   - 订单导出Excel
                   - 批量发货
                
                3. 会员管理
                   - 会员列表（等级、积分、消费记录）
                   - 会员等级设置（普通、银卡、金卡、钻石）
                   - 积分规则设置（消费积分、签到积分、分享积分）
                   - 会员标签管理
                   - 会员黑名单
                
                4. 营销管理
                   - 优惠券管理（满减券、折扣券、无门槛券）
                   - 满减活动（满100减10、满200减30等）
                   - 限时秒杀
                   - 拼团活动
                   - 分销管理（分销员、佣金设置）
                
                5. 数据统计
                   - 销售报表（日/周/月销售额、订单量）
                   - 商品报表（热销榜、库存报表）
                   - 会员报表（新增会员、活跃会员、消费分析）
                   - 财务报表（收入、支出、利润）
                   - 数据可视化图表
                
                6. 系统设置
                   - 店铺信息设置（名称、Logo、联系方式）
                   - 轮播图管理
                   - 公告管理
                   - 员工账号管理（角色、权限）
                   - 操作日志
                
                7. 内容管理
                   - 文章管理（美妆教程、产品评测）
                   - 图片库管理
                   - 视频管理
                
                技术栈：Spring Boot + Vue3 + MySQL + Redis + Elasticsearch
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
     * 输入：详细的需求文档
     * 预期：生成完整的任务书，包含前端、后端、数据库设计
     */
    private String testSplitTasks(String requirementDoc) throws IOException {
        log.info("\n【步骤2】AI拆分任务...");

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
        log.info("✓ 需求文档完整性检查通过，长度: {} 字符", requirementDoc.length());

        // 验证任务文档
        assertTrue(taskDoc.length() > 3000,
                "任务文档应该足够详细（至少3000字符），实际: " + taskDoc.length());
        log.info("✓ 任务文档长度检查通过，长度: {} 字符", taskDoc.length());

        // 检查任务文档是否被截断（以不完整的句子结尾）
        String lastLine = getLastNonEmptyLine(taskDoc);
        boolean isTruncated = isIncompleteLine(lastLine);
        if (isTruncated) {
            log.warn("⚠ 任务文档可能以不完整的句子结尾: {}", lastLine);
        } else {
            log.info("✓ 任务文档完整性检查通过，未被截断");
        }

        // 验证任务书内容
        assertTrue(taskDoc.contains("前端") || taskDoc.contains("页面"),
                "任务文档应包含前端页面开发清单");
        assertTrue(taskDoc.contains("后端") || taskDoc.contains("接口"),
                "任务文档应包含后端接口开发清单");
        assertTrue(taskDoc.contains("数据库"),
                "任务文档应包含数据库设计");
        log.info("✓ 任务文档内容检查通过");
    }

    /**
     * 保存文档到测试目录
     */
    private void saveDocument(String type, String content) throws IOException {
        Path testDir = Paths.get(TEST_OUTPUT_DIR, TEST_PROJECT_ID);
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
                lastChar == ')' || lastChar == ']' || lastChar == '}' ||
                lastChar == '"' || lastChar == '\'');
    }
}
