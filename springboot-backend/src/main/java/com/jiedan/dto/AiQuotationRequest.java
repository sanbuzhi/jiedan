package com.jiedan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiQuotationRequest {

    private Long requirementId;

    private String requirementDescription;

    private List<Long> selectedFunctionIds;

    private List<CustomFunctionDto> customFunctions;

    private String visualStyle;

    private String deploymentMode;

    private String urgency;

    private MaterialsDto materials;
}
