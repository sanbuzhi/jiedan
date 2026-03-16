package com.jiedan.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI聊天响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {

    /**
     * AI生成的内容
     */
    private String content;

    /**
     * 推理内容（如果有）
     */
    private String reasoningContent;

    /**
     * Token使用量
     */
    private AiUsage usage;

    /**
     * 实际使用的模型
     */
    private String model;

    /**
     * 结束原因
     */
    private String finishReason;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息（如果失败）
     */
    private String errorMessage;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTimeMs;

    /**
     * 创建成功响应
     */
    public static AiChatResponse success(String content, String model) {
        return AiChatResponse.builder()
                .content(content)
                .model(model)
                .success(true)
                .build();
    }

    /**
     * 创建失败响应
     */
    public static AiChatResponse error(String errorMessage) {
        return AiChatResponse.builder()
                .errorMessage(errorMessage)
                .success(false)
                .build();
    }
}
