package com.tongquyouyi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云短信配置类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "tongquyouyi.ali.sms")
public class AliSmsProperties {

    /**
     * 访问密钥ID
     */
    private String accessKeyId;

    /**
     * 访问密钥密钥
     */
    private String accessKeySecret;

    /**
     * 短信签名
     */
    private String signName;

    /**
     * 短信模板代码
     */
    private String templateCode;

}