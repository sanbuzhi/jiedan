package com.jiedan.dto.ai;

import com.jiedan.service.ai.AiRetryService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI生成代码响应DTO
 * 实现AiResult接口以支持重试机制
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateCodeResponse implements AiRetryService.AiResult {

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
     * 生成的代码
     */
    private String code;

    /**
     * 代码说明/注释
     */
    private String explanation;

    /**
     * 文件名称建议
     */
    private String suggestedFileName;

    /**
     * 依赖项列表
     */
    private String dependencies;

    /**
     * AI原始响应
     */
    private String rawResponse;

    /**
     * 使用的模型
     */
    private String model;
}
