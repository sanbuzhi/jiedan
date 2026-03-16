package com.jiedan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "requirement_functions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequirementFunction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requirement_id", nullable = false)
    private Long requirementId;

    @Column(name = "function_id")
    private Long functionId;

    @Column(name = "function_name", length = 100)
    private String functionName;

    @Column(name = "function_type", length = 20)
    private String functionType;

    @Column(name = "complexity", length = 20)
    private String complexity;

    @Column(name = "is_custom", nullable = false)
    private Boolean isCustom = false;

    @Column(name = "custom_name", length = 100)
    private String customName;

    @Column(name = "custom_role_name", length = 100)
    private String customRoleName;

    @Column(name = "custom_description", columnDefinition = "TEXT")
    private String customDescription;

    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requirement_id", insertable = false, updatable = false)
    private Requirement requirement;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "function_id", insertable = false, updatable = false)
    private SystemFunction systemFunction;
}
