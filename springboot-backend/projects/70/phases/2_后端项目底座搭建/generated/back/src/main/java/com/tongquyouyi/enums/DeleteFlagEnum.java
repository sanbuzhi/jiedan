package com.tongquyouyi.enums;

import lombok.Getter;

/**
 * 逻辑删除枚举
 */
@Getter
public enum DeleteFlagEnum {
    
    /**
     * 未删除
     */
    NOT_DELETED(0, "未删除"),
    
    /**
     * 已删除
     */
    DELETED(1, "已删除");
    
    private final Integer code;
    private final String desc;
    
    DeleteFlagEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    /**
     * 根据code获取枚举
     */
    public static DeleteFlagEnum getByCode(Integer code) {
        for (DeleteFlagEnum deleteFlagEnum : values()) {
            if (deleteFlagEnum.getCode().equals(code)) {
                return deleteFlagEnum;
            }
        }
        return null;
    }
}