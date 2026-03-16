package com.jiedan.dto;

import lombok.Data;

/**
 * 阶段验收响应DTO
 */
@Data
public class ApproveResponse {
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 当前流程节点
     */
    private Integer currentFlowNode;
}
