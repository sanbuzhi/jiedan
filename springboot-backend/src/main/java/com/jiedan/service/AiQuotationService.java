package com.jiedan.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiedan.dto.*;
import com.jiedan.entity.Requirement;
import com.jiedan.entity.enums.AiQuotationStatus;
import com.jiedan.entity.enums.DevelopmentPhase;
import com.jiedan.repository.RequirementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiQuotationService {

    private final RequirementRepository requirementRepository;
    private final ObjectMapper objectMapper;

    private static final Map<String, BigDecimal> VISUAL_STYLE_FACTORS = Map.of(
            "simple", new BigDecimal("0.1"),
            "business", new BigDecimal("0.2"),
            "premium", new BigDecimal("0.3"),
            "custom", new BigDecimal("0.4")
    );

    private static final Map<String, BigDecimal> INFRASTRUCTURE_COSTS = Map.of(
            "cloud", new BigDecimal("2000"),
            "dedicated", new BigDecimal("5000"),
            "hybrid", new BigDecimal("4000"),
            "none", BigDecimal.ZERO
    );

    @Transactional
    public AiQuotationResponse generateQuotation(Long requirementId, AiQuotationRequest request) {
        log.info("开始生成AI估价, requirementId: {}", requirementId);

        Requirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new RuntimeException("需求不存在: " + requirementId));

        validateRequest(request);

        List<SelectedFunctionDto> selectedFunctions = getSelectedFunctions(request.getSelectedFunctionIds());

        BigDecimal basePrice = calculateBasePrice(selectedFunctions);
        BigDecimal visualFee = calculateVisualCustomizationFee(basePrice, request.getVisualStyle());
        BigDecimal infrastructureCost = calculateInfrastructureCost(request.getDeploymentMode());
        BigDecimal platformServiceFee = calculatePlatformServiceFee(basePrice, visualFee);

        BigDecimal totalAmount = basePrice.add(visualFee).add(infrastructureCost).add(platformServiceFee);

        List<DevelopmentPhaseCost> developmentPhases = calculateDevelopmentPhases(basePrice);

        // 只要 deploymentMode 不是 "none"，就返回 infrastructure 对象
        // 包括 null 的情况（默认需要基础设施）
        InfrastructureDetail infrastructure = null;
        if (!"none".equals(request.getDeploymentMode())) {
            infrastructure = calculateInfrastructureDetail(infrastructureCost);
        }

        QuotationBreakdown breakdown = new QuotationBreakdown(
                basePrice,
                platformServiceFee,
                infrastructureCost,
                infrastructure,
                developmentPhases
        );

        Integer estimatedDays = calculateEstimatedDays(selectedFunctions, request.getUrgency());

        List<String> serviceCommitments = generateServiceCommitments(request.getUrgency());

        AiQuotationResponse response = new AiQuotationResponse(
                requirementId,
                totalAmount,
                "CNY",
                estimatedDays,
                breakdown,
                serviceCommitments
        );

        cacheQuotationResult(requirement, response);

        log.info("AI估价生成完成, requirementId: {}, totalAmount: {}", requirementId, totalAmount);

        return response;
    }

    public BigDecimal calculateBasePrice(List<SelectedFunctionDto> functions) {
        if (functions == null || functions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.ZERO;

        for (SelectedFunctionDto function : functions) {
            BigDecimal complexityFactor = getComplexityFactor(function.getComplexity());
            BigDecimal basePrice = function.getPrice() != null ? function.getPrice() : BigDecimal.ZERO;
            BigDecimal adjustedPrice = basePrice.multiply(complexityFactor);
            total = total.add(adjustedPrice);
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateVisualCustomizationFee(BigDecimal basePrice, String visualStyle) {
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal factor = getVisualStyleFactor(visualStyle);
        return basePrice.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateInfrastructureCost(String deploymentMode) {
        if (deploymentMode == null || deploymentMode.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return INFRASTRUCTURE_COSTS.getOrDefault(deploymentMode, new BigDecimal("2000"));
    }

    public BigDecimal calculatePlatformServiceFee(BigDecimal basePrice, BigDecimal visualFee) {
        BigDecimal subtotal = basePrice.add(visualFee);
        return subtotal.multiply(new BigDecimal("0.15")).setScale(2, RoundingMode.HALF_UP);
    }

    private InfrastructureDetail calculateInfrastructureDetail(BigDecimal totalCost) {
        // 即使 totalCost 为 0 或 null，也返回默认的基础设施详情对象
        BigDecimal cost = (totalCost == null || totalCost.compareTo(BigDecimal.ZERO) <= 0) 
                ? new BigDecimal("2000") : totalCost;

        BigDecimal serverCost = cost.multiply(new BigDecimal("0.6")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal domainCost = cost.multiply(new BigDecimal("0.1")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal sslCost = cost.multiply(new BigDecimal("0.1")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal cdnCost = cost.multiply(new BigDecimal("0.2")).setScale(2, RoundingMode.HALF_UP);

        return new InfrastructureDetail(
                new InfrastructureDetail.InfrastructureItem("云服务器", serverCost, "4核8G内存", false),
                new InfrastructureDetail.InfrastructureItem("域名", domainCost, "一年期", false),
                new InfrastructureDetail.InfrastructureItem("SSL证书", sslCost, "一年期", false),
                new InfrastructureDetail.InfrastructureItem("CDN加速", cdnCost, "按需使用", true)
        );
    }

    private List<DevelopmentPhaseCost> calculateDevelopmentPhases(BigDecimal basePrice) {
        List<DevelopmentPhaseCost> phases = new ArrayList<>();

        phases.add(new DevelopmentPhaseCost(
                DevelopmentPhase.REQUIREMENT_ANALYSIS,
                basePrice.multiply(new BigDecimal("0.15")).setScale(2, RoundingMode.HALF_UP),
                "需求分析与确认"
        ));

        phases.add(new DevelopmentPhaseCost(
                DevelopmentPhase.ARCHITECTURE_DESIGN,
                basePrice.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP),
                "系统架构设计"
        ));

        phases.add(new DevelopmentPhaseCost(
                DevelopmentPhase.PROGRAMMING_DEVELOPMENT,
                basePrice.multiply(new BigDecimal("0.40")).setScale(2, RoundingMode.HALF_UP),
                "程序开发实现"
        ));

        phases.add(new DevelopmentPhaseCost(
                DevelopmentPhase.TESTING_ACCEPTANCE,
                basePrice.multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP),
                "测试与验收"
        ));

        phases.add(new DevelopmentPhaseCost(
                DevelopmentPhase.PERFORMANCE_OPTIMIZATION,
                basePrice.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP),
                "性能优化"
        ));

        phases.add(new DevelopmentPhaseCost(
                DevelopmentPhase.SECURITY_HARDENING,
                basePrice.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP),
                "安全加固"
        ));

        return phases;
    }

    private Integer calculateEstimatedDays(List<SelectedFunctionDto> functions, String urgency) {
        int baseDays = 30;

        if (functions != null && !functions.isEmpty()) {
            baseDays = Math.max(baseDays, functions.size() * 5);
        }

        double urgencyFactor = getUrgencyFactor(urgency);
        return (int) (baseDays * urgencyFactor);
    }

    private List<String> generateServiceCommitments(String urgency) {
        List<String> commitments = new ArrayList<>();

        commitments.add("提供完整的源代码和文档");
        commitments.add("提供3个月免费技术支持");
        commitments.add("提供系统部署和配置服务");
        commitments.add("提供操作培训服务");

        if ("urgent".equals(urgency) || "critical".equals(urgency)) {
            commitments.add("提供7x24小时紧急响应服务");
            commitments.add("提供优先技术支持通道");
        }

        return commitments;
    }

    private void cacheQuotationResult(Requirement requirement, AiQuotationResponse response) {
        try {
            String quotationJson = objectMapper.writeValueAsString(response);
            requirement.setAiQuotationResult(quotationJson);
            requirement.setAiQuotationStatus(AiQuotationStatus.COMPLETED);
            requirementRepository.save(requirement);
            log.debug("AI估价结果已缓存, requirementId: {}", requirement.getId());
        } catch (JsonProcessingException e) {
            log.error("缓存AI估价结果失败, requirementId: {}", requirement.getId(), e);
        }
    }

    public AiQuotationResponse getCachedQuotation(Long requirementId) {
        Requirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new RuntimeException("需求不存在: " + requirementId));

        String quotationJson = requirement.getAiQuotationResult();
        if (quotationJson == null || quotationJson.isEmpty()) {
            throw new RuntimeException("暂无AI估价缓存数据");
        }

        try {
            return objectMapper.readValue(quotationJson, AiQuotationResponse.class);
        } catch (JsonProcessingException e) {
            log.error("解析缓存的AI估价结果失败, requirementId: {}", requirementId, e);
            throw new RuntimeException("解析AI估价数据失败");
        }
    }

    private void validateRequest(AiQuotationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }
    }

    private List<SelectedFunctionDto> getSelectedFunctions(List<Long> functionIds) {
        if (functionIds == null || functionIds.isEmpty()) {
            return Collections.emptyList();
        }

        return functionIds.stream()
                .map(this::getFunctionById)
                .filter(Objects::nonNull)
                .toList();
    }

    private SelectedFunctionDto getFunctionById(Long functionId) {
        return new SelectedFunctionDto(
                functionId,
                "功能" + functionId,
                "standard",
                40,
                new BigDecimal("1000")
        );
    }

    private BigDecimal getComplexityFactor(String complexity) {
        return switch (complexity != null ? complexity : "standard") {
            case "simple" -> new BigDecimal("0.8");
            case "complex" -> new BigDecimal("1.5");
            default -> new BigDecimal("1.0");
        };
    }

    private BigDecimal getVisualStyleFactor(String visualStyle) {
        return VISUAL_STYLE_FACTORS.getOrDefault(visualStyle, new BigDecimal("0.2"));
    }

    private double getUrgencyFactor(String urgency) {
        return switch (urgency != null ? urgency : "normal") {
            case "relaxed" -> 1.5;
            case "urgent" -> 0.7;
            case "critical" -> 0.5;
            default -> 1.0;
        };
    }
}
