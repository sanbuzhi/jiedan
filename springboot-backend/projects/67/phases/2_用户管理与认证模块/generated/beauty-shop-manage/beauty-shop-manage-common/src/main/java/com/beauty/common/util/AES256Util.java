package com.beauty.common.util;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import com.beauty.common.constant.BeautyAuthConstant;

import java.nio.charset.StandardCharsets;

/**
 * 美妆小店AES256加密工具类
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
public class AES256Util {

    private AES256Util() {
    }

    /**
     * 加密
     *
     * @param plainText 明文
     * @param secretKey 密钥（32位字符串）
     * @return 密文
     */
    public static String encrypt(String plainText, String secretKey) {
        AES aes = SecureUtil.aes(secretKey.getBytes(StandardCharsets.UTF_8), BeautyAuthConstant.BEAUTY_AES256_IV.getBytes(StandardCharsets.UTF_8));
        return aes.encryptHex(plainText);
    }

    /**
     * 解密
     *
     * @param cipherText 密文
     * @param secretKey  密钥（32位字符串）
     * @return 明文
     */
    public static String decrypt(String cipherText, String secretKey) {
        AES aes = SecureUtil.aes(secretKey.getBytes(StandardCharsets.UTF_8), BeautyAuthConstant.BEAUTY_AES256_IV.getBytes(StandardCharsets.UTF_8));
        return aes.decryptStr(cipherText, CharsetUtil.CHARSET_UTF_8);
    }

}