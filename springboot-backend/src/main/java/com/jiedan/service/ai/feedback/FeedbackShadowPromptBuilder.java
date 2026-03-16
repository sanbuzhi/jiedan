package com.jiedan.service.ai.feedback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Feedback Shadow Prompt构建器
 * 负责构建检测和修复的Prompt
 * 【修改】简化验证逻辑，只验证文档内容质量，不强制DTO字段
 */
@Slf4j
@Component
public class FeedbackShadowPromptBuilder {

    /**
     * 构建检测Prompt
     */
    public String buildDetectionPrompt(String apiType, String documentContent) {
        StringBuilder prompt = new StringBuilder();

        // System Prompt
        prompt.append("你是一位资深的技术评审专家。你的任务是对AI生成的Markdown文档进行质量检测，并做出放行决策。\n\n");

        prompt.append("【重要说明】\n");
        prompt.append("你是Feedback Shadow系统的核心组件，你的决策将决定文档是否通过检测。\n\n");

        prompt.append("【验证原则 - 重要】\n");
        prompt.append("1. 只验证文档整体质量，不强制要求特定字段\n");
        prompt.append("2. 只要文档结构清晰、内容合理即可通过\n");
        prompt.append("3. 不因为缺少特定章节而拒绝，关注整体完整性\n");
        prompt.append("4. 【放宽标准】轻微问题不拒绝，尽量给出ALLOW或REPAIR\n\n");

        prompt.append("【检测维度】\n");
        prompt.append("1. 文档完整性：是否包含必要的章节（概述、主体内容）\n");
        prompt.append("2. 内容合理性：内容是否详细、可实施\n");
        prompt.append("3. 格式规范性：Markdown格式是否正确\n\n");

        prompt.append("【决策标准 - 放宽】\n");
        prompt.append("- ALLOW（放行）：文档基本完整、内容合理即可，不要求完美\n");
        prompt.append("- REPAIR（需要修复）：只有明显缺陷时才选择，小问题直接ALLOW\n");
        prompt.append("- REJECT（拒绝）：只有文档严重缺失、完全无法使用时才选择\n\n");

        prompt.append("【输出要求】\n");
        prompt.append("1. 使用Markdown格式\n");
        prompt.append("2. 必须包含：\n");
        prompt.append("   - # 质量检测报告（标题）\n");
        prompt.append("   - ## 1. 检测摘要（通过/不通过、总体评价）\n");
        prompt.append("   - ## 2. 详细结果（列出发现的问题）\n");
        prompt.append("   - ## 3. 修复建议（如何改进）\n");
        prompt.append("   - ## 4. 决策建议（ALLOW/REPAIR/REJECT）\n");
        prompt.append("3. 语言：中文\n\n");

        prompt.append("【决策规则 - 重要】\n");
        prompt.append("- 【默认ALLOW】只要文档有基本内容，优先输出ALLOW\n");
        prompt.append("- 【REPAIR】只有明显缺陷（如格式混乱、内容严重缺失）时才选择\n");
        prompt.append("- 【REJECT】只有文档完全空白或完全无法使用时才选择\n");
        prompt.append("- 不强制要求特定字段，关注整体质量\n");
        prompt.append("- 【关键】轻微问题不要拒绝，尽量让文档通过\n\n");

        // 根据apiType添加特定检测重点
        prompt.append(getApiTypeSpecificPrompt(apiType));

        // User Prompt
        prompt.append("请对以下文档进行质量检测：\n\n");
        prompt.append("【文档类型】\n").append(apiType).append("\n\n");
        prompt.append("【文档内容】\n").append(truncateContent(documentContent, 3000)).append("\n\n");
        prompt.append("请输出Markdown格式的质量检测报告，关注文档整体质量。");

        return prompt.toString();
    }

    /**
     * 构建修复Prompt
     */
    public String buildRepairPrompt(String apiType, String originalDocument, String feedbackReport) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("你之前生成的文档未通过质量检测，请根据反馈进行修复。\n\n");

        prompt.append("【原始文档】\n");
        prompt.append(originalDocument).append("\n\n");

        prompt.append("【检测报告】\n");
        prompt.append(feedbackReport).append("\n\n");

        prompt.append("【修复要求】\n");
        prompt.append("1. 根据检测报告中的问题进行修复\n");
        prompt.append("2. 保持文档的Markdown格式\n");
        prompt.append("3. 确保内容完整、详细、可实施\n\n");

        prompt.append("请输出修复后的完整文档。");

        return prompt.toString();
    }

    /**
     * 根据API类型获取特定的检测重点
     * 【简化】只关注文档整体质量，不强制DTO字段
     */
    private String getApiTypeSpecificPrompt(String apiType) {
        switch (apiType) {
            case "clarify-requirement":
                return "【clarify-requirement检测重点 - 放宽】\n" +
                       "参考检查项（不强制）：\n" +
                       "- 是否有需求描述\n" +
                       "- 是否有功能说明\n" +
                       "\n" +
                       "【通过标准 - 放宽】\n" +
                       "只要有基本的需求描述即可ALLOW，不要求完整的章节结构。\n\n";

            case "split-tasks":
                return "【split-tasks检测重点 - 放宽】\n" +
                       "参考检查项（不强制）：\n" +
                       "- 是否有任务说明\n" +
                       "\n" +
                       "【通过标准 - 放宽】\n" +
                       "只要有任务列表或任务说明即可ALLOW，格式不限。\n\n";

            case "generate-code":
                return "【generate-code检测重点 - 放宽】\n" +
                       "参考检查项（不强制）：\n" +
                       "- 是否有代码\n" +
                       "\n" +
                       "【通过标准 - 放宽】\n" +
                       "只要有代码输出即可ALLOW，编译错误在后续阶段处理。\n\n";

            case "functional-test":
                return "【functional-test检测重点 - 放宽】\n" +
                       "参考检查项（不强制）：\n" +
                       "- 是否有测试相关内容\n" +
                       "\n" +
                       "【通过标准 - 放宽】\n" +
                       "只要有测试相关输出即可ALLOW。\n\n";

            case "security-test":
                return "【security-test检测重点 - 放宽】\n" +
                       "参考检查项（不强制）：\n" +
                       "- 是否有安全分析内容\n" +
                       "\n" +
                       "【通过标准 - 放宽】\n" +
                       "只要有安全分析输出即可ALLOW。\n\n";

            default:
                return "";
        }
    }

    /**
     * 截断内容，控制token
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "\n...[内容已截断]";
    }
}
