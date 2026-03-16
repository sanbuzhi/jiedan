package com.jiedan.dto.ai;

import com.jiedan.service.ai.AiRetryService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI功能测试响应DTO
 * 实现AiResult接口以支持重试机制
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunctionalTestResponse implements AiRetryService.AiResult {

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
     * 测试结果摘要
     */
    private String summary;

    /**
     * 测试用例列表
     */
    private List<TestCase> testCases;

    /**
     * 测试覆盖率分析
     */
    private String coverageAnalysis;

    /**
     * 测试代码
     */
    private String testCode;

    /**
     * 测试建议
     */
    private List<String> suggestions;

    /**
     * AI原始响应
     */
    private String rawResponse;

    /**
     * 使用的模型
     */
    private String model;
}
