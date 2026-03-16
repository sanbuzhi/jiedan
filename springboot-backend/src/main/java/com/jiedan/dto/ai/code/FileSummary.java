package com.jiedan.dto.ai.code;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文件摘要DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileSummary {
    
    /**
     * 文件路径
     */
    private String filePath;
    
    /**
     * 类名
     */
    private String className;
    
    /**
     * public方法签名列表
     */
    private List<String> publicMethods;
    
    /**
     * 依赖的其他类
     */
    private List<String> dependencies;
}
