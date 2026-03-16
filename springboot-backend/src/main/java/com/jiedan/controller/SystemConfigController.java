package com.jiedan.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/admin")
public class SystemConfigController {

    @GetMapping("/system-config")
    public ResponseEntity<Map<String, Object>> getSystemConfig() {
        Map<String, Object> config = new HashMap<>();
        
        // 系统基本配置
        config.put("site_name", "智能获客系统");
        config.put("site_logo", "/logo.png");
        config.put("site_description", "专业的智能获客解决方案");
        
        // 联系信息
        config.put("contact_phone", "400-123-4567");
        config.put("contact_email", "support@jiedan.com");
        config.put("contact_address", "北京市朝阳区xxx大厦");
        
        // 功能开关
        config.put("registration_enabled", true);
        config.put("referral_enabled", true);
        config.put("points_enabled", true);
        
        // 积分配置
        config.put("points_per_referral", 100);
        config.put("points_per_order", 50);
        config.put("min_points_for_exchange", 1000);
        
        // 其他配置
        config.put("max_upload_size", 10485760); // 10MB
        config.put("allowed_file_types", List.of("jpg", "jpeg", "png", "gif", "pdf"));
        config.put("session_timeout", 7200); // 2小时
        
        config.put("updated_at", LocalDateTime.now());
        
        return ResponseEntity.ok(config);
    }

    @PutMapping("/system-config")
    public ResponseEntity<Map<String, Object>> updateSystemConfig(@RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "系统配置已更新");
        response.put("updated_config", data);
        response.put("updated_at", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}
