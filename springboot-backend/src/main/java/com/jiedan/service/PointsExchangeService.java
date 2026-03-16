package com.jiedan.service;

import com.jiedan.dto.ExchangeItemResponse;
import com.jiedan.dto.ExchangeRequest;
import com.jiedan.dto.ExchangeResponse;
import com.jiedan.entity.ExchangeItem;
import com.jiedan.entity.ExchangeOrder;
import com.jiedan.entity.PointRecord;
import com.jiedan.entity.User;
import com.jiedan.repository.ExchangeItemRepository;
import com.jiedan.repository.ExchangeOrderRepository;
import com.jiedan.repository.PointRecordRepository;
import com.jiedan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PointsExchangeService {

    private final ExchangeItemRepository exchangeItemRepository;
    private final ExchangeOrderRepository exchangeOrderRepository;
    private final PointRecordRepository pointRecordRepository;
    private final UserRepository userRepository;

    /**
     * 获取所有可兑换的商品列表
     */
    public List<ExchangeItemResponse> getExchangeItems() {
        return exchangeItemRepository.findByIsActiveTrueAndStockGreaterThan(0)
                .stream()
                .map(this::convertToItemResponse)
                .collect(Collectors.toList());
    }

    /**
     * 分页获取可兑换的商品列表
     */
    public Page<ExchangeItemResponse> getExchangeItems(Pageable pageable) {
        return exchangeItemRepository.findByIsActiveTrue(pageable)
                .map(this::convertToItemResponse);
    }

    /**
     * 积分兑换商品
     */
    @Transactional
    public ExchangeResponse exchange(Long userId, ExchangeRequest request) {
        // 获取用户信息
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 获取商品信息
        ExchangeItem item = exchangeItemRepository.findById(request.getItemId())
                .orElseThrow(() -> new RuntimeException("兑换商品不存在"));

        // 检查商品是否可兑换
        if (!item.getIsActive()) {
            throw new RuntimeException("该商品已下架");
        }

        // 检查库存
        if (item.getStock() <= 0) {
            throw new RuntimeException("商品库存不足");
        }

        // 计算所需积分
        int quantity = request.getQuantity() != null ? request.getQuantity() : 1;
        int totalPointsRequired = item.getPointsRequired() * quantity;

        // 检查用户积分是否足够
        if (user.getTotalPoints() < totalPointsRequired) {
            throw new RuntimeException("积分不足，当前积分: " + user.getTotalPoints() + ", 需要积分: " + totalPointsRequired);
        }

        // 创建兑换订单
        ExchangeOrder order = new ExchangeOrder();
        order.setUserId(userId);
        order.setItemId(item.getId());
        order.setPointsUsed(totalPointsRequired);
        order.setCashPaid(request.getCashPaid());
        order.setStatus(ExchangeOrder.ExchangeOrderStatus.PENDING);
        order.setRemark(request.getRemark());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order = exchangeOrderRepository.save(order);

        // 扣除用户积分
        int newBalance = user.getTotalPoints() - totalPointsRequired;
        user.setTotalPoints(newBalance);
        userRepository.save(user);

        // 创建积分记录
        PointRecord pointRecord = new PointRecord();
        pointRecord.setUserId(userId);
        pointRecord.setType("EXCHANGE");
        pointRecord.setAmount(-totalPointsRequired);
        pointRecord.setBalance(newBalance);
        pointRecord.setDescription("兑换商品: " + item.getName() + " x" + quantity);
        pointRecord.setCreatedAt(LocalDateTime.now());
        pointRecordRepository.save(pointRecord);

        // 扣除库存
        item.setStock(item.getStock() - quantity);
        exchangeItemRepository.save(item);

        return convertToExchangeResponse(order, item);
    }

    /**
     * 获取用户的兑换订单列表
     */
    public Page<ExchangeResponse> getExchangeOrders(Long userId, Pageable pageable) {
        return exchangeOrderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(order -> {
                    ExchangeItem item = exchangeItemRepository.findById(order.getItemId()).orElse(null);
                    return convertToExchangeResponse(order, item);
                });
    }

    /**
     * 获取用户的所有兑换订单
     */
    public List<ExchangeResponse> getExchangeOrders(Long userId) {
        return exchangeOrderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(order -> {
                    ExchangeItem item = exchangeItemRepository.findById(order.getItemId()).orElse(null);
                    return convertToExchangeResponse(order, item);
                })
                .collect(Collectors.toList());
    }

    private ExchangeItemResponse convertToItemResponse(ExchangeItem item) {
        ExchangeItemResponse response = new ExchangeItemResponse();
        response.setId(item.getId());
        response.setName(item.getName());
        response.setDescription(item.getDescription());
        response.setImageUrl(item.getImageUrl());
        response.setPointsRequired(item.getPointsRequired());
        response.setCashPrice(item.getCashPrice());
        response.setStock(item.getStock());
        response.setIsActive(item.getIsActive());
        response.setCreatedAt(item.getCreatedAt());
        return response;
    }

    private ExchangeResponse convertToExchangeResponse(ExchangeOrder order, ExchangeItem item) {
        ExchangeResponse response = new ExchangeResponse();
        response.setId(order.getId());
        response.setItemId(order.getItemId());
        response.setItemName(item != null ? item.getName() : "未知商品");
        response.setItemImageUrl(item != null ? item.getImageUrl() : null);
        response.setPointsUsed(order.getPointsUsed());
        response.setCashPaid(order.getCashPaid());
        response.setStatus(order.getStatus());
        response.setRemark(order.getRemark());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        return response;
    }
}
