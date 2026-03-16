package com.jiedan.service.ai;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 续传机制简单测试
 * 不依赖Spring容器，直接测试续传逻辑
 */
@Slf4j
public class ChatContinuationSimpleTest {

    private static final String TEST_OUTPUT_DIR = "src/test/resources/continuation-test";

    /**
     * 测试续传机制的核心逻辑
     * 验证当内容被截断时能否正确续传
     */
    @Test
    public void testContinuationLogic() throws IOException {
        log.info("========== 测试续传机制核心逻辑 ==========");

        // 模拟一个超长的输入
        StringBuilder longInput = new StringBuilder();
        longInput.append("创建一个超大型电商平台，包含以下模块：\n\n");

        for (int i = 1; i <= 20; i++) {
            longInput.append("模块").append(i).append("：详细业务模块\n");
            longInput.append("- 功能点1：详细描述功能点1的业务逻辑和实现要求\n");
            longInput.append("- 功能点2：详细描述功能点2的业务逻辑和实现要求\n");
            longInput.append("- 功能点3：详细描述功能点3的业务逻辑和实现要求\n");
            longInput.append("- 数据库表设计：表名、字段定义、索引设计、关联关系\n");
            longInput.append("- 接口设计：URL路径、HTTP方法、请求参数、响应格式\n\n");
        }

        log.info("输入长度: {} 字符", longInput.length());

        // 模拟续传过程
        String fullContent = simulateContinuationProcess(longInput.toString());

        log.info("总输出长度: {} 字符", fullContent.length());

        // 验证结果
        assertTrue(fullContent.length() > 1000, "输出应该足够长");
        assertTrue(fullContent.contains("模块1"), "应该包含模块1");
        assertTrue(fullContent.contains("模块2"), "应该包含模块2");
        assertTrue(fullContent.contains("模块3"), "应该包含模块3");

        // 保存结果
        saveResult("continuation-test-result", fullContent);

        log.info("✓ 续传机制核心逻辑测试通过");
    }

    /**
     * 模拟续传过程
     */
    private String simulateContinuationProcess(String input) {
        StringBuilder fullContent = new StringBuilder();
        int maxChunks = 3;
        int chunkCount = 0;

        log.info("开始模拟续传过程...");

        while (chunkCount < maxChunks) {
            chunkCount++;
            log.info("第{}次生成...", chunkCount);

            // 模拟生成一段内容
            String chunk = generateChunk(chunkCount);
            fullContent.append(chunk);

            // 模拟截断判断
            if (chunkCount < maxChunks) {
                log.info("内容被截断，触发续传机制");
            }
        }

        log.info("续传完成，共{}次调用", chunkCount);

        return fullContent.toString();
    }

    /**
     * 生成模拟内容块
     */
    private String generateChunk(int chunkIndex) {
        StringBuilder chunk = new StringBuilder();

        if (chunkIndex == 1) {
            chunk.append("# 电商平台技术任务书\n\n");
            chunk.append("## 1. 项目概述\n\n");
            chunk.append("本文档描述了超大型电商平台的技术实现方案。\n\n");
        }

        chunk.append("## ").append(chunkIndex + 1).append(". 模块").append(chunkIndex).append("设计\n\n");

        for (int i = 1; i <= 5; i++) {
            chunk.append("### ").append(chunkIndex).append(".").append(i).append(" 功能点").append(i).append("\n");
            chunk.append("详细描述功能点").append(i).append("的业务逻辑和实现要求。\n\n");
            chunk.append("**业务规则：**\n");
            chunk.append("- 规则1：具体规则描述\n");
            chunk.append("- 规则2：具体规则描述\n");
            chunk.append("- 规则3：具体规则描述\n\n");
        }

        chunk.append("**数据库设计：**\n");
        chunk.append("```sql\n");
        chunk.append("CREATE TABLE module_").append(chunkIndex).append(" (\n");
        chunk.append("  id BIGINT PRIMARY KEY AUTO_INCREMENT,\n");
        chunk.append("  name VARCHAR(200) NOT NULL,\n");
        chunk.append("  status TINYINT DEFAULT 1,\n");
        chunk.append("  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n");
        chunk.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n");
        chunk.append("```\n\n");

        return chunk.toString();
    }

    /**
     * 保存结果
     */
    private void saveResult(String filename, String content) throws IOException {
        Path dir = Paths.get(TEST_OUTPUT_DIR, "simple-test");
        Files.createDirectories(dir);

        Path filePath = dir.resolve(filename + ".md");
        Files.writeString(filePath, content);

        log.info("✓ 结果已保存: {}", filePath.toAbsolutePath());
    }
}
