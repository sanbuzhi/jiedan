package com.jiedan.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPayRequest {
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;

    @NotBlank(message = "交易ID不能为空")
    private String transactionId;
}
