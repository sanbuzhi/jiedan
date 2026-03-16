package com.jiedan.controller;

import com.jiedan.entity.Order;
import com.jiedan.entity.Requirement;
import com.jiedan.entity.User;
import com.jiedan.repository.OrderRepository;
import com.jiedan.repository.RequirementRepository;
import com.jiedan.repository.UserRepository;
import com.jiedan.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final RequirementRepository requirementRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats(
            @CurrentUser Long userId,
            @RequestParam(defaultValue = "30") int days) {

        LocalDateTime startDate = LocalDateTime.now().minus(days, ChronoUnit.DAYS);

        // 获取统计数据
        long totalUsers = userRepository.count();
        long newUsers = userRepository.countByCreatedAtAfter(startDate);

        long totalOrders = orderRepository.count();
        long newOrders = orderRepository.countByCreatedAtAfter(startDate);

        long totalRequirements = requirementRepository.count();
        long newRequirements = requirementRepository.countByCreatedAtAfter(startDate);

        // 计算收入
        List<Order> orders = orderRepository.findAll();
        double totalRevenue = orders.stream()
                .filter(o -> "PAID".equals(o.getStatus()))
                .mapToDouble(o -> o.getAmount() != null ? o.getAmount().doubleValue() : 0)
                .sum();

        // 获取最近数据
        List<Requirement> recentRequirements = requirementRepository
                .findTop5ByOrderByCreatedAtDesc();
        List<Order> recentOrders = orderRepository
                .findTop5ByOrderByCreatedAtDesc();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total_views", totalUsers * 10); // 模拟数据
        stats.put("total_clicks", totalUsers * 3); // 模拟数据
        stats.put("total_registrations", totalUsers);
        stats.put("new_registrations", newUsers);
        stats.put("total_conversions", totalOrders);
        stats.put("new_conversions", newOrders);
        stats.put("total_orders", totalOrders);
        stats.put("new_orders", newOrders);
        stats.put("total_revenue", totalRevenue);
        stats.put("new_revenue", totalRevenue * 0.1); // 模拟近期收入
        stats.put("total_requirements", totalRequirements);
        stats.put("new_requirements", newRequirements);

        // 最近的需求
        stats.put("recent_requirements", recentRequirements.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("title", "需求 #" + r.getId());
            map.put("status", r.getStatus());
            map.put("created_at", r.getCreatedAt());
            map.put("user", Map.of("nickname", "用户" + r.getUserId()));
            return map;
        }).toList());

        // 最近的订单
        stats.put("recent_orders", recentOrders.stream().map(o -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", o.getId());
            map.put("order_no", "ORD" + o.getId());
            map.put("status", o.getStatus());
            map.put("total_amount", o.getAmount());
            map.put("created_at", o.getCreatedAt());
            map.put("user", Map.of("nickname", "用户" + o.getUserId()));
            return map;
        }).toList());

        // 模拟事件数据
        stats.put("recent_events", List.of(
            Map.of("id", 1, "event_type", "VIEW", "description", "页面访问", "created_at", LocalDateTime.now()),
            Map.of("id", 2, "event_type", "CLICK", "description", "按钮点击", "created_at", LocalDateTime.now().minusMinutes(5)),
            Map.of("id", 3, "event_type", "REGISTER", "description", "用户注册", "created_at", LocalDateTime.now().minusMinutes(10))
        ));

        // 模拟平台数据 - 使用前端期望的格式
        stats.put("top_platforms", List.of(
            Map.of("platform", "微信小程序", "count", 4500),
            Map.of("platform", "微信公众号", "count", 2800),
            Map.of("platform", "抖音", "count", 1200),
            Map.of("platform", "小红书", "count", 800)
        ));

        return ResponseEntity.ok(stats);
    }
}
