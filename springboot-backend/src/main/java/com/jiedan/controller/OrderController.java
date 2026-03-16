package com.jiedan.controller;

import com.jiedan.dto.OrderCreate;
import com.jiedan.dto.OrderPayRequest;
import com.jiedan.dto.OrderRefundRequest;
import com.jiedan.dto.OrderResponse;
import com.jiedan.entity.Order;
import com.jiedan.entity.Requirement;
import com.jiedan.entity.User;
import com.jiedan.repository.OrderRepository;
import com.jiedan.repository.RequirementRepository;
import com.jiedan.repository.UserRepository;
import com.jiedan.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final RequirementRepository requirementRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @CurrentUser Long userId,
            @Valid @RequestBody OrderCreate request) {
        Requirement requirement = requirementRepository.findById(request.getRequirementId())
                .orElseThrow(() -> new RuntimeException("需求不存在"));

        if (!requirement.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Order order = new Order();
        order.setRequirementId(request.getRequirementId());
        order.setUserId(userId);
        order.setAmount(request.getAmount());
        order.setPaymentType(request.getPaymentType());
        order.setStatus("pending");

        orderRepository.save(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponse(order));
    }

    @GetMapping
    public ResponseEntity<?> listOrders(
            @CurrentUser Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentType,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) String orderNo,
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "10") int limit) {
        
        Pageable pageable = PageRequest.of(skip / limit, limit);
        Page<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        // 转换为前端期望的格式
        Map<String, Object> response = new HashMap<>();
        response.put("items", orders.getContent().stream().map(this::convertToAdminResponse).toList());
        response.put("total", orders.getTotalElements());
        response.put("skip", skip);
        response.put("limit", limit);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(
            @CurrentUser Long userId,
            @PathVariable Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(convertToResponse(order));
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<OrderResponse> payOrder(
            @CurrentUser Long userId,
            @PathVariable Long id,
            @Valid @RequestBody OrderPayRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (!"pending".equals(order.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        order.setStatus("paid");
        order.setPaidAt(LocalDateTime.now());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setTransactionId(request.getTransactionId());

        orderRepository.save(order);
        return ResponseEntity.ok(convertToResponse(order));
    }

    @PutMapping("/{id}/refund")
    public ResponseEntity<OrderResponse> refundOrder(
            @CurrentUser Long userId,
            @PathVariable Long id,
            @Valid @RequestBody OrderRefundRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (!"paid".equals(order.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (request.getRefundAmount().compareTo(order.getAmount()) > 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        order.setStatus("refunded");
        order.setRefundedAt(LocalDateTime.now());
        order.setRefundAmount(request.getRefundAmount());
        order.setRefundReason(request.getRefundReason());

        orderRepository.save(order);
        return ResponseEntity.ok(convertToResponse(order));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @CurrentUser Long userId,
            @PathVariable Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (!"pending".equals(order.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        order.setStatus("cancelled");

        orderRepository.save(order);
        return ResponseEntity.ok(convertToResponse(order));
    }

    private OrderResponse convertToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setRequirementId(order.getRequirementId());
        response.setUserId(order.getUserId().toString());
        response.setAmount(order.getAmount());
        response.setPaymentType(order.getPaymentType());
        response.setStatus(order.getStatus());
        response.setPaidAt(order.getPaidAt());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setTransactionId(order.getTransactionId());
        response.setRefundedAt(order.getRefundedAt());
        response.setRefundAmount(order.getRefundAmount());
        response.setRefundReason(order.getRefundReason());
        response.setNotes(order.getNotes());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        return response;
    }

    // 管理员视图转换 - 包含用户信息
    private Map<String, Object> convertToAdminResponse(Order order) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", order.getId());
        response.put("order_no", "ORD" + order.getId());
        response.put("amount", order.getAmount());
        response.put("status", order.getStatus());
        response.put("payment_type", order.getPaymentType());
        response.put("payment_method", order.getPaymentMethod());
        response.put("created_at", order.getCreatedAt());
        response.put("paid_at", order.getPaidAt());
        
        // 获取用户信息
        User user = userRepository.findById(order.getUserId()).orElse(null);
        response.put("user_phone", user != null ? user.getPhone() : "");
        response.put("user_id", order.getUserId());
        
        return response;
    }
}
