package com.tongquyouyi.constant;

/**
 * 消息常量
 */
public class MessageConstant {

    // 通用
    public static final String SUCCESS = "操作成功";
    public static final String FAIL = "操作失败";
    public static final String PARAM_ERROR = "参数错误";
    public static final String SYSTEM_ERROR = "系统错误";
    public static final String DATA_NOT_FOUND = "数据不存在";
    public static final String DATA_ALREADY_EXISTS = "数据已存在";
    public static final String DELETE_SUCCESS = "删除成功";
    public static final String UPDATE_SUCCESS = "修改成功";
    public static final String ADD_SUCCESS = "新增成功";

    // 用户
    public static final String USER_NOT_LOGIN = "用户未登录";
    public static final String USER_ACCOUNT_NOT_EXIST = "账号不存在";
    public static final String USER_PASSWORD_ERROR = "密码错误";
    public static final String USER_ACCOUNT_DISABLED = "账号已禁用";
    public static final String USER_LOGIN_SUCCESS = "登录成功";
    public static final String USER_LOGOUT_SUCCESS = "登出成功";
    public static final String USER_ALREADY_EXISTS = "用户已存在";
    public static final String USER_REGISTER_SUCCESS = "注册成功";

    // 验证码
    public static final String CAPTCHA_ERROR = "验证码错误";
    public static final String CAPTCHA_EXPIRED = "验证码已过期";
    public static final String CAPTCHA_SEND_SUCCESS = "验证码发送成功";
    public static final String CAPTCHA_SEND_FAIL = "验证码发送失败";
    public static final String CAPTCHA_FREQUENTLY = "验证码发送过于频繁";

    // 文件
    public static final String FILE_UPLOAD_SUCCESS = "文件上传成功";
    public static final String FILE_UPLOAD_FAIL = "文件上传失败";
    public static final String FILE_DELETE_SUCCESS = "文件删除成功";
    public static final String FILE_DELETE_FAIL = "文件删除失败";
    public static final String FILE_FORMAT_ERROR = "文件格式错误";
    public static final String FILE_SIZE_EXCEEDED = "文件大小超出限制";

    // 支付
    public static final String PAY_ORDER_NOT_FOUND = "支付订单不存在";
    public static final String PAY_ORDER_PAID = "订单已支付";
    public static final String PAY_ORDER_CANCELLED = "订单已取消";
    public static final String PAY_ORDER_REFUNDED = "订单已退款";
    public static final String PAY_SUCCESS = "支付成功";
    public static final String PAY_FAIL = "支付失败";
    public static final String REFUND_SUCCESS = "退款成功";
    public static final String REFUND_FAIL = "退款失败";
}