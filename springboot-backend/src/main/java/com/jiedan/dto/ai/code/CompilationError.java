package com.jiedan.dto.ai.code;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 编译错误DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompilationError {
    
    /**
     * 错误文件路径
     */
    private String filePath;
    
    /**
     * 错误行号
     */
    private Integer lineNumber;
    
    /**
     * 错误信息
     */
    private String message;
    
    /**
     * 错误级别（error/warning）
     */
    private String severity;
}
