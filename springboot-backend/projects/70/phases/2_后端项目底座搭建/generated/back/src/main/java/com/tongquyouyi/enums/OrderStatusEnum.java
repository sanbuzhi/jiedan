package com.tongquyouyi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态枚举
 */
@Getter
@AllArgsConstructor
public enum OrderStatusEnum {

    UNPAID(0, "待支付"),
    PAID(1, "已支付"),
    CANCELLED(2, "已取消"),
    REFUNDED(3, "已退款"),
    COMPLETED(4, "已完成");

    private final Integer code;
    private final String desc;

    /**
     * 根据code获取枚举
     */
    public static OrderStatusEnum getByCode(Integer code) {
        for (OrderStatusEnum statusEnum : values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }
}