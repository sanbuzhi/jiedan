package com.jiedan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemTemplateSearchResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String category;
    private Double matchScore;
}
