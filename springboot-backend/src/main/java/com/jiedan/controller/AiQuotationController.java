package com.jiedan.controller;

import com.jiedan.dto.AiQuotationRequest;
import com.jiedan.dto.AiQuotationResponse;
import com.jiedan.dto.ApiResponse;
import com.jiedan.entity.Requirement;
import com.jiedan.repository.RequirementRepository;
import com.jiedan.security.CurrentUser;
import com.jiedan.service.AiQuotationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/requirements")
@RequiredArgsConstructor
@Slf4j
public class AiQuotationController {

    private final AiQuotationService aiQuotationService;
    private final RequirementRepository requirementRepository;

    @PostMapping("/{id}/ai-quotation")
    public ResponseEntity<ApiResponse<AiQuotationResponse>> generateAiQuotation(
            @CurrentUser Long userId,
            @PathVariable("id") Long requirementId,
            @Valid @RequestBody AiQuotationRequest request) {
        log.info("收到AI估价请求, requirementId: {}, userId: {}", requirementId, userId);

        Requirement requirement = requirementRepository.findById(requirementId)
                .orElse(null);

        if (requirement == null) {
            log.warn("需求不存在, requirementId: {}", requirementId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("需求不存在"));
        }

        if (userId == null) {
            log.warn("无法获取当前用户ID, requirementId: {}", requirementId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("用户未认证"));
        }

        if (!requirement.getUserId().equals(userId)) {
            log.warn("用户无权访问该需求, requirementId: {}, userId: {}", requirementId, userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("无权访问该需求"));
        }

        try {
            AiQuotationResponse response = aiQuotationService.generateQuotation(requirementId, request);
            log.info("AI估价生成成功, requirementId: {}", requirementId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            log.warn("AI估价参数错误, requirementId: {}, error: {}", requirementId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("AI估价生成失败, requirementId: {}, error: {}", requirementId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("估价生成失败，请稍后重试"));
        }
    }

    @GetMapping("/{id}/ai-quotation")
    public ResponseEntity<ApiResponse<AiQuotationResponse>> getCachedAiQuotation(
            @CurrentUser Long userId,
            @PathVariable("id") Long requirementId) {
        log.info("获取缓存的AI估价, requirementId: {}, userId: {}", requirementId, userId);

        Requirement requirement = requirementRepository.findById(requirementId)
                .orElse(null);

        if (requirement == null) {
            log.warn("需求不存在, requirementId: {}", requirementId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("需求不存在"));
        }

        if (!requirement.getUserId().equals(userId)) {
            log.warn("用户无权访问该需求, requirementId: {}, userId: {}", requirementId, userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("无权访问该需求"));
        }

        try {
            AiQuotationResponse response = aiQuotationService.getCachedQuotation(requirementId);
            log.info("获取缓存AI估价成功, requirementId: {}", requirementId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("暂无AI估价缓存数据")) {
                log.warn("该需求暂无AI估价缓存数据, requirementId: {}", requirementId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("暂无估价数据"));
            }
            log.error("获取缓存AI估价失败, requirementId: {}, error: {}", requirementId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取估价数据失败"));
        }
    }
}
