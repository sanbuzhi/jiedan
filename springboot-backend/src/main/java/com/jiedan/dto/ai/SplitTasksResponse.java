package com.jiedan.dto.ai;

import com.jiedan.service.ai.AiRetryService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI拆分任务响应DTO
 * 【简化】只返回文档内容，不解析具体字段
 * 实现AiResult接口以支持重试机制
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SplitTasksResponse implements AiRetryService.AiResult {

    /**
     * 是否成功（Feedback Shadow验证通过）
     */
    private boolean success;

    /**
     * 错误信息（验证失败时）
     */
    private String errorMessage;

    /**
     * 验证决策（ALLOW/REPAIR/REJECT）
     */
    private String validationDecision;

    /**
     * 验证问题列表
     */
    private List<String> validationIssues;

    /**
     * 任务拆分文档内容（Markdown格式）
     * 【简化】直接返回完整的Markdown文档，不解析具体字段
     */
    private String documentContent;

    /**
     * 获取原始响应（用于Feedback Shadow验证）
     * @return documentContent
     */
    public String getRawResponse() {
        return documentContent;
    }
}
