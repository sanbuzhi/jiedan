package com.jiedan.service.ai;

import com.jiedan.dto.ai.AiChatRequest;
import com.jiedan.dto.ai.AiChatResponse;

/**
 * AI服务提供商策略接口
 * 定义AI服务的标准契约，支持多种AI模型提供商的实现
 */
public interface AIProviderStrategy {

    /**
     * 执行聊天补全请求
     *
     * @param request 聊天请求
     * @return 聊天响应
     */
    AiChatResponse chatCompletion(AiChatRequest request);

    /**
     * 获取提供商名称
     *
     * @return 提供商名称
     */
    String getProviderName();

    /**
     * 判断是否支持指定的模型类型
     *
     * @param modelType 模型类型
     * @return 是否支持
     */
    boolean supports(String modelType);

    /**
     * 获取优先级
     *
     * @return 优先级数值，数值越小优先级越高
     */
    default int getPriority() {
        return 100;
    }
}
