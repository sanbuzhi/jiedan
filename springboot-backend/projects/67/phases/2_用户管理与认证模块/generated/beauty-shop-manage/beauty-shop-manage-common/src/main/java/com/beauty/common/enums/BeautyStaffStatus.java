package com.beauty.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 美妆小店员工状态枚举
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
@Getter
@AllArgsConstructor
public enum BeautyStaffStatus {

    /**
     * 正常
     */
    NORMAL(1, "正常"),

    /**
     * 停用
     */
    DISABLED(0, "停用");

    private final Integer code;
    private final String desc;

    /**
     * 根据code获取枚举
     *
     * @param code 状态码
     * @return 枚举对象
     */
    public static BeautyStaffStatus getByCode(Integer code) {
        for (BeautyStaffStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

}