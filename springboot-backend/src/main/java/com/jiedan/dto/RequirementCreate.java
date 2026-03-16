package com.jiedan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequirementCreate {

    // ========== 基础信息字段（step页面使用）==========
    private String userType;

    private String userTypeOther;

    private String projectType;

    private String projectTypeOther;

    private Boolean needOnline;

    private Map<String, Object> traffic;

    private String urgency;

    private Integer deliveryPeriod;

    private String uiStyle;

    // ========== 详细需求字段（step_all页面使用）==========
    private String requirementDescription;

    private List<String> selectedFunctions;

    private List<Map<String, Object>> customFunctions;

    private Map<String, Object> materials;

    private String visualStyle;

    private String deploymentMode;

    private Map<String, Object> quotation;
}
