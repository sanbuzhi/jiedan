package com.jiedan.dto.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * AI生成代码请求DTO
 * 【重构】基于需求书和任务书生成完整项目代码
 * 【优化】支持模块化生成，每个模块单独调用AI
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateCodeRequest {

    /**
     * 项目ID（用于Feedback Shadow验证）
     */
    private String projectId;

    /**
     * 【核心输入】需求文档（Markdown格式）
     * 包含：需求概述、功能模块、用户角色、业务流程、非功能性需求等
     */
    private String requirementDoc;

    /**
     * 【核心输入】任务文档（Markdown格式）
     * 包含：技术规格、前端页面清单、后端接口清单、数据库设计、开发顺序等
     */
    private String taskDoc;

    /**
     * 【模块化生成】当前要生成的模块名称
     * 例如："用户管理模块"、"订单管理模块"、"数据库初始化"等
     */
    private String moduleName;

    /**
     * 【模块化生成】当前模块要生成的文件清单
     * 例如：["User.java", "UserMapper.java", "UserController.java"]
     */
    private List<String> fileList;

    /**
     * 【模块化生成】当前模块的优先级/顺序
     * 用于控制生成顺序：1-数据库和实体类, 2-Mapper和Service, 3-Controller和前端页面
     */
    private Integer moduleOrder;

    /**
     * 【模块化生成】是否是最后一个模块
     * 用于触发最终整合
     */
    private Boolean isLastModule;

    /**
     * 【模块化生成】已生成模块的代码摘要
     * 用于保持模块间一致性，格式：模块名 -> 关键类/接口列表
     */
    private Map<String, String> generatedModulesSummary;

    /**
     * 编程语言（可选，从任务书自动推断）
     */
    private String language;

    /**
     * 框架/技术栈（可选，从任务书自动推断）
     */
    private String framework;

    /**
     * 特殊要求
     */
    private List<String> requirements;

    /**
     * 模型类型（可选）
     */
    private String model;
}
