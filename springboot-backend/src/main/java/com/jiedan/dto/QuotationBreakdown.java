package com.jiedan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuotationBreakdown {

    private BigDecimal aiDevelopmentCost;

    private BigDecimal platformServiceFee;

    private BigDecimal infrastructureCost;

    private InfrastructureDetail infrastructure;

    private List<DevelopmentPhaseCost> developmentPhases;
}
