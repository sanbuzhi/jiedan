package com.jiedan.dto;

import lombok.Data;

/**
 * 提交建议响应DTO
 */
@Data
public class SuggestionResponse {
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 消息
     */
    private String message;
}
