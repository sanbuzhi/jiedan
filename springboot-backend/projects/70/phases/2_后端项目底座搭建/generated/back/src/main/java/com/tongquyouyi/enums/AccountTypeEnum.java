package com.tongquyouyi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账号类型枚举
 */
@Getter
@AllArgsConstructor
public enum AccountTypeEnum {

    PHONE(1, "手机号"),
    EMAIL(2, "邮箱"),
    WECHAT(3, "微信"),
    ALIPAY(4, "支付宝");

    private final Integer code;
    private final String desc;

    /**
     * 根据code获取枚举
     */
    public static AccountTypeEnum getByCode(Integer code) {
        for (AccountTypeEnum typeEnum : values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}