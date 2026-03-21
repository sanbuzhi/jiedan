package com.beauty.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.beauty.common.enums.BeautyStaffStatus;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 美妆小店员工档案表实体
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
@Data
@TableName("beauty_staff")
public class BeautyStaff implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 员工工号
     */
    @TableField("staff_no")
    private String staffNo;

    /**
     * 登录账号
     */
    @TableField("login_account")
    private String loginAccount;

    /**
     * 登录密码（AES256加密）
     */
    @TableField("password")
    private String password;

    /**
     * 员工姓名
     */
    @TableField("staff_name")
    private String staffName;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 角色ID（关联beauty_role）
     */
    @TableField("role_id")
    private Long roleId;

    /**
     * 员工状态（1正常，0停用）
     */
    @TableField("staff_status")
    private BeautyStaffStatus staffStatus;

    /**
     * 是否首次登录（1是，0否）
     */
    @TableField("is_first_login")
    private Integer isFirstLogin;

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