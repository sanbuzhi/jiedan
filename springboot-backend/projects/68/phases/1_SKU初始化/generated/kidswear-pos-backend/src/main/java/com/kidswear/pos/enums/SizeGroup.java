package com.kidswear.pos.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 童装尺码组枚举
 */
@Getter
public enum SizeGroup {
    INFANT(1, "婴儿装", Arrays.asList("59", "66", "73", "80", "90")),
    TODDLER(2, "幼童装", Arrays.asList("90", "100", "110", "120")),
    CHILDREN(3, "儿童装", Arrays.asList("120", "130", "140", "150", "160")),
    TEEN(4, "大童装", Arrays.asList("160", "165", "170", "175"));

    @EnumValue
    private final Integer code;
    private final String desc;
    private final List<String> defaultSizes;

    SizeGroup(Integer code, String desc, List<String> defaultSizes) {
        this.code = code;
        this.desc = desc;
        this.defaultSizes = defaultSizes;
    }

    /**
     * 根据code获取枚举
     */
    public static SizeGroup getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SizeGroup group : values()) {
            if (group.getCode().equals(code)) {
                return group;
            }
        }
        return null;
    }
}