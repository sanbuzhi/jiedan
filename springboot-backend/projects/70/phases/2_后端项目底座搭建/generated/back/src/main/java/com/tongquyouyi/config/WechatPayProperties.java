package com.tongquyouyi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信支付V3配置类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "tongquyouyi.wechat.pay")
public class WechatPayProperties {

    /**
     * 小程序AppID
     */
    private String appId;

    /**
     * 商户号
     */
    private String mchId;

    /**
     * API V3密钥
     */
    private String apiV3Key;

    /**
     * 商户私钥路径
     */
    private String privateKeyPath;

    /**
     * 商户证书序列号
     */
    private String merchantSerialNumber;

    /**
     * 支付结果通知地址
     */
    private String notifyUrl;

    /**
     * 是否沙箱环境
     */
    private Boolean sandbox;

}