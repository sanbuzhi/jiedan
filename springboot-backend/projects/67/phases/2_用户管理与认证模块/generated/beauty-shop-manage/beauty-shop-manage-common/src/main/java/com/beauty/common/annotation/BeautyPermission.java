package com.beauty.common.annotation;

import com.beauty.common.enums.BeautyPermissionCode;

import java.lang.annotation.*;

/**
 * 美妆小店权限校验注解
 *
 * @author beauty-shop
 * @since 2024-06-01
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BeautyPermission {

    /**
     * 需要的权限码
     */
    BeautyPermissionCode[] value();

    /**
     * 是否需要所有权限，默认false（只要一个权限满足即可）
     */
    boolean requireAll() default false;

}