package com.jiedan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiQuotationResponse {

    private Long requirementId;

    private BigDecimal totalAmount;

    private String currency = "CNY";

    private Integer estimatedDays;

    private QuotationBreakdown breakdown;

    private List<String> serviceCommitments;
}
