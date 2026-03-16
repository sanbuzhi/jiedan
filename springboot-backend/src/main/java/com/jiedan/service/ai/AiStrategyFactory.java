package com.jiedan.service.ai;

import com.jiedan.dto.ai.AiChatRequest;
import com.jiedan.dto.ai.AiChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI策略工厂（带限流和降级）
 * 根据模型类型动态选择对应的AI策略实现，并应用限流控制
 */
@Slf4j
@Component
public class AiStrategyFactory {

    private final List<AIProviderStrategy> strategies;
    private final AiRateLimiterService rateLimiterService;

    @Autowired
    public AiStrategyFactory(List<AIProviderStrategy> strategies, 
                             AiRateLimiterService rateLimiterService) {
        this.strategies = strategies;
        this.rateLimiterService = rateLimiterService;
        log.info("已加载 {} 个AI策略实现", strategies.size());
        strategies.forEach(s -> log.info("  - {}", s.getProviderName()));
    }

    /**
     * 根据模型类型获取对应的策略实现（带限流和降级）
     *
     * @param modelType 模型类型
     * @return AI策略实现
     * @throws IllegalArgumentException 如果没有找到对应的策略
     */
    public AIProviderStrategy getStrategy(String modelType) {
        // 1. 获取可用的模型（考虑限流和降级）
        String availableModel = rateLimiterService.getAvailableStrategy(modelType);
        
        // 2. 根据可用模型获取策略
        AIProviderStrategy strategy = findStrategyByModel(availableModel);
        
        if (strategy != null) {
            log.debug("使用AI策略: {} (模型: {})", strategy.getProviderName(), availableModel);
            return new RateLimitedAIProviderStrategy(strategy, rateLimiterService, availableModel);
        }

        // 3. 如果没有找到，使用默认策略
        log.warn("未找到模型 {} 的策略，使用默认策略", availableModel);
        return getDefaultStrategy();
    }

    /**
     * 根据模型名称查找策略
     */
    private AIProviderStrategy findStrategyByModel(String model) {
        String normalizedModel = model.toLowerCase();
        
        for (AIProviderStrategy strategy : strategies) {
            if (strategy.supports(normalizedModel)) {
                return strategy;
            }
        }
        
        // 如果没有精确匹配，返回第一个策略作为默认
        return strategies.isEmpty() ? null : strategies.get(0);
    }

    /**
     * 获取默认策略
     */
    private AIProviderStrategy getDefaultStrategy() {
        if (strategies.isEmpty()) {
            throw new IllegalStateException("没有可用的AI策略实现");
        }
        AIProviderStrategy defaultStrategy = strategies.get(0);
        return new RateLimitedAIProviderStrategy(defaultStrategy, rateLimiterService, "default");
    }

    /**
     * 获取所有可用的策略列表
     */
    public List<AIProviderStrategy> getAllStrategies() {
        return List.copyOf(strategies);
    }

    /**
     * 获取限流统计信息
     */
    public java.util.Map<String, AiRateLimiterService.RateLimitStats> getRateLimitStats() {
        return rateLimiterService.getStats();
    }

    // ========== 内部类：带限流的策略包装器 ==========

    /**
     * 带限流控制的AI策略包装器
     */
    public static class RateLimitedAIProviderStrategy implements AIProviderStrategy {
        private final AIProviderStrategy delegate;
        private final AiRateLimiterService rateLimiterService;
        private final String modelName;

        public RateLimitedAIProviderStrategy(AIProviderStrategy delegate,
                                              AiRateLimiterService rateLimiterService,
                                              String modelName) {
            this.delegate = delegate;
            this.rateLimiterService = rateLimiterService;
            this.modelName = modelName;
        }

        @Override
        public AiChatResponse chatCompletion(AiChatRequest request) {
            // 在调用前获取许可
            rateLimiterService.acquirePermit(modelName);
            
            try {
                return delegate.chatCompletion(request);
            } catch (Exception e) {
                // 如果调用失败，标记模型不健康
                if (isRateLimitError(e)) {
                    rateLimiterService.setModelHealth(modelName, false);
                    log.warn("模型 {} 触发限流，标记为不健康", modelName);
                }
                throw e;
            }
        }

        @Override
        public String getProviderName() {
            return delegate.getProviderName();
        }

        @Override
        public boolean supports(String modelType) {
            return delegate.supports(modelType);
        }

        @Override
        public int getPriority() {
            return delegate.getPriority();
        }

        private boolean isRateLimitError(Exception e) {
            String message = e.getMessage();
            return message != null && (
                message.contains("rate limit") ||
                message.contains("RateLimitError") ||
                message.contains("429") ||
                message.contains("Too Many Requests")
            );
        }
    }
}
