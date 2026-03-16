package com.jiedan.controller;

import com.jiedan.dto.ReferralTreeStats;
import com.jiedan.dto.UserResponse;
import com.jiedan.dto.UserUpdate;
import com.jiedan.entity.PointRecord;
import com.jiedan.entity.Rule;
import com.jiedan.entity.User;
import com.jiedan.repository.PointRecordRepository;
import com.jiedan.repository.RuleRepository;
import com.jiedan.repository.UserRepository;
import com.jiedan.security.CurrentUser;
import com.jiedan.service.UserProfileService;
import com.jiedan.service.UserService;
import com.jiedan.service.WechatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PointRecordRepository pointRecordRepository;
    private final RuleRepository ruleRepository;
    private final UserService userService;
    private final WechatService wechatService;
    private final UserProfileService userProfileService;

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@CurrentUser Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("userId", user.getId().toString());
        response.put("phone", user.getPhone());
        response.put("nickname", user.getNickname());
        response.put("avatar", user.getAvatar());
        response.put("isDefaultAvatar", userProfileService.isDefaultAvatar(user.getAvatar()));
        response.put("referralCode", user.getReferralCode());
        response.put("referrerId", user.getReferrerId() != null ? user.getReferrerId().toString() : null);
        response.put("totalPoints", user.getTotalPoints());
        response.put("isActive", user.getIsActive());
        response.put("createdAt", user.getCreatedAt());
        response.put("updatedAt", user.getUpdatedAt());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @CurrentUser Long userId,
            @Valid @RequestBody UserUpdate request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        userRepository.save(user);
        return ResponseEntity.ok(convertToUserResponse(user));
    }

    /**
     * 上传用户头像
     */
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadAvatar(
            @CurrentUser Long userId,
            @RequestParam("file") MultipartFile file) {
        String avatarUrl = userProfileService.updateAvatar(userId, file);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Map<String, Object> response = new HashMap<>();
        response.put("avatar", avatarUrl);
        response.put("isDefaultAvatar", userProfileService.isDefaultAvatar(avatarUrl));
        response.put("user", convertToUserResponse(user));
        return ResponseEntity.ok(response);
    }

    /**
     * 更新用户昵称
     */
    @PutMapping("/me/nickname")
    public ResponseEntity<Map<String, String>> updateNickname(
            @CurrentUser Long userId,
            @RequestBody Map<String, String> request) {
        String nickname = request.get("nickname");
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new RuntimeException("昵称不能为空");
        }

        String updatedNickname = userProfileService.updateNickname(userId, nickname);

        Map<String, String> response = new HashMap<>();
        response.put("nickname", updatedNickname);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/referrals")
    public ResponseEntity<Page<ReferralResponse>> getReferrals(
            @CurrentUser Long userId,
            Pageable pageable) {
        Page<User> referrals = userRepository.findByReferrerId(userId, pageable);
        return ResponseEntity.ok(referrals.map(this::convertToReferralResponse));
    }

    @GetMapping("/referrals/tree")
    public ResponseEntity<ReferralTreeItem> getReferralTree(@CurrentUser Long userId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        ReferralTreeItem root = buildReferralTree(currentUser, 0);
        return ResponseEntity.ok(root);
    }

    @GetMapping("/referrals/tree-stats")
    public ResponseEntity<ReferralTreeStats> getReferralTreeStats(@CurrentUser Long userId) {
        ReferralTreeStats stats = userService.getReferralTreeStats(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Build referral tree recursively using referrer_id field
     */
    private ReferralTreeItem buildReferralTree(User user, int level) {
        ReferralTreeItem item = new ReferralTreeItem();
        item.setUser(convertToUserResponse(user));
        item.setLevel(level);
        item.setChildren(new ArrayList<>());

        // Only build up to 3 levels
        if (level < 3) {
            List<User> children = userRepository.findByReferrerId(user.getId());
            for (User child : children) {
                ReferralTreeItem childItem = buildReferralTree(child, level + 1);
                item.getChildren().add(childItem);
            }
        }

        return item;
    }

    @GetMapping("/points")
    public ResponseEntity<Page<PointRecord>> getPointRecords(
            @CurrentUser Long userId,
            Pageable pageable) {
        Page<PointRecord> records = pointRecordRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/rules")
    public ResponseEntity<List<Rule>> getRules(@CurrentUser Long userId) {
        List<Rule> rules = ruleRepository.findByIsActiveTrue();
        return ResponseEntity.ok(rules);
    }

    @PostMapping("/rules/init")
    public ResponseEntity<Void> initRules(@CurrentUser Long userId) {
        List<Rule> existingRules = ruleRepository.findAll();
        if (!existingRules.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        List<Rule> defaultRules = Arrays.asList(
                createRule("注册奖励", "REGISTER", "新用户注册奖励", "register", 100, 3),
                createRule("邀请奖励", "INVITE", "邀请新用户奖励", "invite", 50, 3),
                createRule("下单奖励", "ORDER", "完成订单奖励", "order", 200, 3)
        );

        ruleRepository.saveAll(defaultRules);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/referral-qrcode")
    public ResponseEntity<byte[]> getReferralQRCode(@CurrentUser Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        byte[] qrCodeImage = wechatService.generateReferralQRCode(user.getReferralCode());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(qrCodeImage.length);
        headers.setCacheControl("max-age=3600");

        return new ResponseEntity<>(qrCodeImage, headers, HttpStatus.OK);
    }

    private Rule createRule(String name, String code, String description, String type, int points, int maxLevel) {
        Rule rule = new Rule();
        rule.setName(name);
        rule.setCode(code);
        rule.setDescription(description);
        rule.setType(type);
        rule.setPoints(points);
        rule.setMaxLevel(maxLevel);
        rule.setIsActive(true);
        return rule;
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUserId(user.getId().toString());
        response.setPhone(user.getPhone());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setReferralCode(user.getReferralCode());
        response.setReferrerId(user.getReferrerId() != null ? user.getReferrerId().toString() : null);
        response.setTotalPoints(user.getTotalPoints());
        response.setIsActive(user.getIsActive());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }

    private ReferralResponse convertToReferralResponse(User user) {
        ReferralResponse response = new ReferralResponse();
        response.setUserId(user.getId().toString());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReferralResponse {
        private String userId;
        private String nickname;
        private String avatar;
        private java.time.LocalDateTime createdAt;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReferralTreeItem {
        private UserResponse user;
        private Integer level;
        private List<ReferralTreeItem> children;
    }
}
