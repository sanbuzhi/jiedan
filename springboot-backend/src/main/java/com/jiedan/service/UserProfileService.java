package com.jiedan.service;

import com.jiedan.config.FileStorageConfig;
import com.jiedan.entity.User;
import com.jiedan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 用户资料服务
 * 处理头像上传、昵称修改等功能
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final UserRepository userRepository;
    private final FileStorageConfig fileStorageConfig;

    // 允许的图片格式
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp"
    ));

    // 最大文件大小 2MB
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    // 昵称最小长度
    private static final int MIN_NICKNAME_LENGTH = 2;

    // 昵称最大长度
    private static final int MAX_NICKNAME_LENGTH = 20;

    /**
     * 更新用户头像
     *
     * @param userId 用户ID
     * @param file   头像文件
     * @return 头像URL
     */
    @Transactional
    public String updateAvatar(Long userId, MultipartFile file) {
        // 验证用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 验证文件
        validateImageFile(file);

        try {
            // 生成文件名: user_{userId}_{timestamp}_{uuid}.{ext}
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String filename = String.format("user_%d_%d_%s.%s",
                    userId,
                    System.currentTimeMillis(),
                    UUID.randomUUID().toString().substring(0, 8),
                    extension);

            // 保存文件
            Path targetPath = fileStorageConfig.getAvatarStoragePath().resolve(filename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // 删除旧头像（如果是非默认头像）
            deleteOldAvatar(user.getAvatar());

            // 更新用户头像URL
            String avatarUrl = fileStorageConfig.getAvatarUrlPrefix() + filename;
            user.setAvatar(avatarUrl);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            log.info("用户 {} 更新头像成功: {}", userId, avatarUrl);
            return avatarUrl;

        } catch (IOException e) {
            log.error("保存头像文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("保存头像失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户昵称
     *
     * @param userId   用户ID
     * @param nickname 新昵称
     * @return 更新后的昵称
     */
    @Transactional
    public String updateNickname(Long userId, String nickname) {
        // 验证用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 验证昵称
        validateNickname(nickname);

        // 更新昵称
        user.setNickname(nickname.trim());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("用户 {} 更新昵称成功: {}", userId, nickname);
        return nickname;
    }

    /**
     * 获取默认头像URL
     */
    public String getDefaultAvatarUrl() {
        return fileStorageConfig.getDefaultAvatarUrl();
    }

    /**
     * 判断是否为默认头像
     */
    public boolean isDefaultAvatar(String avatarUrl) {
        return fileStorageConfig.isDefaultAvatar(avatarUrl);
    }

    /**
     * 生成默认昵称
     *
     * @param referralCode 推荐码
     * @return 默认昵称
     */
    public String generateDefaultNickname(String referralCode) {
        return "智搭小掌柜-" + referralCode;
    }

    /**
     * 验证图片文件
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择要上传的图片");
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("图片大小不能超过2MB");
        }

        // 检查文件格式
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new RuntimeException("不支持的图片格式，请上传 jpg、jpeg、png、gif 或 webp 格式的图片");
        }

        // 检查 content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("无效的图片文件");
        }
    }

    /**
     * 验证昵称
     */
    private void validateNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new RuntimeException("昵称不能为空");
        }

        String trimmed = nickname.trim();

        if (trimmed.length() < MIN_NICKNAME_LENGTH) {
            throw new RuntimeException("昵称至少需要" + MIN_NICKNAME_LENGTH + "个字符");
        }

        if (trimmed.length() > MAX_NICKNAME_LENGTH) {
            throw new RuntimeException("昵称不能超过" + MAX_NICKNAME_LENGTH + "个字符");
        }

        // 检查是否包含非法字符
        if (!trimmed.matches("^[\\u4e00-\\u9fa5a-zA-Z0-9_\\-\\s]+$")) {
            throw new RuntimeException("昵称只能包含中文、英文、数字、下划线、横线和空格");
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "png";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 删除旧头像文件
     */
    private void deleteOldAvatar(String oldAvatarUrl) {
        if (oldAvatarUrl == null || oldAvatarUrl.isEmpty()) {
            return;
        }

        // 不删除默认头像
        if (fileStorageConfig.isDefaultAvatar(oldAvatarUrl)) {
            return;
        }

        try {
            String filename = oldAvatarUrl.substring(oldAvatarUrl.lastIndexOf("/") + 1);
            Path oldPath = fileStorageConfig.getAvatarStoragePath().resolve(filename);
            Files.deleteIfExists(oldPath);
            log.debug("删除旧头像: {}", oldPath);
        } catch (IOException e) {
            log.warn("删除旧头像失败: {}", e.getMessage());
        }
    }
}
