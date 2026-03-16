package com.jiedan.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRefundRequest {
    @NotNull(message = "退款金额不能为空")
    private BigDecimal refundAmount;

    private String refundReason;
}
