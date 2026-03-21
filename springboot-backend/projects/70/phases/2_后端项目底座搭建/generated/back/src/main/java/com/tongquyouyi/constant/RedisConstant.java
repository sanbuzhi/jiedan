package com.tongquyouyi.constant;

/**
 * Redis常量类
 */
public class RedisConstant {
    
    /**
     * 验证码key前缀
     */
    public static final String CAPTCHA_KEY_PREFIX = "captcha:";
    
    /**
     * 短信验证码key前缀
     */
    public static final String SMS_CODE_KEY_PREFIX = "sms:code:";
    
    /**
     * 用户token key前缀
     */
    public static final String USER_TOKEN_KEY_PREFIX = "user:token:";
    
    /**
     * 管理员token key前缀
     */
    public static final String ADMIN_TOKEN_KEY_PREFIX = "admin:token:";
    
    /**
     * 防刷IP key前缀
     */
    public static final String IP_BRUSH_KEY_PREFIX = "ip:brush:";
    
    /**
     * 验证码过期时间（秒）
     */
    public static final long CAPTCHA_EXPIRE_TIME = 300;
    
    /**
     * 短信验证码过期时间（秒）
     */
    public static final long SMS_CODE_EXPIRE_TIME = 300;
    
    /**
     * 用户token过期时间（秒）
     */
    public static final long USER_TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60;
    
    /**
     * 管理员token过期时间（秒）
     */
    public static final long ADMIN_TOKEN_EXPIRE_TIME = 24 * 60 * 60;
    
    /**
     * 防刷IP过期时间（秒）
     */
    public static final long IP_BRUSH_EXPIRE_TIME = 60;
}