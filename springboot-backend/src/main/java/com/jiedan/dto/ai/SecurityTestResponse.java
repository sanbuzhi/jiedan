package com.jiedan.dto.ai;

import com.jiedan.service.ai.AiRetryService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI安全测试响应DTO
 * 实现AiResult接口以支持重试机制
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityTestResponse implements AiRetryService.AiResult {

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
     * 安全测试报告摘要
     */
    private String summary;

    /**
     * 漏洞列表
     */
    private List<SecurityVulnerability> vulnerabilities;

    /**
     * 漏洞总数
     */
    private Integer totalVulnerabilities;

    /**
     * 高风险漏洞数
     */
    private Integer highRiskCount;

    /**
     * 中风险漏洞数
     */
    private Integer mediumRiskCount;

    /**
     * 低风险漏洞数
     */
    private Integer lowRiskCount;

    /**
     * 安全最佳实践建议
     */
    private List<String> bestPractices;

    /**
     * AI原始响应
     */
    private String rawResponse;

    /**
     * 使用的模型
     */
    private String model;
}
