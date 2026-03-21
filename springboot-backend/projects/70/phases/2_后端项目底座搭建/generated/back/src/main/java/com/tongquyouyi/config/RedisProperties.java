package com.tongquyouyi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Redis配置类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "tongquyouyi.redis")
public class RedisProperties {

    /**
     * Redis键前缀
     */
    private String prefix;

    /**
     * 图形验证码有效期（秒）
     */
    private Long captchaExpireSeconds;

    /**
     * 短信验证码有效期（秒）
     */
    private Long smsExpireSeconds;

    /**
     * Token有效期（秒）
     */
    private Long tokenExpireSeconds;

    /**
     * 刷新Token有效期（秒）
     */
    private Long refreshTokenExpireSeconds;

    /**
     * IP防刷时间窗口（秒）
     */
    private Long ipBrushExpireSeconds;

    /**
     * IP防刷最大请求数
     */
    private Integer ipBrushMaxCount;

}