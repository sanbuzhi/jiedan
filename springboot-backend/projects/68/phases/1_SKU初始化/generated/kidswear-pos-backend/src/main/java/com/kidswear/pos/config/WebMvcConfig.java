package com.kidswear.pos.config;

import com.kidswear.pos.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * Web MVC配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private JwtInterceptor jwtInterceptor;

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.url-prefix}")
    private String urlPrefix;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/**", "/common/upload", "/uploads/**", "/error", "/swagger-ui/**", "/v3/api-docs/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源映射，用于访问上传的文件
        String pathPattern = urlPrefix.substring(urlPrefix.lastIndexOf("/")) + "/**";
        registry.addResourceHandler(pathPattern)
                .addResourceLocations("file:" + uploadPath + "/");
    }
}