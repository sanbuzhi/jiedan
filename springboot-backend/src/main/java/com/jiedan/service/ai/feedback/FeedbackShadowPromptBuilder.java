package com.jiedan.service.ai.feedback;

import com.jiedan.service.ai.prompt.AiPromptTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Feedback Shadow Prompt构建器
 * 负责构建检测和修复的Prompt
 * 【规范化】使用标准化的Prompt模板
 */
@Slf4j
@Component
public class FeedbackShadowPromptBuilder {

    /**
     * 构建检测Prompt
     * 【规范化】使用AiPromptTemplate中的标准Prompt
     */
    public String buildDetectionPrompt(String apiType, String documentContent) {
        // 使用标准化的System Prompt
        String systemPrompt = AiPromptTemplate.FEEDBACK_SHADOW_SYSTEM;
        
        // 根据apiType添加特定的放行标准
        String specificCriteria = getApiTypeSpecificCriteria(apiType);
        
        // 构建完整的System Prompt
        StringBuilder fullSystemPrompt = new StringBuilder(systemPrompt);
        fullSystemPrompt.append("\n\n【当前检测类型的特定标准】\n");
        fullSystemPrompt.append(specificCriteria);
        
        // 使用标准化的User Prompt
        String userPrompt = AiPromptTemplate.buildFeedbackShadowUserPrompt(apiType, documentContent);
        
        // 组合完整的Prompt
        return fullSystemPrompt.toString() + "\n\n" + userPrompt;
    }

    /**
     * 构建修复Prompt
     * 【规范化】使用标准化的修复Prompt格式
     */
    public String buildRepairPrompt(String apiType, String originalDocument, String feedbackReport) {
        StringBuilder prompt = new StringBuilder();

        // System Prompt
        prompt.append("【角色定义】\n");
        prompt.append("你是一位资深的技术文档专家，擅长根据反馈修复和完善技术文档。\n\n");

        prompt.append("【核心任务】\n");
        prompt.append("根据质量检测报告中的问题和建议，修复原始文档。\n\n");

        prompt.append("【修复原则】\n");
        prompt.append("1. 针对检测报告中列出的每个问题进行修复\n");
        prompt.append("2. 保持文档的整体结构和格式\n");
        prompt.append("3. 确保修复后的文档满足放行标准\n");
        prompt.append("4. 不引入新的问题\n\n");

        // User Prompt
        prompt.append("【原始文档】\n");
        prompt.append(originalDocument).append("\n\n");

        prompt.append("【检测报告】\n");
        prompt.append(feedbackReport).append("\n\n");

        prompt.append("【修复要求】\n");
        prompt.append("1. 根据检测报告中的问题进行修复\n");
        prompt.append("2. 保持文档的Markdown格式\n");
        prompt.append("3. 确保内容完整、详细、可实施\n");
        prompt.append("4. 输出完整的修复后文档，不要只输出修改部分\n\n");

        prompt.append("请输出修复后的完整文档。");

        return prompt.toString();
    }

    /**
     * 根据API类型获取特定的放行标准
     * 【规范化】与AiPromptTemplate中的标准保持一致
     */
    private String getApiTypeSpecificCriteria(String apiType) {
        return switch (apiType) {
            case "clarify-requirement" -> """
                【需求明确阶段 - 放行标准】
                - 必须有需求描述和基本结构
                - 功能点描述清晰无歧义
                - 允许 minor 格式问题
                - 默认 ALLOW，除非完全无法使用
                """;

            case "split-tasks" -> """
                【任务拆分阶段 - 放行标准 - 放宽】
                ✅ 只要包含7个必要章节即可 ALLOW，即使某些章节内容较简略
                ✅ 技术规格章节只要有技术栈和项目结构即可
                ✅ 前端/后端开发清单只要有基本模块划分即可
                ✅ 数据库设计只要有主要表结构即可
                ✅ 允许 minor 的内容缺失或格式问题
                ❌ 只有以下情况才 REJECT：
                   - 完全缺失技术规格章节
                   - 完全缺失前端/后端/数据库任一章节
                   - 输出的是引导性文字而非实际任务书内容
                """;

            case "functional-test", "security-test" -> """
                【测试阶段 - 放行标准】
                - 有测试相关内容即可
                - 默认 ALLOW
                """;

            default -> """
                【通用放行标准】
                - 文档基本完整
                - 内容合理可实施
                - 默认 ALLOW
                """;
        };
    }

}
