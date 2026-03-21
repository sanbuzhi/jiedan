package com.jiedan.dto.ai.code;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartProjectRequest {
    private Long requirementId;
    private String projectId;
}
