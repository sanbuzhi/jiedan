package com.jiedan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialsDto {

    private List<String> referenceImages;

    private String sourceCodePackage;

    private String repositoryUrl;
}
