package com.jiedan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectedFunctionDto {

    private Long functionId;

    private String functionName;

    private String complexity;

    private Integer estimatedHours;

    private BigDecimal price;
}
