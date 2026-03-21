package com.tongquyouyi.common;

/**
 * 错误码枚举
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
public enum ErrorCode {

    // 公共模块
    PARAM_ERROR(400001, "参数校验失败"),
    SYSTEM_ERROR(500001, "系统异常"),
    UNAUTHORIZED(401001, "未授权访问"),
    TOKEN_EXPIRED(401002, "Token已过期"),
    TOKEN_INVALID(401003, "Token无效"),
    IP_BRUSH(429001, "请求过于频繁，请稍后再试"),

    // 管理后台登录/权限
    ADMIN_USER_NOT_FOUND(4010101, "账号不存在"),
    ADMIN_PASSWORD_ERROR(4010102, "密码错误"),
    ADMIN_CAPTCHA_EXPIRED(4010103, "验证码失效"),
    ADMIN_CAPTCHA_ERROR(4010104, "验证码错误"),

    // 商品管理
    PRODUCT_NO_STOCK_ON(402001, "商品库存不足，不可上架"),
    PRODUCT_ON_CANNOT_MODIFY_CATEGORY(402002, "已上架商品不可修改分类/SKU结构"),
    PRODUCT_CANNOT_DELETE(402003, "仅允许删除已下架且无库存、无订单记录的商品"),

    // 库存管理
    SKU_INSUFFICIENT_STOCK(403001, "SKU库存不足"),
    CHECK_ONLY_PENDING(403002, "仅允许修改「待盘点」状态的盘点单"),
    CHECK_ONLY_CONFIRM_PENDING(403003, "仅允许确认「待盘点」状态的盘点单"),

    // 会员管理
    MEMBER_PHONE_EXIST(404001, "手机号已注册"),
    MEMBER_STORED_INSUFFICIENT(404002, "储值余额不足"),
    MEMBER_LEVEL_PRESET_CANNOT_MODIFY(404003, "普通会员不可删除/修改升级条件"),

    // 订单管理
    ORDER_STATUS_NOT_ALLOW(405001, "订单状态不允许操作"),
    COUPON_USED_OR_EXPIRED(405002, "优惠券已使用/已过期"),
    POINT_DEDUCT_OVER_RATIO(405003, "积分抵扣比例超过系统设置的最高比例"),

    // 支付模块
    PAY_FAILED(406001, "支付失败"),
    REFUND_FAILED(406002, "退款失败");

    private final Integer code;
    private final String msg;

    ErrorCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}