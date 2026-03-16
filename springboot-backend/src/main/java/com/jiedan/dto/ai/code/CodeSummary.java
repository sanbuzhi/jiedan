package com.jiedan.dto.ai.code;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 代码摘要DTO（用于上下文传递）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeSummary {
    
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 任务名称
     */
    private String taskName;
    
    /**
     * 文件摘要列表
     */
    private List<FileSummary> files;
}
