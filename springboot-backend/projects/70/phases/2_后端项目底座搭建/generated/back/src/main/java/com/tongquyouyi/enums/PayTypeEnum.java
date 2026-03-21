package com.tongquyouyi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付方式枚举
 */
@Getter
@AllArgsConstructor
public enum PayTypeEnum {

    WECHAT_PAY(1, "微信支付"),
    ALIPAY(2, "支付宝");

    private final Integer code;
    private final String desc;

    /**
     * 根据code获取枚举
     */
    public static PayTypeEnum getByCode(Integer code) {
        for (PayTypeEnum typeEnum : values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}