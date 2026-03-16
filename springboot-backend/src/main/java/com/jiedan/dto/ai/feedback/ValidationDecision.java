package com.jiedan.dto.ai.feedback;

/**
 * Feedback Shadow验证决策枚举
 */
public enum ValidationDecision {
    /**
     * 放行：文档质量良好，可以直接使用
     */
    ALLOW,
    
    /**
     * 需要修复：文档有小问题，需要修复后重新提交
     */
    REPAIR,
    
    /**
     * 拒绝：文档有严重问题，无法使用
     */
    REJECT
}
