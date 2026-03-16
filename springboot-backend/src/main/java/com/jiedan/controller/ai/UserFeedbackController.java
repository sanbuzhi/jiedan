package com.jiedan.controller.ai;

import com.jiedan.dto.ai.feedback.UserFeedbackRequest;
import com.jiedan.dto.ai.feedback.UserFeedbackResponse;
import com.jiedan.entity.UserFeedback;
import com.jiedan.service.ai.feedback.UserFeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户反馈控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/feedback")
@RequiredArgsConstructor
public class UserFeedbackController {

    private final UserFeedbackService userFeedbackService;

    /**
     * 提交用户反馈
     */
    @PostMapping("/submit")
    public ResponseEntity<UserFeedbackResponse> submitFeedback(@RequestBody UserFeedbackRequest request) {
        log.info("提交用户反馈, projectId: {}", request.getProjectId());

        UserFeedbackResponse response = userFeedbackService.submitFeedback(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取项目的所有反馈
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<UserFeedback>> getProjectFeedbacks(@PathVariable String projectId) {
        log.info("获取项目反馈, projectId: {}", projectId);

        List<UserFeedback> feedbacks = userFeedbackService.getProjectFeedbacks(projectId);
        return ResponseEntity.ok(feedbacks);
    }

    /**
     * 获取反馈详情
     */
    @GetMapping("/{feedbackId}")
    public ResponseEntity<UserFeedback> getFeedback(@PathVariable String feedbackId) {
        log.info("获取反馈详情, feedbackId: {}", feedbackId);

        UserFeedback feedback = userFeedbackService.getFeedback(feedbackId);

        if (feedback != null) {
            return ResponseEntity.ok(feedback);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 重新处理反馈（手动触发）
     */
    @PostMapping("/{feedbackId}/reprocess")
    public ResponseEntity<UserFeedbackResponse> reprocessFeedback(@PathVariable String feedbackId) {
        log.info("重新处理反馈, feedbackId: {}", feedbackId);

        UserFeedback feedback = userFeedbackService.getFeedback(feedbackId);

        if (feedback == null) {
            return ResponseEntity.notFound().build();
        }

        // 重置状态并重新处理
        feedback.setStatus("PENDING");
        feedback.setRepairAttempts(0);
        userFeedbackService.processFeedbackAsync(feedbackId);

        return ResponseEntity.ok(UserFeedbackResponse.success(feedbackId, "PROCESSING"));
    }
}
