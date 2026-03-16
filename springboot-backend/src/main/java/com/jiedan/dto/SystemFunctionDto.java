package com.jiedan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemFunctionDto {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String complexity;
    private Integer estimatedHours;
    private BigDecimal basePrice;
    private Integer priority;
}
