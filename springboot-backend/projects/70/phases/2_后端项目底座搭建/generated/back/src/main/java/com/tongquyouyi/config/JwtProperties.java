package com.tongquyouyi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "tongquyouyi.jwt")
public class JwtProperties {

    /**
     * 签名密钥
     */
    private String secret;

    /**
     * Token有效期（天）
     */
    private Integer tokenExpireDays;

    /**
     * 刷新Token有效期（天）
     */
    private Integer refreshTokenExpireDays;

    /**
     * Token请求头
     */
    private String header;

    /**
     * Token前缀
     */
    private String prefix;

}