package com.kidswear.pos.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * SKU状态枚举
 */
@Getter
public enum SkuStatus {
    NORMAL(1, "正常销售"),
    OFF_SHELF(2, "已下架"),
    OUT_OF_STOCK(3, "缺货"),
    PRE_SALE(4, "预售中");

    @EnumValue
    private final Integer code;
    private final String desc;

    SkuStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     */
    public static SkuStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SkuStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}