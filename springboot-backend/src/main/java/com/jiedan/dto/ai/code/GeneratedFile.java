package com.jiedan.dto.ai.code;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 生成的代码文件DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedFile {
    
    /**
     * 文件路径（相对路径）
     */
    private String path;
    
    /**
     * 文件内容
     */
    private String content;
    
    /**
     * 编程语言
     */
    private String language;
    
    /**
     * 文件说明
     */
    private String description;
    
    /**
     * 是否需要重新生成（用于修复循环）
     */
    private Boolean needsRegeneration;
    
    /**
     * 错误信息（如果有）
     */
    private String errorMessage;
}
