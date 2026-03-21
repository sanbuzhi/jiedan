package com.jiedan.dto.ai.code;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartProjectResponse {
    private String projectId;
    private String status;
    private String message;

    public static StartProjectResponse success(String projectId) {
        return new StartProjectResponse(projectId, "PROCESSING", "项目开发已启动");
    }

    public static StartProjectResponse fail(String projectId, String errorMessage) {
        return new StartProjectResponse(projectId, "FAILED", errorMessage);
    }
}
