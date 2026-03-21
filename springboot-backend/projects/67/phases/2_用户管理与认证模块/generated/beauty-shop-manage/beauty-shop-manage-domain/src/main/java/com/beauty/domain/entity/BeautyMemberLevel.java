package com.beauty.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 美妆小店会员等级表实体
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
@Data
@TableName("beauty_member_level")
public class BeautyMemberLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 等级名称
     */
    @TableField("level_name")
    private String levelName;

    /**
     * 等级图标URL
     */
    @TableField("level_icon")
    private String levelIcon;

    /**
     * 升级条件（累计消费金额≥X元）
     */
    @TableField("level_condition")
    private BigDecimal levelCondition;

    /**
     * 普通折扣率（0.01-1.00）
     */
    @TableField("general_discount")
    private BigDecimal generalDiscount;

    /**
     * 生日折扣率（0.01-1.00）
     */
    @TableField("birthday_discount")
    private BigDecimal birthdayDiscount;

    /**
     * 积分兑换比例（1元=X积分）
     */
    @TableField("point_ratio")
    private BigDecimal pointRatio;

    /**
     * 积分抵现比例（X积分=1元）
     */
    @TableField("point_cash_ratio")
    private Integer pointCashRatio;

    /**
     * 排序顺序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 更新人ID
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 逻辑删除（0未删除，1已删除）
     */
    @TableField("deleted")
    @TableLogic
    private Integer deleted;

}