package com.jiedan.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRequest {

    @NotNull(message = "商品ID不能为空")
    private Long itemId;

    @Min(value = 1, message = "兑换数量至少为1")
    private Integer quantity = 1;

    private BigDecimal cashPaid;

    private String remark;
}
