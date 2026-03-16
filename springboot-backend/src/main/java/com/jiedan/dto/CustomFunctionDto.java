package com.jiedan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomFunctionDto {

    private String roleName;

    private String functionName;

    private String description;

    private String complexity;
}
