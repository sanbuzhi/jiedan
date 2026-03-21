package com.beauty.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 美妆小店权限码枚举（简化版，仅包含阶段2+基础权限）
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
@Getter
@AllArgsConstructor
public enum BeautyPermissionCode {

    // -------------- 员工管理权限 --------------
    STAFF_LIST("staff:list", "员工列表查询"),
    STAFF_SAVE("staff:save", "员工新增"),
    STAFF_UPDATE("staff:update", "员工修改"),
    STAFF_DELETE("staff:delete", "员工删除"),
    STAFF_RESET_PASSWORD("staff:reset:password", "员工重置密码"),
    STAFF_BATCH_IMPORT("staff:batch:import", "员工批量导入"),
    STAFF_TEMPLATE_DOWNLOAD("staff:template:download", "员工模板下载"),
    STAFF_EXPORT("staff:export", "员工批量导出"),

    // -------------- 角色管理权限 --------------
    ROLE_LIST("role:list", "角色列表查询"),
    ROLE_SAVE("role:save", "角色新增"),
    ROLE_UPDATE("role:update", "角色修改"),
    ROLE_DELETE("role:delete", "角色删除"),

    // -------------- 会员管理权限 --------------
    MEMBER_LIST("member:list", "会员列表查询"),
    MEMBER_SAVE("member:save", "会员新增"),
    MEMBER_UPDATE("member:update", "会员修改"),
    MEMBER_DELETE("member:delete", "会员删除"),
    MEMBER_BATCH_IMPORT("member:batch:import", "会员批量导入"),
    MEMBER_TEMPLATE_DOWNLOAD("member:template:download", "会员模板下载"),
    MEMBER_EXPORT("member:export", "会员批量导出"),
    MEMBER_RECHARGE("member:recharge", "会员储值充值"),
    MEMBER_POINT_ADJUST("member:point:adjust", "会员积分调整"),
    MEMBER_DETAIL("member:detail", "会员详情查询"),
    MEMBER_FLOW_LIST("member:flow:list", "会员流水查询"),
    MEMBER_FLOW_EXPORT("member:flow:export", "会员流水导出"),

    // -------------- 会员等级管理权限 --------------
    LEVEL_LIST("level:list", "会员等级列表查询"),
    LEVEL_SAVE("level:save", "会员等级新增"),
    LEVEL_UPDATE("level:update", "会员等级修改"),
    LEVEL_DELETE("level:delete", "会员等级删除"),

    // -------------- 基础权限 --------------
    DASHBOARD("dashboard", "系统首页"),
    LOGOUT("logout", "登出"),
    FIRST_PASSWORD_CHANGE("first:password:change", "首次登录强制修改密码"),
    REFRESH_TOKEN("refresh:token", "刷新Token");

    private final String code;
    private final String desc;

}