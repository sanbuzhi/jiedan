package com.jiedan.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 提交建议请求DTO
 */
@Data
public class SuggestionRequest {
    
    /**
     * 阶段名称
     */
    @NotBlank(message = "阶段不能为空")
    private String stage;
    
    /**
     * 建议内容
     */
    @NotBlank(message = "建议内容不能为空")
    private String suggestion;
}
