package com.jiedan.controller;

import com.jiedan.dto.ApiResponse;
import com.jiedan.dto.ExchangeItemResponse;
import com.jiedan.dto.ExchangeRequest;
import com.jiedan.dto.ExchangeResponse;
import com.jiedan.security.CurrentUser;
import com.jiedan.service.PointsExchangeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class PointsExchangeController {

    private final PointsExchangeService pointsExchangeService;

    /**
     * 获取兑换商品列表
     * GET /api/v1/exchange-items
     */
    @GetMapping("/exchange-items")
    public ResponseEntity<ApiResponse<List<ExchangeItemResponse>>> getExchangeItems() {
        List<ExchangeItemResponse> items = pointsExchangeService.getExchangeItems();
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    /**
     * 分页获取兑换商品列表
     * GET /api/v1/exchange-items/paged
     */
    @GetMapping("/exchange-items/paged")
    public ResponseEntity<ApiResponse<Page<ExchangeItemResponse>>> getExchangeItemsPaged(Pageable pageable) {
        Page<ExchangeItemResponse> items = pointsExchangeService.getExchangeItems(pageable);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    /**
     * 积分兑换商品
     * POST /api/v1/points/exchange
     */
    @PostMapping("/points/exchange")
    public ResponseEntity<ApiResponse<ExchangeResponse>> exchange(
            @CurrentUser Long userId,
            @Valid @RequestBody ExchangeRequest request) {
        ExchangeResponse response = pointsExchangeService.exchange(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 获取用户的兑换订单列表
     * GET /api/v1/points/exchange-orders
     */
    @GetMapping("/points/exchange-orders")
    public ResponseEntity<ApiResponse<Page<ExchangeResponse>>> getExchangeOrders(
            @CurrentUser Long userId,
            Pageable pageable) {
        Page<ExchangeResponse> orders = pointsExchangeService.getExchangeOrders(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    /**
     * 获取用户的所有兑换订单（不分页）
     * GET /api/v1/points/exchange-orders/all
     */
    @GetMapping("/points/exchange-orders/all")
    public ResponseEntity<ApiResponse<List<ExchangeResponse>>> getAllExchangeOrders(
            @CurrentUser Long userId) {
        List<ExchangeResponse> orders = pointsExchangeService.getExchangeOrders(userId);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
}
