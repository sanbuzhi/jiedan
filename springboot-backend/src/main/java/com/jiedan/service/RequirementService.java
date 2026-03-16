package com.jiedan.service;

import com.jiedan.dto.*;
import com.jiedan.entity.Requirement;
import com.jiedan.entity.RequirementFunction;
import com.jiedan.repository.RequirementFunctionRepository;
import com.jiedan.repository.RequirementRepository;
import com.jiedan.repository.SystemFunctionRepository;
import com.jiedan.util.BudgetCalculator;
import com.jiedan.util.FlowNodeCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequirementService {

    private final RequirementRepository requirementRepository;
    private final RequirementFunctionRepository requirementFunctionRepository;
    private final SystemFunctionRepository systemFunctionRepository;
    private final BudgetCalculator budgetCalculator;
    private final FlowNodeCalculator flowNodeCalculator;

    @Transactional
    public Requirement createRequirement(Long userId, RequirementCreate dto) {
        Requirement requirement = new Requirement();
        requirement.setUserId(userId);
        requirement.setUserType(dto.getUserType());
        requirement.setUserTypeOther(dto.getUserTypeOther());
        requirement.setProjectType(dto.getProjectType());
        requirement.setProjectTypeOther(dto.getProjectTypeOther());
        requirement.setNeedOnline(dto.getNeedOnline());
        requirement.setTraffic(dto.getTraffic());
        requirement.setUrgency(dto.getUrgency());
        requirement.setDeliveryPeriod(dto.getDeliveryPeriod());
        requirement.setUiStyle(dto.getUiStyle());
        requirement.setStatus("PENDING");
        requirement.setCurrentFlowNode(0);
        requirement.setCreatedAt(LocalDateTime.now());
        requirement.setUpdatedAt(LocalDateTime.now());

        // Calculate and cache budget
        BudgetResponse budget = budgetCalculator.calculate(requirement);
        requirement.setBudgetCalculated(Map.of(
                "aiDevelopmentFee", budget.getAiDevelopmentFee(),
                "platformServiceFee", budget.getPlatformServiceFee(),
                "totalBudget", budget.getTotalBudget()
        ));

        return requirementRepository.save(requirement);
    }

    public Page<Requirement> listRequirements(Long userId, Pageable pageable) {
        return requirementRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Requirement getRequirement(Long requirementId) {
        return requirementRepository.findById(requirementId)
                .orElseThrow(() -> new RuntimeException("需求不存在"));
    }

    @Transactional
    public Requirement updateRequirement(Long requirementId, RequirementUpdate dto) {
        Requirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new RuntimeException("需求不存在"));

        if (dto.getUserType() != null) {
            requirement.setUserType(dto.getUserType());
        }
        if (dto.getUserTypeOther() != null) {
            requirement.setUserTypeOther(dto.getUserTypeOther());
        }
        if (dto.getProjectType() != null) {
            requirement.setProjectType(dto.getProjectType());
        }
        if (dto.getProjectTypeOther() != null) {
            requirement.setProjectTypeOther(dto.getProjectTypeOther());
        }
        if (dto.getNeedOnline() != null) {
            requirement.setNeedOnline(dto.getNeedOnline());
        }
        if (dto.getTraffic() != null) {
            requirement.setTraffic(dto.getTraffic());
        }
        if (dto.getUrgency() != null) {
            requirement.setUrgency(dto.getUrgency());
        }
        if (dto.getDeliveryPeriod() != null) {
            requirement.setDeliveryPeriod(dto.getDeliveryPeriod());
        }
        if (dto.getUiStyle() != null) {
            requirement.setUiStyle(dto.getUiStyle());
        }

        requirement.setUpdatedAt(LocalDateTime.now());
        return requirementRepository.save(requirement);
    }

    @Transactional
    public void deleteRequirement(Long requirementId) {
        requirementRepository.deleteById(requirementId);
    }

    @Transactional
    public void saveStepData(Long requirementId, Integer step, Map<String, Object> data) {
        Requirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new RuntimeException("需求不存在"));

        Map<String, Object> stepData = requirement.getStepData();
        if (stepData == null) {
            stepData = new java.util.HashMap<>();
        }
        stepData.put(String.valueOf(step), data);
        requirement.setStepData(stepData);
        requirement.setUpdatedAt(LocalDateTime.now());

        requirementRepository.save(requirement);
    }

    public Map<String, Object> getStepData(Long requirementId) {
        Requirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new RuntimeException("需求不存在"));

        return requirement.getStepData() != null ? requirement.getStepData() : new java.util.HashMap<>();
    }

    public FlowNodeStatusResponse getFlowStatus(Long requirementId) {
        Requirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new RuntimeException("需求不存在"));

        List<FlowNode> nodes = flowNodeCalculator.calculate(
                requirement.getStatus(),
                requirement.getNeedOnline(),
                requirement.getCurrentFlowNode()
        );

        FlowNodeStatusResponse response = new FlowNodeStatusResponse();
        response.setFlowNodeStatus(nodes);
        response.setCurrentFlowNode(requirement.getCurrentFlowNode());

        return response;
    }

    @Transactional
    public void updateFlowStatus(Long requirementId, Integer nodeIndex, String status) {
        Requirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new RuntimeException("需求不存在"));

        // If setting to active, update current flow node
        if ("active".equals(status)) {
            requirement.setCurrentFlowNode(nodeIndex);
        }

        requirement.setUpdatedAt(LocalDateTime.now());
        requirementRepository.save(requirement);
    }

    @Transactional
    public void saveRequirementFunctions(Long requirementId, List<Long> functionIds, List<CustomFunctionDto> customFunctions) {
        log.info("保存需求功能点, requirementId: {}", requirementId);

        Requirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new RuntimeException("需求不存在: " + requirementId));

        // 删除现有功能点关联
        List<RequirementFunction> existingFunctions = requirementFunctionRepository.findByRequirementId(requirementId);
        if (!existingFunctions.isEmpty()) {
            requirementFunctionRepository.deleteAll(existingFunctions);
        }

        // 保存系统功能点
        if (functionIds != null && !functionIds.isEmpty()) {
            for (Long functionId : functionIds) {
                systemFunctionRepository.findById(functionId).ifPresent(func -> {
                    RequirementFunction rf = new RequirementFunction();
                    rf.setRequirementId(requirementId);
                    rf.setFunctionId(functionId);
                    rf.setFunctionName(func.getName());
                    rf.setFunctionType("SYSTEM");
                    rf.setComplexity(func.getComplexity());
                    requirementFunctionRepository.save(rf);
                });
            }
        }

        // 保存自定义功能点
        if (customFunctions != null && !customFunctions.isEmpty()) {
            for (CustomFunctionDto custom : customFunctions) {
                RequirementFunction rf = new RequirementFunction();
                rf.setRequirementId(requirementId);
                rf.setFunctionName(custom.getFunctionName());
                rf.setFunctionType("CUSTOM");
                rf.setComplexity(custom.getComplexity());
                rf.setCustomRoleName(custom.getRoleName());
                rf.setCustomDescription(custom.getDescription());
                requirementFunctionRepository.save(rf);
            }
        }

        // 更新需求的selectedFunctions字段
        Map<String, Object> selectedFunctions = new HashMap<>();
        selectedFunctions.put("systemFunctionIds", functionIds);
        selectedFunctions.put("customFunctions", customFunctions);
        requirement.setSelectedFunctions(selectedFunctions);
        requirement.setUpdatedAt(LocalDateTime.now());
        requirementRepository.save(requirement);

        log.info("需求功能点保存完成, requirementId: {}", requirementId);
    }

    @Transactional
    public void saveRequirementMaterials(Long requirementId, Map<String, Object> materials) {
        log.info("保存需求资料, requirementId: {}", requirementId);

        Requirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new RuntimeException("需求不存在: " + requirementId));

        if (materials == null) {
            throw new IllegalArgumentException("资料不能为空");
        }

        requirement.setMaterials(materials);
        requirement.setUpdatedAt(LocalDateTime.now());
        requirementRepository.save(requirement);

        log.info("需求资料保存完成, requirementId: {}", requirementId);
    }

    @Transactional
    public void updateRequirementDescription(Long requirementId, String description, String deploymentMode) {
        log.info("更新需求描述, requirementId: {}", requirementId);

        Requirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new RuntimeException("需求不存在: " + requirementId));

        if (description != null && !description.isEmpty()) {
            requirement.setRequirementDescription(description);
        }

        if (deploymentMode != null && !deploymentMode.isEmpty()) {
            requirement.setDeploymentMode(deploymentMode);
        }

        requirement.setUpdatedAt(LocalDateTime.now());
        requirementRepository.save(requirement);

        log.info("需求描述更新完成, requirementId: {}", requirementId);
    }
}
