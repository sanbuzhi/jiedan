package com.jiedan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private Long requirementId;
    private String userId;
    private BigDecimal amount;
    private String paymentType;
    private String status;
    private LocalDateTime paidAt;
    private String paymentMethod;
    private String transactionId;
    private LocalDateTime refundedAt;
    private BigDecimal refundAmount;
    private String refundReason;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
