package com.jiedan.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI消息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiMessage {

    /**
     * 角色：system/user/assistant
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 创建系统消息
     */
    public static AiMessage system(String content) {
        return AiMessage.builder()
                .role("system")
                .content(content)
                .build();
    }

    /**
     * 创建用户消息
     */
    public static AiMessage user(String content) {
        return AiMessage.builder()
                .role("user")
                .content(content)
                .build();
    }

    /**
     * 创建助手消息
     */
    public static AiMessage assistant(String content) {
        return AiMessage.builder()
                .role("assistant")
                .content(content)
                .build();
    }
}
