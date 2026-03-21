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