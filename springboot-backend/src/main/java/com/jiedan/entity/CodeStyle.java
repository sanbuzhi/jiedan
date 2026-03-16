package com.jiedan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 代码风格实体
 */
@Entity
@Table(name = "code_style")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeStyle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 项目ID
     */
    @Column(name = "project_id", length = 64, unique = true, nullable = false)
    private String projectId;

    /**
     * 缩进方式（4_spaces/2_spaces/tab）
     */
    @Column(name = "indentation", length = 20)
    private String indentation;

    /**
     * 命名规范（camelCase/snake_case）
     */
    @Column(name = "naming_convention", length = 50)
    private String namingConvention;

    /**
     * 包结构
     */
    @Column(name = "package_structure", length = 200)
    private String packageStructure;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
