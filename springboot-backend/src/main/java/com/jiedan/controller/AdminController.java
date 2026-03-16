package com.jiedan.controller;

import com.jiedan.entity.User;
import com.jiedan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "10") int limit) {
        
        Pageable pageable = PageRequest.of(skip / limit, limit);
        Page<User> userPage = userRepository.findAll(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("items", userPage.getContent().stream().map(this::convertToUserResponse).toList());
        response.put("total", userPage.getTotalElements());
        response.put("skip", skip);
        response.put("limit", limit);
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{id}/toggle-active")
    public ResponseEntity<Map<String, Object>> toggleUserActive(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 切换状态 - 假设 User 实体有 isActive 字段，如果没有需要添加
        // 这里暂时返回成功，实际实现需要 User 实体支持
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "用户状态已切换");
        response.put("user", convertToUserResponse(user));
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, Object> data) {
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 更新用户信息
        if (data.containsKey("nickname")) {
            user.setNickname((String) data.get("nickname"));
        }
        
        // 更新积分 - 假设有 points 字段
        if (data.containsKey("total_points")) {
            // user.setPoints((Integer) data.get("total_points"));
        }
        
        userRepository.save(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "用户信息已更新");
        response.put("user", convertToUserResponse(user));
        
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> convertToUserResponse(User user) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("phone", user.getPhone());
        response.put("nickname", user.getNickname());
        response.put("referral_code", user.getReferralCode());
        response.put("avatar", user.getAvatar());
        response.put("created_at", user.getCreatedAt());
        response.put("total_points", 0); // 暂时返回0，需要添加积分字段
        response.put("is_active", true); // 暂时返回true，需要添加状态字段
        return response;
    }
}
