package com.tongquyouyi.utils;

import com.tongquyouyi.config.RedisProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Component
public class RedisUtils {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisProperties redisProperties;

    /**
     * 拼接前缀
     */
    private String buildKey(String key) {
        return redisProperties.getPrefix() + key;
    }

    // ====================== 基本操作 ======================

    /**
     * 设置键值对
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(buildKey(key), value);
    }

    /**
     * 设置键值对及过期时间
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(buildKey(key), value, timeout, unit);
    }

    /**
     * 获取键值对
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(buildKey(key));
    }

    /**
     * 删除键
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(buildKey(key));
    }

    /**
     * 批量删除键
     */
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys.stream().map(this::buildKey).toList());
    }

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(buildKey(key), timeout, unit);
    }

    /**
     * 获取过期时间
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(buildKey(key));
    }

    /**
     * 判断键是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(buildKey(key));
    }

    // ====================== 计数器操作 ======================

    /**
     * 递增
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(buildKey(key));
    }

    /**
     * 递增指定值
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(buildKey(key), delta);
    }

    /**
     * 递减
     */
    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(buildKey(key));
    }

    /**
     * 递减指定值
     */
    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(buildKey(key), delta);
    }

}