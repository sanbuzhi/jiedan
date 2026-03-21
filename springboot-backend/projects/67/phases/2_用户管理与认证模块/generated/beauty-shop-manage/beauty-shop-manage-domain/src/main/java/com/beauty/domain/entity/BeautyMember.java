package com.beauty.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.beauty.common.enums.BeautyMemberStatus;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 美妆小店会员档案表实体
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
@Data
@TableName("beauty_member")
public class BeautyMember implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会员条码
     */
    @TableField("member_barcode")
    private String memberBarcode;

    /**
     * 会员卡号
     */
    @TableField("member_card_no")
    private String memberCardNo;

    /**
     * 会员姓名
     */
    @TableField("member_name")
    private String memberName;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 性别（1男，0女，2未知）
     */
    @TableField("gender")
    private Integer gender;

    /**
     * 生日
     */
    @TableField("birthday")
    private LocalDate birthday;

    /**
     * 等级ID（关联beauty_member_level）
     */
    @TableField("level_id")
    private Long levelId;

    /**
     * 累计消费金额（元）
     */
    @TableField("total_consume_amount")
    private BigDecimal totalConsumeAmount;

    /**
     * 消费次数
     */
    @TableField("consume_count")
    private Integer consumeCount;

    /**
     * 可用储值余额（元）
     */
    @TableField("available_balance")
    private BigDecimal availableBalance;

    /**
     * 可用积分
     */
    @TableField("available_points")
    private Integer availablePoints;

    /**
     * 会员状态（1正常，0冻结）
     */
    @TableField("member_status")
    private BeautyMemberStatus memberStatus;

    /**
     * 会员头像URL
     */
    @TableField("member_avatar")
    private String memberAvatar;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 版本号（乐观锁）
     */
    @TableField("version")
    @Version
    private Integer version;

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