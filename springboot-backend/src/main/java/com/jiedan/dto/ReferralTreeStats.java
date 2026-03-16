package com.jiedan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 推荐树统计信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReferralTreeStats {
    
    /**
     * 最大层级数
     */
    private int maxLevel;
    
    /**
     * 每级节点数量 [level0, level1, level2, ...]
     */
    private List<Integer> nodesPerLevel;
    
    /**
     * 总节点数
     */
    private int totalNodes;
    
    /**
     * 每级统计详情
     */
    private Map<String, Integer> levelStats;
    
    /**
     * 最大宽度所需像素（用于前端画布计算）
     */
    private int requiredWidth;
    
    /**
     * 是否需要横向滚动
     */
    private boolean needHorizontalScroll;
}