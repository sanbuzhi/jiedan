package com.jiedan.dto.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI生成代码请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateCodeRequest {

    /**
     * 项目ID（用于Feedback Shadow验证）
     */
    private String projectId;

    /**
     * 任务描述
     */
    @NotBlank(message = "任务描述不能为空")
    private String taskDescription;

    /**
     * 编程语言
     */
    private String language;

    /**
     * 框架/技术栈
     */
    private String framework;

    /**
     * 相关代码上下文
     */
    private String contextCode;

    /**
     * 特殊要求
     */
    private List<String> requirements;

    /**
     * 模型类型（可选）
     */
    private String model;
}
