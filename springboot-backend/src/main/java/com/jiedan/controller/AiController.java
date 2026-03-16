package com.jiedan.controller;

import com.jiedan.dto.ApiResponse;
import com.jiedan.dto.ai.*;
import com.jiedan.security.CurrentUser;
import com.jiedan.service.ai.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AI服务Controller
 * 统一处理所有AI相关请求
 */
@RestController
@RequestMapping("/v1/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final AiService aiService;

    /**
     * AI明确需求
     * POST /v1/ai/clarify-requirement
     */
    @PostMapping("/clarify-requirement")
    public ResponseEntity<ApiResponse<ClarifyRequirementResponse>> clarifyRequirement(
            @CurrentUser Long userId,
            @Valid @RequestBody ClarifyRequirementRequest request) {
        log.info("收到AI明确需求请求, userId: {}", userId);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("用户未认证"));
        }

        try {
            ClarifyRequirementResponse response = aiService.clarifyRequirement(request);
            log.info("AI明确需求成功, userId: {}", userId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("AI明确需求失败, userId: {}, error: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("AI明确需求失败: " + e.getMessage()));
        }
    }

    /**
     * AI拆分任务
     * POST /v1/ai/split-tasks
     */
    @PostMapping("/split-tasks")
    public ResponseEntity<ApiResponse<SplitTasksResponse>> splitTasks(
            @CurrentUser Long userId,
            @Valid @RequestBody SplitTasksRequest request) {
        log.info("收到AI拆分任务请求, userId: {}", userId);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("用户未认证"));
        }

        try {
            SplitTasksResponse response = aiService.splitTasks(request);
            log.info("AI拆分任务成功, userId: {}", userId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("AI拆分任务失败, userId: {}, error: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("AI拆分任务失败: " + e.getMessage()));
        }
    }

    /**
     * AI生成代码
     * POST /v1/ai/generate-code
     */
    @PostMapping("/generate-code")
    public ResponseEntity<ApiResponse<GenerateCodeResponse>> generateCode(
            @CurrentUser Long userId,
            @Valid @RequestBody GenerateCodeRequest request) {
        log.info("收到AI生成代码请求, userId: {}", userId);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("用户未认证"));
        }

        try {
            GenerateCodeResponse response = aiService.generateCode(request);
            log.info("AI生成代码成功, userId: {}", userId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("AI生成代码失败, userId: {}, error: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("AI生成代码失败: " + e.getMessage()));
        }
    }

    /**
     * AI功能测试
     * POST /v1/ai/functional-test
     */
    @PostMapping("/functional-test")
    public ResponseEntity<ApiResponse<FunctionalTestResponse>> functionalTest(
            @CurrentUser Long userId,
            @Valid @RequestBody FunctionalTestRequest request) {
        log.info("收到AI功能测试请求, userId: {}", userId);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("用户未认证"));
        }

        try {
            FunctionalTestResponse response = aiService.functionalTest(request);
            log.info("AI功能测试成功, userId: {}", userId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("AI功能测试失败, userId: {}, error: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("AI功能测试失败: " + e.getMessage()));
        }
    }

    /**
     * AI安全测试
     * POST /v1/ai/security-test
     */
    @PostMapping("/security-test")
    public ResponseEntity<ApiResponse<SecurityTestResponse>> securityTest(
            @CurrentUser Long userId,
            @Valid @RequestBody SecurityTestRequest request) {
        log.info("收到AI安全测试请求, userId: {}", userId);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("用户未认证"));
        }

        try {
            SecurityTestResponse response = aiService.securityTest(request);
            log.info("AI安全测试成功, userId: {}", userId);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("AI安全测试失败, userId: {}, error: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("AI安全测试失败: " + e.getMessage()));
        }
    }
}
