package com.jiedan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponse {
    private BigDecimal aiDevelopmentFee;
    private BigDecimal platformServiceFee;
    private BigDecimal totalBudget;
    private String currency;
}
