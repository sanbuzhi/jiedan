package com.jiedan.service;

import com.jiedan.dto.*;
import com.jiedan.entity.Order;
import com.jiedan.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public Order createOrder(Long userId, OrderCreate dto) {
        Order order = new Order();
        order.setRequirementId(dto.getRequirementId());
        order.setUserId(userId);
        order.setAmount(dto.getAmount());
        order.setPaymentType(dto.getPaymentType());
        order.setStatus("PENDING_PAYMENT");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    public Page<Order> getOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Order getOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问此订单");
        }

        return order;
    }

    @Transactional
    public Order payOrder(Long userId, Long orderId, OrderPayRequest dto) {
        Order order = getOrder(userId, orderId);

        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不正确");
        }

        order.setStatus("PAID");
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setTransactionId(dto.getTransactionId());
        order.setPaidAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    @Transactional
    public Order refundOrder(Long userId, Long orderId, OrderRefundRequest dto) {
        Order order = getOrder(userId, orderId);

        if (!"PAID".equals(order.getStatus())) {
            throw new RuntimeException("订单未支付，无法退款");
        }

        if (dto.getRefundAmount() == null || dto.getRefundAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("退款金额必须大于0");
        }

        if (dto.getRefundAmount().compareTo(order.getAmount()) > 0) {
            throw new RuntimeException("退款金额不能超过订单金额");
        }

        order.setStatus("REFUNDED");
        order.setRefundAmount(dto.getRefundAmount());
        order.setRefundReason(dto.getRefundReason());
        order.setRefundedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(Long userId, Long orderId) {
        Order order = getOrder(userId, orderId);

        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new RuntimeException("只能取消待支付的订单");
        }

        order.setStatus("CANCELLED");
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }
}
