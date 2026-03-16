package com.jiedan.dto.ai.code;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 脚手架配置DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScaffoldConfig {

    /**
     * 需要生成的模块（前端/后端/小程序）
     */
    private List<String> modules;

    /**
     * Java版本
     */
    private String javaVersion;

    /**
     * SpringBoot版本
     */
    private String springBootVersion;

    /**
     * 基础依赖
     */
    private List<String> dependencies;
}
