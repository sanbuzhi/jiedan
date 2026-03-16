package com.jiedan.config;

import com.jiedan.security.CurrentUserArgumentResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    private final CurrentUserArgumentResolver currentUserArgumentResolver;

    @Value("${app.upload.avatar-path:${user.dir}/uploads/avatars/}")
    private String avatarPath;

    @Value("${app.upload.avatar-url-prefix:/uploads/avatars/}")
    private String avatarUrlPrefix;

    public WebConfig(CurrentUserArgumentResolver currentUserArgumentResolver) {
        this.currentUserArgumentResolver = currentUserArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置头像静态资源映射 - 支持多种路径格式
        String uploadPath = "file:" + avatarPath;
        if (!avatarPath.endsWith("/")) {
            uploadPath += "/";
        }

        log.info("配置静态资源映射:");
        log.info("  URL前缀: {}", avatarUrlPrefix);
        log.info("  本地路径: {}", uploadPath);

        // 支持 /uploads/avatars/** 格式
        registry.addResourceHandler(avatarUrlPrefix + "**")
                .addResourceLocations(uploadPath);

        // 支持 /api/uploads/avatars/** 格式（兼容context-path）
        registry.addResourceHandler("/api" + avatarUrlPrefix + "**")
                .addResourceLocations(uploadPath);
    }
}
