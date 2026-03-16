package com.jiedan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 推荐统计数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReferralStats {
    private int level1; // 一级推荐数
    private int level2; // 二级推荐数
    private int level3; // 三级推荐数
    
    public int getTotal() {
        return level1 + level2 + level3;
    }
}