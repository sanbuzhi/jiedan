package com.beauty.common.constant;

/**
 * 美妆小店认证相关常量
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
public interface BeautyAuthConstant {

    /**
     * JWT令牌前缀
     */
    String BEAUTY_JWT_TOKEN_PREFIX = "Bearer ";

    /**
     * JWT请求头名称
     */
    String BEAUTY_JWT_HEADER_NAME = "Authorization";

    /**
     * JWT有效期（天）
     */
    Integer BEAUTY_JWT_EXPIRE_DAYS = 7;

    /**
     * Refresh JWT有效期（天）
     */
    Integer BEAUTY_REFRESH_JWT_EXPIRE_DAYS = 30;

    /**
     * JWT黑名单Redis前缀
     */
    String BEAUTY_JWT_BLACKLIST_REDIS_PREFIX = "beauty:jwt:blacklist:";

    /**
     * Refresh JWT Redis前缀
     */
    String BEAUTY_REFRESH_JWT_REDIS_PREFIX = "beauty:refresh:jwt:";

    /**
     * AES256加密初始向量
     */
    String BEAUTY_AES256_IV = "BEAUTY_SHOP_IV_16";

    /**
     * 员工初始密码AES256加密前值
     */
    String BEAUTY_STAFF_INITIAL_PASSWORD = "Admin@123";

    /**
     * 员工首次登录强制修改密码标记Redis前缀
     */
    String BEAUTY_STAFF_FIRST_LOGIN_REDIS_PREFIX = "beauty:staff:first:login:";

    /**
     * 员工登录验证码Redis前缀
     */
    String BEAUTY_STAFF_LOGIN_CAPTCHA_REDIS_PREFIX = "beauty:staff:login:captcha:";

    /**
     * 员工登录验证码有效期（秒）
     */
    Integer BEAUTY_STAFF_LOGIN_CAPTCHA_EXPIRE_SECONDS = 300;

}