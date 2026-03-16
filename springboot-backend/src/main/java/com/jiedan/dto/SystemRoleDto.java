package com.jiedan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemRoleDto {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String responsibilities;
    private Integer sortOrder;
    private List<SystemFunctionDto> functions;
}
