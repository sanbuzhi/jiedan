package com.jiedan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import com.jiedan.entity.enums.AiQuotationStatus;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

@Entity
@Table(name = "requirements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Requirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_type", length = 50, nullable = false)
    private String userType;

    @Column(name = "user_type_other", length = 100)
    private String userTypeOther;

    @Column(name = "project_type", length = 50, nullable = false)
    private String projectType;

    @Column(name = "project_type_other", length = 100)
    private String projectTypeOther;

    @Column(name = "need_online")
    private Boolean needOnline = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "traffic", columnDefinition = "json")
    private Map<String, Object> traffic;

    @Column(name = "urgency", length = 50)
    private String urgency;

    @Column(name = "delivery_period")
    private Integer deliveryPeriod;

    @Column(name = "ui_style", length = 200)
    private String uiStyle;

    @Column(name = "status", length = 50)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "budget_calculated", columnDefinition = "json")
    private Map<String, Object> budgetCalculated;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "step_data", columnDefinition = "json")
    private Map<String, Object> stepData;

    @Column(name = "current_flow_node", columnDefinition = "INT DEFAULT 0")
    private Integer currentFlowNode = 0;

    @Column(name = "requirement_description", columnDefinition = "TEXT")
    private String requirementDescription;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "selected_functions", columnDefinition = "json")
    private Map<String, Object> selectedFunctions;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "materials", columnDefinition = "json")
    private Map<String, Object> materials;

    @Column(name = "deployment_mode", length = 50)
    private String deploymentMode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_quotation_result", columnDefinition = "json")
    private String aiQuotationResult;

    @Enumerated(EnumType.STRING)
    @Column(name = "ai_quotation_status", length = 20)
    private AiQuotationStatus aiQuotationStatus;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
