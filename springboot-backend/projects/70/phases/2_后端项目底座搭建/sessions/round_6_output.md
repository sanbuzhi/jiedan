===FILE:back/src/main/java/com/tongquyouyi/enums/CaptchaTypeEnum.java===
```java
package com.tongquyouyi.enums;

import lombok.Getter;

/**
 * 验证码类型枚举
 */
@Getter
public enum CaptchaTypeEnum {
    /**
     * 短信验证码
     */
    SMS("sms", "短信验证码"),
    /**
     * 邮箱验证码
     */
    EMAIL("email", "邮箱验证码"),
    /**
     * 图片验证码
     */
    IMAGE("image", "图片验证码");

    private final String code;
    private final String desc;

    CaptchaTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
```