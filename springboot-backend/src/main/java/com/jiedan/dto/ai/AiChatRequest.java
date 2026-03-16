package com.jiedan.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * AI聊天请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequest {

    /**
     * 模型名称
     */
    private String model;

    /**
     * 消息列表
     */
    @Builder.Default
    private List<AiMessage> messages = new ArrayList<>();

    /**
     * 温度参数（0-2），控制随机性
     */
    private Double temperature;

    /**
     * 最大生成token数
     */
    private Integer maxTokens;

    /**
     * 是否流式输出
     */
    @Builder.Default
    private Boolean stream = false;

    /**
     * 添加系统消息
     */
    public AiChatRequest addSystemMessage(String content) {
        messages.add(AiMessage.system(content));
        return this;
    }

    /**
     * 添加用户消息
     */
    public AiChatRequest addUserMessage(String content) {
        messages.add(AiMessage.user(content));
        return this;
    }

    /**
     * 添加助手消息
     */
    public AiChatRequest addAssistantMessage(String content) {
        messages.add(AiMessage.assistant(content));
        return this;
    }
}
