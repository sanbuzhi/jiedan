package com.beauty.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 美妆小店员工角色表实体
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
@Data
@TableName("beauty_role")
public class BeautyRole implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 角色名称
     */
    @TableField("role_name")
    private String roleName;

    /**
     * 角色描述
     */
    @TableField("role_desc")
    private String roleDesc;

    /**
     * 权限码列表（逗号分隔）
     */
    @TableField("permission_codes")
    private String permissionCodes;

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