package com.jiedan.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 任务项DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskItem {

    /**
     * 任务ID
     */
    private String id;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 预估工时（小时）
     */
    private Integer estimatedHours;

    /**
     * 优先级：高/中/低
     */
    private String priority;

    /**
     * 依赖任务ID列表
     */
    private List<String> dependencies;

    /**
     * 所属模块
     */
    private String module;
}
