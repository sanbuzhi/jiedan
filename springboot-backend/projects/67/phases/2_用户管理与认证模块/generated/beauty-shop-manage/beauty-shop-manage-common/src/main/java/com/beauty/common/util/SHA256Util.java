package com.beauty.common.util;

import cn.hutool.crypto.SecureUtil;

/**
 * 美妆小店SHA256加密工具类
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
public class SHA256Util {

    private SHA256Util() {
    }

    /**
     * SHA256加密
     *
     * @param plainText 明文
     * @return 密文
     */
    public static String encrypt(String plainText) {
        return SecureUtil.sha256(plainText);
    }

}