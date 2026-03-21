package com.tongquyouyi.utils;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 图形验证码工具类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Component
public class CaptchaUtils {

    /**
     * 生成图形验证码
     *
     * @return 包含captchaKey和captchaImg的Map
     */
    public static Map<String, String> generateCaptcha() {
        // 生成4位数字验证码，宽度120，高度40
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 20);
        Map<String, String> result = new HashMap<>();
        // 使用UUID作为captchaKey
        result.put("captchaKey", cn.hutool.core.lang.UUID.fastUUID().toString(true));
        // 获取Base64格式的验证码图片
        result.put("captchaImg", lineCaptcha.getImageBase64Data());
        // 存储验证码值到Redis的key（这里只返回验证码值，实际存储在Redis由业务层处理）
        result.put("captchaValue", lineCaptcha.getCode());
        return result;
    }

}