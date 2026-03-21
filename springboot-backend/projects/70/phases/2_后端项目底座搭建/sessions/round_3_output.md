===FILE:back/src/main/java/com/tongquyouyi/base/BaseMapper.java===
```java
package com.tongquyouyi.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 基础Mapper接口
 * @param <T> 实体类
 */
public interface BaseMapper<T> extends com.baomidou.mybatisplus.core.mapper.BaseMapper<T> {
}
```

===FILE:back/src/main/java/com/tongquyouyi/base/BaseService.java===
```java
package com.tongquyouyi.base;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 基础Service接口
 * @param <T> 实体类
 */
public interface BaseService<T> extends IService<T> {
}
```

===FILE:back/src/main/java/com/tongquyouyi/base/BaseServiceImpl.java===
```java
package com.tongquyouyi.base;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 基础Service实现类
 * @param <M> Mapper接口
 * @param <T> 实体类
 */
public class BaseServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> implements BaseService<T> {
}
```

===FILE:back/src/main/java/com/tongquyouyi/base/BaseController.java===
```java
package com.tongquyouyi.base;

import com.tongquyouyi.common.Result;

/**
 * 基础Controller
 */
public class BaseController {

    /**
     * 成功响应
     */
    protected <T> Result<T> success() {
        return Result.success();
    }

    /**
     * 成功响应（带数据）
     */
    protected <T> Result<T> success(T data) {
        return Result.success(data);
    }

    /**
     * 成功响应（带数据和消息）
     */
    protected <T> Result<T> success(T data, String msg) {
        return Result.success(data, msg);
    }

    /**
     * 失败响应
     */
    protected <T> Result<T> error() {
        return Result.error();
    }

    /**
     * 失败响应（带消息）
     */
    protected <T> Result<T> error(String msg) {
        return Result.error(msg);
    }

    /**
     * 失败响应（带错误码和消息）
     */
    protected <T> Result<T> error(Integer code, String msg) {
        return Result.error(code, msg);
    }
}
```

===FILE:back/src/main/java/com/tongquyouyi/constant/MessageConstant.java===
```java
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
```

===FILE:back/src/main/java/com/tongquyouyi/enums/AccountTypeEnum.java===
```java
package com.tongquyouyi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账号类型枚举
 */
@Getter
@AllArgsConstructor
public enum AccountTypeEnum {

    PHONE(1, "手机号"),
    EMAIL(2, "邮箱"),
    WECHAT(3, "微信"),
    ALIPAY(4, "支付宝");

    private final Integer code;
    private final String desc;

    /**
     * 根据code获取枚举
     */
    public static AccountTypeEnum getByCode(Integer code) {
        for (AccountTypeEnum typeEnum : values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}
```

===FILE:back/src/main/java/com/tongquyouyi/enums/PayTypeEnum.java===
```java
package com.tongquyouyi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付方式枚举
 */
@Getter
@AllArgsConstructor
public enum PayTypeEnum {

    WECHAT_PAY(1, "微信支付"),
    ALIPAY(2, "支付宝");

    private final Integer code;
    private final String desc;

    /**
     * 根据code获取枚举
     */
    public static PayTypeEnum getByCode(Integer code) {
        for (PayTypeEnum typeEnum : values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}
```

===FILE:back/src/main/java/com/tongquyouyi/enums/OrderStatusEnum.java===
```java
package com.tongquyouyi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态枚举
 */
@Getter
@AllArgsConstructor
public enum OrderStatusEnum {

    UNPAID(0, "待支付"),
    PAID(1, "已支付"),
    CANCELLED(2, "已取消"),
    REFUNDED(3, "已退款"),
    COMPLETED(4, "已完成");

    private final Integer code;
    private final String desc;

    /**
     * 根据code获取枚举
     */
    public static OrderStatusEnum getByCode(Integer code) {
        for (OrderStatusEnum statusEnum : values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }
}
```