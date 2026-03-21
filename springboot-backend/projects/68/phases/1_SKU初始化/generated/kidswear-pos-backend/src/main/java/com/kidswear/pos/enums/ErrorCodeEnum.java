package com.kidswear.pos.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
@AllArgsConstructor
public enum ErrorCodeEnum {
    // 通用错误
    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限操作"),
    NOT_FOUND(404, "数据不存在"),
    INTERNAL_ERROR(500, "系统内部错误"),

    // 用户相关
    USER_NOT_FOUND(1001, "用户不存在"),
    PASSWORD_ERROR(1002, "密码错误"),
    USER_DISABLED(1003, "用户已禁用"),

    // 分类相关
    CATEGORY_NAME_EXISTS(2001, "分类名称已存在"),
    CATEGORY_HAS_CHILDREN(2002, "该分类下有子分类，无法删除"),
    CATEGORY_HAS_SKUS(2003, "该分类下有商品，无法删除"),
    CATEGORY_PARENT_NOT_FOUND(2004, "父分类不存在"),

    // SKU相关
    SKU_CODE_EXISTS(3001, "SKU编码已存在"),
    SKU_NOT_FOUND(3002, "SKU不存在"),
    SKU_STATUS_INVALID(3003, "SKU状态无效");

    private final Integer code;
    private final String message;
}