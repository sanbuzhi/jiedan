package com.jiedan.dto.ai.feedback;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Feedback Shadow验证请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackShadowValidateRequest {
    
    /**
     * 项目ID
     */
    @NotBlank(message = "项目ID不能为空")
    private String projectId;
    
    /**
     * 被检测的AI接口类型
     * clarify-requirement/split-tasks/generate-code/functional-test/security-test
     */
    @NotBlank(message = "API类型不能为空")
    private String apiType;
    
    /**
     * 文档路径
     */
    private String documentPath;
    
    /**
     * 文档内容（限制3000字以内）
     */
    private String documentContent;
    
    /**
     * 上次反馈（修复时传入）
     */
    private String previousFeedback;
    
    /**
     * 请求ID（用于追踪）
     */
    private String requestId;
    
    /**
     * 任务ID
     */
    private String taskId;
}
