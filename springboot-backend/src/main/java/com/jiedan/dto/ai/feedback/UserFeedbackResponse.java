package com.jiedan.dto.ai.feedback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户反馈响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFeedbackResponse {

    /**
     * 是否成功接收反馈
     */
    private boolean success;

    /**
     * 反馈ID
     */
    private String feedbackId;

    /**
     * 处理状态：PENDING/PROCESSING/COMPLETED/FAILED
     */
    private String status;

    /**
     * 预计修复时间（分钟）
     */
    private Integer estimatedRepairTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建成功响应
     */
    public static UserFeedbackResponse success(String feedbackId, String status) {
        return UserFeedbackResponse.builder()
                .success(true)
                .feedbackId(feedbackId)
                .status(status)
                .estimatedRepairTime(5)
                .build();
    }

    /**
     * 创建失败响应
     */
    public static UserFeedbackResponse fail(String errorMessage) {
        return UserFeedbackResponse.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
