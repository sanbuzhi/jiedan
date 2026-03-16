package com.jiedan.dto.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI安全测试请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityTestRequest {

    /**
     * 代码内容
     */
    @NotBlank(message = "代码内容不能为空")
    private String code;

    /**
     * 编程语言
     */
    private String language;

    /**
     * 应用类型
     */
    private String applicationType;

    /**
     * 模型类型（可选）
     */
    private String model;

    /**
     * 项目ID（用于Git版本控制）
     */
    private String projectId;
}
