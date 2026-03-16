package com.jiedan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemTemplateDetailResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String category;
    private List<SystemRoleDto> roles;
}
