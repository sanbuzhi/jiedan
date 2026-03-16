package com.jiedan.dto;

import com.jiedan.entity.ExchangeOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeResponse {

    private Long id;
    private Long itemId;
    private String itemName;
    private String itemImageUrl;
    private Integer pointsUsed;
    private BigDecimal cashPaid;
    private ExchangeOrder.ExchangeOrderStatus status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
