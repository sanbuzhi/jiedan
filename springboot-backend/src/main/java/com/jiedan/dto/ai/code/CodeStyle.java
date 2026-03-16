package com.jiedan.dto.ai.code;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 代码风格DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeStyle {
    
    /**
     * 缩进方式（4_spaces/2_spaces/tab）
     */
    private String indentation;
    
    /**
     * 命名规范（camelCase/snake_case）
     */
    private String namingConvention;
    
    /**
     * 包结构
     */
    private String packageStructure;
}
