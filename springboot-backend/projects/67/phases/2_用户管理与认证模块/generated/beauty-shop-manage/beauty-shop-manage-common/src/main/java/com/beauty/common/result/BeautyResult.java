package com.beauty.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 美妆小店统一响应结果
 *
 * @param <T> 响应数据类型
 * @author beauty-shop
 * @since 2024-06-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BeautyResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应状态码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 成功响应（无数据）
     *
     * @param <T> 响应数据类型
     * @return 统一响应结果
     */
    public static <T> BeautyResult<T> success() {
        return new BeautyResult<>(200, "操作成功", null);
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  响应数据类型
     * @return 统一响应结果
     */
    public static <T> BeautyResult<T> success(T data) {
        return new BeautyResult<>(200, "操作成功", data);
    }

    /**
     * 成功响应（自定义消息）
     *
     * @param message 响应消息
     * @param <T>     响应数据类型
     * @return 统一响应结果
     */
    public static <T> BeautyResult<T> success(String message) {
        return new BeautyResult<>(200, message, null);
    }

    /**
     * 失败响应（默认状态码）
     *
     * @param message 响应消息
     * @param <T>     响应数据类型
     * @return 统一响应结果
     */
    public static <T> BeautyResult<T> fail(String message) {
        return new BeautyResult<>(500, message, null);
    }

    /**
     * 失败响应（自定义状态码）
     *
     * @param code    响应状态码
     * @param message 响应消息
     * @param <T>     响应数据类型
     * @return 统一响应结果
     */
    public static <T> BeautyResult<T> fail(Integer code, String message) {
        return new BeautyResult<>(code, message, null);
    }

}