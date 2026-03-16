package com.jiedan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeItemResponse {

    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private Integer pointsRequired;
    private BigDecimal cashPrice;
    private Integer stock;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
