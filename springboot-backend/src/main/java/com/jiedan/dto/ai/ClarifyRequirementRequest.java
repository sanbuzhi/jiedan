package com.jiedan.dto.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI明确需求请求DTO
 * 【简化】只保留核心字段，直接生成Markdown文档
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClarifyRequirementRequest {

    /**
     * 项目ID（用于Git版本控制）
     */
    @NotBlank(message = "项目ID不能为空")
    private String projectId;

    /**
     * 需求描述（用户输入）
     */
    @NotBlank(message = "需求描述不能为空")
    private String requirementDescription;
}
