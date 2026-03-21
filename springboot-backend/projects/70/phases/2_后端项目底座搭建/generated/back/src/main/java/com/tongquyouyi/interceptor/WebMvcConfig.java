package com.tongquyouyi.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类（拦截器注册）
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private IpBrushInterceptor ipBrushInterceptor;

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // IP防刷拦截器（拦截所有请求）
        registry.addInterceptor(ipBrushInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/doc.html", "/webjars/**", "/swagger-resources/**", "/v3/api-docs/**");

        // JWT身份认证拦截器（拦截管理后台和线上商城的业务接口）
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/admin/**", "/api/store/**")
                .excludePathPatterns(
                        // 管理后台登录相关
                        "/api/admin/auth/captcha",
                        "/api/admin/auth/login",
                        "/api/admin/auth/refresh",
                        // 线上商城登录相关
                        "/api/store/auth/send-sms",
                        "/api/store/auth/login",
                        "/api/store/auth/refresh",
                        // 线上商城首页相关
                        "/api/store/index/**",
                        "/api/store/product/list",
                        "/api/store/product/search",
                        "/api/store/product/get",
                        // 支付回调
                        "/api/store/pay/**/notify"
                );
    }

}