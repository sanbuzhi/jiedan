package com.jiedan.dto;

import com.jiedan.entity.enums.DevelopmentPhase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DevelopmentPhaseCost {

    private DevelopmentPhase phaseName;

    private BigDecimal cost;

    private String description;
}
