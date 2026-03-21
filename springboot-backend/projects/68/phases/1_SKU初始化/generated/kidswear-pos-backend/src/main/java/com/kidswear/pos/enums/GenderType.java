package com.kidswear.pos.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 性别适用枚举
 */
@Getter
public enum GenderType {
    UNISEX(0, "男女通用"),
    BOY(1, "男童"),
    GIRL(2, "女童");

    @EnumValue
    private final Integer code;
    private final String desc;

    GenderType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     */
    public static GenderType getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (GenderType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}