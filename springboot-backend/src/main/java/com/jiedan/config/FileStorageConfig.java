package com.jiedan.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件存储配置类
 * 用于管理头像等静态文件的存储路径
 */
@Configuration
@Getter
@Slf4j
public class FileStorageConfig {

    @Value("${app.upload.avatar-path:${user.dir}/uploads/avatars/}")
    private String avatarPath;

    @Value("${app.upload.avatar-url-prefix:/uploads/avatars/}")
    private String avatarUrlPrefix;

    @Value("${app.upload.default-avatar:default-avatar.png}")
    private String defaultAvatar;

    /**
     * 初始化存储目录
     */
    @PostConstruct
    public void init() {
        try {
            Path path = Paths.get(avatarPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("创建头像存储目录: {}", path.toAbsolutePath());
            }

            // 检查默认头像是否存在
            Path defaultAvatarPath = path.resolve(defaultAvatar);
            if (!Files.exists(defaultAvatarPath)) {
                log.warn("默认头像文件不存在: {}", defaultAvatarPath);
                // 尝试从classpath复制默认头像
                copyDefaultAvatarFromClasspath(defaultAvatarPath);
            }
        } catch (IOException e) {
            log.error("初始化头像存储目录失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 从classpath复制默认头像
     */
    private void copyDefaultAvatarFromClasspath(Path targetPath) {
        try (var inputStream = getClass().getResourceAsStream("/static/" + defaultAvatar)) {
            if (inputStream != null) {
                Files.copy(inputStream, targetPath);
                log.info("从classpath复制默认头像到: {}", targetPath);
            } else {
                log.warn("classpath中未找到默认头像文件");
            }
        } catch (IOException e) {
            log.error("复制默认头像失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取头像存储路径
     */
    public Path getAvatarStoragePath() {
        return Paths.get(avatarPath);
    }

    /**
     * 获取默认头像URL
     */
    public String getDefaultAvatarUrl() {
        return avatarUrlPrefix + defaultAvatar;
    }

    /**
     * 判断是否为默认头像
     */
    public boolean isDefaultAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return true;
        }
        return avatarUrl.endsWith(defaultAvatar);
    }
}
