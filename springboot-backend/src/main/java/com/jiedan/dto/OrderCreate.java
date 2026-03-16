package com.jiedan.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreate {
    @NotNull(message = "需求ID不能为空")
    private Long requirementId;

    @NotNull(message = "订单金额不能为空")
    private BigDecimal amount;

    @NotNull(message = "支付类型不能为空")
    private String paymentType;
}
