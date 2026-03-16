package com.jiedan.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 阶段验收请求DTO
 */
@Data
public class ApproveRequest {
    
    /**
     * 阶段名称
     */
    @NotBlank(message = "阶段不能为空")
    private String stage;
    
    /**
     * 项目ID
     */
    private String projectId;
    
    /**
     * 操作类型
     */
    private String action;
}
