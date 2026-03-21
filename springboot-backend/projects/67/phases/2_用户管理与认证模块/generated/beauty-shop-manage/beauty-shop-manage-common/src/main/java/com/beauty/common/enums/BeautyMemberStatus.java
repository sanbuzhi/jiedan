package com.beauty.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 美妆小店会员状态枚举
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
@Getter
@AllArgsConstructor
public enum BeautyMemberStatus {

    /**
     * 正常
     */
    NORMAL(1, "正常"),

    /**
     * 冻结
     */
    FROZEN(0, "冻结");

    private final Integer code;
    private final String desc;

    /**
     * 根据code获取枚举
     *
     * @param code 状态码
     * @return 枚举对象
     */
    public static BeautyMemberStatus getByCode(Integer code) {
        for (BeautyMemberStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

}