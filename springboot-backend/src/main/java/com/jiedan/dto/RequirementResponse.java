package com.jiedan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequirementResponse {
    private Long id;
    private String userId;
    private String userType;
    private String userTypeOther;
    private String projectType;
    private String projectTypeOther;
    private Boolean needOnline;
    private TrafficData traffic;
    private String urgency;
    private Integer deliveryPeriod;
    private String uiStyle;
    private String status;
    private BigDecimal budgetCalculated;
    private Map<String, Object> stepData;
    private List<FlowNode> flowNodeStatus;
    private Integer currentFlowNode;
    private String requirementDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
