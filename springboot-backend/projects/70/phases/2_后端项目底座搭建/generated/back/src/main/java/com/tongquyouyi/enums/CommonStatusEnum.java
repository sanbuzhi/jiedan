package com.tongquyouyi.enums;

import lombok.Getter;

/**
 * 通用状态枚举
 */
@Getter
public enum CommonStatusEnum {
    
    /**
     * 禁用
     */
    DISABLE(0, "禁用"),
    
    /**
     * 启用
     */
    ENABLE(1, "启用");
    
    private final Integer code;
    private final String desc;
    
    CommonStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    /**
     * 根据code获取枚举
     */
    public static CommonStatusEnum getByCode(Integer code) {
        for (CommonStatusEnum statusEnum : values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }
    
    /**
     * 判断是否存在该code
     */
    public static boolean exists(Integer code) {
        return getByCode(code) != null;
    }
}