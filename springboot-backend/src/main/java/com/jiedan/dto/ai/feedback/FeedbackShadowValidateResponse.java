package com.jiedan.dto.ai.feedback;

import com.jiedan.dto.ai.AiUsage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Feedback Shadow验证响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackShadowValidateResponse {
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 请求ID
     */
    private String requestId;
    
    /**
     * 检测报告路径
     */
    private String reportPath;
    
    /**
     * 检测报告内容
     */
    private String reportContent;
    
    /**
     * 决策结果：ALLOW/REPAIR/REJECT
     */
    private ValidationDecision decision;
    
    /**
     * 发现的问题列表
     */
    private List<String> issues;
    
    /**
     * 修复建议
     */
    private String repairSuggestion;
    
    /**
     * AI调用元数据
     */
    private AiUsage usage;
    
    /**
     * 响应时间（毫秒）
     */
    private Long responseTimeMs;
}
