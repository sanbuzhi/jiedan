package com.jiedan.service.ai;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * AI API限流服务
 * 基于令牌桶算法实现多模型限流和降级策略
 */
@Slf4j
@Service
public class AiRateLimiterService {

    // 主模型限流器 (OpenAI GPT-4)
    private final RateLimiter openAiLimiter;
    
    // 备用模型限流器 (Claude)
    private final RateLimiter claudeLimiter;
    
    // 国产模型限流器 (通义千问)
    private final RateLimiter qwenLimiter;
    
    // 用户级别限流器缓存
    private final Map<String, RateLimiter> userLimiters = new ConcurrentHashMap<>();
    
    // 模型健康状态
    private final Map<String, Boolean> modelHealthStatus = new ConcurrentHashMap<>();
    
    // 统计信息
    private final Map<String, RateLimitStats> statsMap = new ConcurrentHashMap<>();

    public AiRateLimiterService() {
        // 初始化主模型限流器: 20 requests per minute (OpenAI限制)
        this.openAiLimiter = RateLimiter.create(20.0 / 60.0);
        
        // 初始化备用模型限流器: 30 requests per minute
        this.claudeLimiter = RateLimiter.create(30.0 / 60.0);
        
        // 初始化国产模型限流器: 60 requests per minute (通义千问较宽松)
        this.qwenLimiter = RateLimiter.create(60.0 / 60.0);
        
        // 初始化健康状态
        modelHealthStatus.put("openai", true);
        modelHealthStatus.put("claude", true);
        modelHealthStatus.put("qwen", true);
        
        // 初始化统计
        statsMap.put("openai", new RateLimitStats());
        statsMap.put("claude", new RateLimitStats());
        statsMap.put("qwen", new RateLimitStats());
    }

    /**
     * 获取可用的AI策略（带限流和降级）
     * 优先级: OpenAI -> Claude -> 通义千问
     */
    public String getAvailableStrategy(String preferredModel) {
        // 1. 尝试首选模型
        if (preferredModel != null && isModelAvailable(preferredModel)) {
            if (tryAcquirePermit(preferredModel)) {
                return preferredModel;
            }
        }
        
        // 2. 尝试OpenAI (主模型)
        if (isModelAvailable("openai") && tryAcquirePermit("openai")) {
            return "openai";
        }
        
        // 3. 尝试Claude (备用模型)
        if (isModelAvailable("claude") && tryAcquirePermit("claude")) {
            return "claude";
        }
        
        // 4. 尝试通义千问 (兜底模型)
        if (isModelAvailable("qwen") && tryAcquirePermit("qwen")) {
            return "qwen";
        }
        
        // 5. 所有模型都不可用，等待主模型
        log.warn("所有AI模型限流，等待主模型可用...");
        return waitForMainModel();
    }

    /**
     * 尝试获取许可（非阻塞）
     */
    public boolean tryAcquirePermit(String model) {
        RateLimiter limiter = getLimiter(model);
        boolean acquired = limiter.tryAcquire(1, 100, TimeUnit.MILLISECONDS);
        
        if (acquired) {
            recordSuccess(model);
        } else {
            recordLimitHit(model);
        }
        
        return acquired;
    }

    /**
     * 获取许可（阻塞等待）
     */
    public void acquirePermit(String model) {
        RateLimiter limiter = getLimiter(model);
        limiter.acquire(1);
        recordSuccess(model);
    }

    /**
     * 检查模型是否可用
     */
    public boolean isModelAvailable(String model) {
        return modelHealthStatus.getOrDefault(model, false);
    }

    /**
     * 设置模型健康状态
     */
    public void setModelHealth(String model, boolean healthy) {
        modelHealthStatus.put(model, healthy);
        log.info("模型健康状态更新: {} = {}", model, healthy);
    }

    /**
     * 获取用户限流器
     * 防止单个用户占用过多资源
     */
    public boolean tryAcquireUserPermit(String userId) {
        RateLimiter userLimiter = userLimiters.computeIfAbsent(userId, 
            k -> RateLimiter.create(10.0 / 60.0)); // 每个用户每分钟10次
        
        return userLimiter.tryAcquire(1, 500, TimeUnit.MILLISECONDS);
    }

    /**
     * 等待主模型可用
     */
    private String waitForMainModel() {
        try {
            // 最多等待30秒
            for (int i = 0; i < 30; i++) {
                if (tryAcquirePermit("openai")) {
                    return "openai";
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 超时后强制返回主模型（可能会失败）
        return "openai";
    }

    /**
     * 获取限流器
     */
    private RateLimiter getLimiter(String model) {
        return switch (model.toLowerCase()) {
            case "openai", "gpt-4", "gpt-3.5" -> openAiLimiter;
            case "claude", "anthropic" -> claudeLimiter;
            case "qwen", "tongyi", "通义千问" -> qwenLimiter;
            default -> openAiLimiter;
        };
    }

    /**
     * 记录成功调用
     */
    private void recordSuccess(String model) {
        RateLimitStats stats = statsMap.get(model);
        if (stats != null) {
            stats.incrementSuccess();
        }
    }

    /**
     * 记录限流命中
     */
    private void recordLimitHit(String model) {
        RateLimitStats stats = statsMap.get(model);
        if (stats != null) {
            stats.incrementLimitHit();
        }
    }

    /**
     * 获取统计信息
     */
    public Map<String, RateLimitStats> getStats() {
        return new ConcurrentHashMap<>(statsMap);
    }

    /**
     * 重置统计
     */
    public void resetStats() {
        statsMap.values().forEach(RateLimitStats::reset);
    }

    // ========== 内部类 ==========

    public static class RateLimitStats {
        private volatile long successCount = 0;
        private volatile long limitHitCount = 0;
        private volatile long lastResetTime = System.currentTimeMillis();

        public void incrementSuccess() {
            successCount++;
        }

        public void incrementLimitHit() {
            limitHitCount++;
        }

        public void reset() {
            successCount = 0;
            limitHitCount = 0;
            lastResetTime = System.currentTimeMillis();
        }

        public long getSuccessCount() {
            return successCount;
        }

        public long getLimitHitCount() {
            return limitHitCount;
        }

        public long getLastResetTime() {
            return lastResetTime;
        }

        public double getLimitHitRate() {
            long total = successCount + limitHitCount;
            return total > 0 ? (double) limitHitCount / total : 0.0;
        }
    }
}
