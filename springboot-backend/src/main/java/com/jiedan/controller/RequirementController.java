package com.jiedan.controller;

import com.jiedan.dto.*;
import com.jiedan.entity.Requirement;
import com.jiedan.repository.RequirementRepository;
import com.jiedan.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/v1/requirements")
@RequiredArgsConstructor
public class RequirementController {

    private final RequirementRepository requirementRepository;

    @PostMapping
    public ResponseEntity<RequirementResponse> createRequirement(
            @CurrentUser Long userId,
            @Valid @RequestBody RequirementCreate request) {
        Requirement requirement = new Requirement();
        requirement.setUserId(userId);
        
        // ========== 基础信息字段（兼容step页面）==========
        requirement.setUserType(request.getUserType() != null ? request.getUserType() : "individual");
        requirement.setUserTypeOther(request.getUserTypeOther());
        requirement.setProjectType(request.getProjectType() != null ? request.getProjectType() : "website");
        requirement.setProjectTypeOther(request.getProjectTypeOther());
        requirement.setNeedOnline(request.getNeedOnline() != null ? request.getNeedOnline() : false);
        if (request.getTraffic() != null) {
            requirement.setTraffic(request.getTraffic());
        }
        requirement.setUrgency(request.getUrgency() != null ? request.getUrgency() : "normal");
        requirement.setDeliveryPeriod(request.getDeliveryPeriod() != null ? request.getDeliveryPeriod() : 30);
        // 兼容uiStyle和visualStyle两个字段名
        String style = request.getUiStyle() != null ? request.getUiStyle() : request.getVisualStyle();
        requirement.setUiStyle(style != null ? style : "modern");
        
        // ========== 详细需求字段（兼容step_all页面）==========
        requirement.setRequirementDescription(request.getRequirementDescription());
        
        // 处理selectedFunctions - 转换为Map存储
        if (request.getSelectedFunctions() != null) {
            Map<String, Object> selectedFuncsMap = new HashMap<>();
            selectedFuncsMap.put("functionIds", request.getSelectedFunctions());
            requirement.setSelectedFunctions(selectedFuncsMap);
        }
        
        // 处理customFunctions - 存储到stepData中
        if (request.getCustomFunctions() != null || request.getMaterials() != null || request.getQuotation() != null) {
            Map<String, Object> stepData = new HashMap<>();
            if (request.getCustomFunctions() != null) {
                stepData.put("customFunctions", request.getCustomFunctions());
            }
            if (request.getMaterials() != null) {
                stepData.put("materials", request.getMaterials());
                requirement.setMaterials(request.getMaterials());
            }
            if (request.getQuotation() != null) {
                stepData.put("quotation", request.getQuotation());
                // 同时保存到aiQuotationResult（转为JSON字符串）
                try {
                    requirement.setAiQuotationResult(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(request.getQuotation()));
                } catch (Exception e) {
                    // 忽略序列化错误
                }
            }
            requirement.setStepData(stepData);
        } else {
            requirement.setStepData(new HashMap<>());
        }
        
        // 部署模式
        requirement.setDeploymentMode(request.getDeploymentMode());
        
        requirement.setStatus("draft");
        requirement.setCurrentFlowNode(0);

        requirementRepository.save(requirement);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToResponse(requirement));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listRequirements(
            @CurrentUser Long userId,
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(skip / limit, limit);
        Page<Requirement> requirements = requirementRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("items", requirements.getContent().stream().map(this::convertToResponse).toList());
        response.put("total", requirements.getTotalElements());
        response.put("skip", skip);
        response.put("limit", limit);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RequirementResponse> getRequirement(
            @CurrentUser Long userId,
            @PathVariable Long id) {
        Requirement requirement = requirementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("需求不存在"));

        if (!requirement.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(convertToResponse(requirement));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RequirementResponse> updateRequirement(
            @CurrentUser Long userId,
            @PathVariable Long id,
            @Valid @RequestBody RequirementUpdate request) {
        Requirement requirement = requirementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("需求不存在"));

        if (!requirement.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // ========== 基础信息字段 ==========
        if (request.getUserType() != null) {
            requirement.setUserType(request.getUserType());
        }
        if (request.getUserTypeOther() != null) {
            requirement.setUserTypeOther(request.getUserTypeOther());
        }
        if (request.getProjectType() != null) {
            requirement.setProjectType(request.getProjectType());
        }
        if (request.getProjectTypeOther() != null) {
            requirement.setProjectTypeOther(request.getProjectTypeOther());
        }
        if (request.getNeedOnline() != null) {
            requirement.setNeedOnline(request.getNeedOnline());
        }
        if (request.getTraffic() != null) {
            requirement.setTraffic(request.getTraffic());
        }
        if (request.getUrgency() != null) {
            requirement.setUrgency(request.getUrgency());
        }
        if (request.getDeliveryPeriod() != null) {
            requirement.setDeliveryPeriod(request.getDeliveryPeriod());
        }
        // 兼容uiStyle和visualStyle两个字段名
        if (request.getUiStyle() != null) {
            requirement.setUiStyle(request.getUiStyle());
        } else if (request.getVisualStyle() != null) {
            requirement.setUiStyle(request.getVisualStyle());
        }
        
        // ========== 详细需求字段（step_all页面使用）==========
        if (request.getRequirementDescription() != null) {
            requirement.setRequirementDescription(request.getRequirementDescription());
        }
        
        // 处理selectedFunctions
        if (request.getSelectedFunctions() != null) {
            Map<String, Object> selectedFuncsMap = new HashMap<>();
            selectedFuncsMap.put("functionIds", request.getSelectedFunctions());
            requirement.setSelectedFunctions(selectedFuncsMap);
        }
        
        // 处理materials
        if (request.getMaterials() != null) {
            requirement.setMaterials(request.getMaterials());
        }
        
        // 处理deploymentMode
        if (request.getDeploymentMode() != null) {
            requirement.setDeploymentMode(request.getDeploymentMode());
        }
        
        // 处理customFunctions和quotation - 存储到stepData
        if (request.getCustomFunctions() != null || request.getQuotation() != null) {
            Map<String, Object> stepData = requirement.getStepData();
            if (stepData == null) {
                stepData = new HashMap<>();
            }
            if (request.getCustomFunctions() != null) {
                stepData.put("customFunctions", request.getCustomFunctions());
            }
            if (request.getQuotation() != null) {
                stepData.put("quotation", request.getQuotation());
                // 同时保存到aiQuotationResult
                try {
                    requirement.setAiQuotationResult(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(request.getQuotation()));
                } catch (Exception e) {
                    // 忽略序列化错误
                }
            }
            requirement.setStepData(stepData);
        }

        requirementRepository.save(requirement);
        return ResponseEntity.ok(convertToResponse(requirement));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequirement(
            @CurrentUser Long userId,
            @PathVariable Long id) {
        Requirement requirement = requirementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("需求不存在"));

        if (!requirement.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        requirementRepository.delete(requirement);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/budget")
    public ResponseEntity<BudgetResponse> calculateBudget(
            @CurrentUser Long userId,
            @PathVariable Long id) {
        Requirement requirement = requirementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("需求不存在"));

        if (!requirement.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        BigDecimal aiDevelopmentFee = calculateAiDevelopmentFee(requirement);
        BigDecimal platformServiceFee = aiDevelopmentFee.multiply(new BigDecimal("0.1"));
        BigDecimal totalBudget = aiDevelopmentFee.add(platformServiceFee);

        Map<String, Object> budgetCalculated = new HashMap<>();
        budgetCalculated.put("aiDevelopmentFee", aiDevelopmentFee);
        budgetCalculated.put("platformServiceFee", platformServiceFee);
        budgetCalculated.put("totalBudget", totalBudget);
        requirement.setBudgetCalculated(budgetCalculated);
        requirementRepository.save(requirement);

        BudgetResponse response = new BudgetResponse();
        response.setAiDevelopmentFee(aiDevelopmentFee);
        response.setPlatformServiceFee(platformServiceFee);
        response.setTotalBudget(totalBudget);
        response.setCurrency("CNY");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/step-data")
    public ResponseEntity<Void> saveStepData(
            @CurrentUser Long userId,
            @PathVariable Long id,
            @Valid @RequestBody StepDataSave request) {
        Requirement requirement = requirementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("需求不存在"));

        if (!requirement.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Map<String, Object> stepData = requirement.getStepData();
        if (stepData == null) {
            stepData = new HashMap<>();
        }
        stepData.put(String.valueOf(request.getStep()), request.getData());
        requirement.setStepData(stepData);
        requirementRepository.save(requirement);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/step-data")
    public ResponseEntity<StepDataResponse> getStepData(
            @CurrentUser Long userId,
            @PathVariable Long id) {
        Requirement requirement = requirementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("需求不存在"));

        if (!requirement.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        StepDataResponse response = new StepDataResponse();
        response.setStepData(requirement.getStepData());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/flow-status")
    public ResponseEntity<FlowNodeStatusResponse> getFlowStatus(
            @CurrentUser Long userId,
            @PathVariable Long id) {
        Requirement requirement = requirementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("需求不存在"));

        if (!requirement.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 使用 currentFlowNode 计算节点状态
        List<FlowNode> flowNodes = calculateFlowNodes(requirement.getCurrentFlowNode());

        FlowNodeStatusResponse response = new FlowNodeStatusResponse();
        response.setFlowNodeStatus(flowNodes);
        response.setCurrentFlowNode(requirement.getCurrentFlowNode());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/flow-status")
    public ResponseEntity<FlowNodeStatusResponse> updateFlowStatus(
            @CurrentUser Long userId,
            @PathVariable Long id,
            @Valid @RequestBody FlowNodeUpdate request) {
        Requirement requirement = requirementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("需求不存在"));

        if (!requirement.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        requirement.setCurrentFlowNode(request.getNodeIndex());
        requirementRepository.save(requirement);

        return getFlowStatus(userId, id);
    }

    private List<FlowNode> calculateFlowNodes(Integer currentFlowNode) {
        // 定义11个固定节点（增加AI安全测试）
        String[] titles = {
            "明确需求", 
            "AI明确需求", 
            "客户验收", 
            "AI拆分任务", 
            "AI开发", 
            "AI功能测试", 
            "AI安全测试",
            "客户验收", 
            "打包交付", 
            "客户验收", 
            "项目完成"
        };
        String[] descs = {
            "客户提交初步需求", 
            "AI分析并完善需求", 
            "确认需求文档", 
            "自动拆分子任务", 
            "智能编码实现", 
            "自动化功能测试",
            "测试接口漏洞",
            "功能验收测试", 
            "项目交付", 
            "最终验收", 
            "项目结束"
        };
        
        List<FlowNode> flowNodes = new ArrayList<>();
        int current = currentFlowNode != null ? currentFlowNode : 0;
        
        for (int i = 0; i < 11; i++) {
            FlowNode node = new FlowNode();
            node.setTitle(titles[i]);
            node.setDesc(descs[i]);
            
            // 根据 currentFlowNode 计算状态
            if (i < current) {
                node.setStatus("completed");
            } else if (i == current) {
                node.setStatus("active");
            } else {
                node.setStatus("pending");
            }
            
            flowNodes.add(node);
        }
        
        return flowNodes;
    }

    private BigDecimal calculateAiDevelopmentFee(Requirement requirement) {
        BigDecimal baseFee = new BigDecimal("5000");

        if (requirement.getDeliveryPeriod() != null && requirement.getDeliveryPeriod() < 7) {
            baseFee = baseFee.multiply(new BigDecimal("1.5"));
        }

        if ("urgent".equals(requirement.getUrgency())) {
            baseFee = baseFee.multiply(new BigDecimal("1.3"));
        }

        return baseFee;
    }

    private RequirementResponse convertToResponse(Requirement requirement) {
        RequirementResponse response = new RequirementResponse();
        response.setId(requirement.getId());
        response.setUserId(requirement.getUserId().toString());
        response.setUserType(requirement.getUserType());
        response.setUserTypeOther(requirement.getUserTypeOther());
        response.setProjectType(requirement.getProjectType());
        response.setProjectTypeOther(requirement.getProjectTypeOther());
        response.setNeedOnline(requirement.getNeedOnline());

        if (requirement.getTraffic() != null) {
            TrafficData traffic = new TrafficData();
            Object totalUsers = requirement.getTraffic().get("totalUsers");
            Object dau = requirement.getTraffic().get("dau");
            Object concurrent = requirement.getTraffic().get("concurrent");
            if (totalUsers != null) traffic.setTotalUsers((Integer) totalUsers);
            if (dau != null) traffic.setDau((Integer) dau);
            if (concurrent != null) traffic.setConcurrent((Integer) concurrent);
            response.setTraffic(traffic);
        }

        response.setUrgency(requirement.getUrgency());
        response.setDeliveryPeriod(requirement.getDeliveryPeriod());
        response.setUiStyle(requirement.getUiStyle());
        response.setStatus(requirement.getStatus());

        if (requirement.getBudgetCalculated() != null) {
            Object totalBudget = requirement.getBudgetCalculated().get("totalBudget");
            if (totalBudget != null) {
                response.setBudgetCalculated(new BigDecimal(totalBudget.toString()));
            }
        }

        response.setStepData(requirement.getStepData());

        // 使用 currentFlowNode 计算节点状态
        List<FlowNode> flowNodes = calculateFlowNodes(requirement.getCurrentFlowNode());
        response.setFlowNodeStatus(flowNodes);
        response.setCurrentFlowNode(requirement.getCurrentFlowNode());
        response.setRequirementDescription(requirement.getRequirementDescription());
        response.setCreatedAt(requirement.getCreatedAt());
        response.setUpdatedAt(requirement.getUpdatedAt());

        return response;
    }

    // ========== 客户验收相关接口 ==========

    /**
     * 阶段验收通过
     * POST /v1/requirements/{id}/approve
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApproveResponse> approveStage(
            @CurrentUser Long userId,
            @PathVariable Long id,
            @Valid @RequestBody ApproveRequest request) {
        Requirement requirement = requirementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("需求不存在"));

        if (!requirement.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 更新需求状态，进入下一阶段
        Integer currentNode = requirement.getCurrentFlowNode();
        if (currentNode == null) {
            currentNode = 0;
        }

        // 根据当前阶段跳转到下一阶段
        Integer nextNode = calculateNextNode(currentNode, request.getStage());
        requirement.setCurrentFlowNode(nextNode);

        // 保存审批记录
        Map<String, Object> stepData = requirement.getStepData();
        if (stepData == null) {
            stepData = new HashMap<>();
        }
        Map<String, Object> approvalRecord = new HashMap<>();
        approvalRecord.put("stage", request.getStage());
        approvalRecord.put("action", "approve");
        approvalRecord.put("timestamp", new Date());
        stepData.put("approval_" + request.getStage(), approvalRecord);
        requirement.setStepData(stepData);

        requirementRepository.save(requirement);

        ApproveResponse response = new ApproveResponse();
        response.setSuccess(true);
        response.setMessage("验收通过");
        response.setCurrentFlowNode(nextNode);

        return ResponseEntity.ok(response);
    }

    /**
     * 提交建议
     * POST /v1/requirements/{id}/suggestion
     */
    @PostMapping("/{id}/suggestion")
    public ResponseEntity<SuggestionResponse> submitSuggestion(
            @CurrentUser Long userId,
            @PathVariable Long id,
            @Valid @RequestBody SuggestionRequest request) {
        Requirement requirement = requirementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("需求不存在"));

        if (!requirement.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 保存建议到stepData
        Map<String, Object> stepData = requirement.getStepData();
        if (stepData == null) {
            stepData = new HashMap<>();
        }

        Map<String, Object> suggestionRecord = new HashMap<>();
        suggestionRecord.put("stage", request.getStage());
        suggestionRecord.put("suggestion", request.getSuggestion());
        suggestionRecord.put("timestamp", new Date());
        stepData.put("suggestion_" + request.getStage() + "_" + System.currentTimeMillis(), suggestionRecord);
        requirement.setStepData(stepData);

        requirementRepository.save(requirement);

        SuggestionResponse response = new SuggestionResponse();
        response.setSuccess(true);
        response.setMessage("建议已提交");

        return ResponseEntity.ok(response);
    }

    private Integer calculateNextNode(Integer currentNode, String stage) {
        // 根据阶段名称计算下一个节点
        return switch (stage) {
            case "clarify" -> 3; // AI明确需求 -> AI拆分任务
            case "split" -> 4;   // AI拆分任务 -> AI开发
            case "develop" -> 5; // AI开发 -> AI功能测试
            case "test" -> 6;    // AI功能测试 -> AI安全测试
            case "security" -> 7; // AI安全测试 -> 客户验收
            default -> currentNode + 1;
        };
    }
}
