package com.tongquyouyi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝配置类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "tongquyouyi.alipay.pay")
public class AlipayProperties {

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 应用私钥
     */
    private String privateKey;

    /**
     * 支付宝公钥
     */
    private String publicKey;

    /**
     * 支付宝网关
     */
    private String gatewayUrl;

    /**
     * 支付结果异步通知地址
     */
    private String notifyUrl;

    /**
     * 支付结果同步跳转地址
     */
    private String returnUrl;

    /**
     * 是否沙箱环境
     */
    private Boolean sandbox;

}