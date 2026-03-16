package com.jiedan.dto.ai.feedback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户反馈请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFeedbackRequest {

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 任务ID（可选，如果针对具体任务）
     */
    private String taskId;

    /**
     * 反馈类型：CODE_ISSUE/DESIGN_ISSUE/FUNCTION_MISSING/OTHER
     */
    private String feedbackType;

    /**
     * 问题描述
     */
    private String description;

    /**
     * 问题文件路径列表
     */
    private List<String> affectedFiles;

    /**
     * 期望的修复方式
     */
    private String expectedFix;

    /**
     * 严重程度：CRITICAL/HIGH/MEDIUM/LOW
     */
    private String severity;

    /**
     * 反馈来源：USER/TEST/AUTO
     */
    private String source;
}
