package com.jiedan.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/v1/analytics")
public class ABTestController {

    @GetMapping("/experiments")
    public ResponseEntity<Map<String, Object>> getExperiments(
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "10") int limit) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("items", List.of(
            Map.of(
                "id", 1,
                "name", "首页按钮颜色测试",
                "status", "running",
                "start_date", LocalDateTime.now().minusDays(7),
                "end_date", LocalDateTime.now().plusDays(7),
                "control_variant", "蓝色",
                "treatment_variant", "红色"
            ),
            Map.of(
                "id", 2,
                "name", "注册流程优化",
                "status", "completed",
                "start_date", LocalDateTime.now().minusDays(30),
                "end_date", LocalDateTime.now().minusDays(1),
                "control_variant", "原流程",
                "treatment_variant", "简化流程"
            )
        ));
        response.put("total", 2);
        response.put("skip", skip);
        response.put("limit", limit);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/experiments")
    public ResponseEntity<Map<String, Object>> createExperiment(@RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", new Random().nextInt(1000));
        response.put("success", true);
        response.put("message", "实验创建成功");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/experiments/{id}")
    public ResponseEntity<Map<String, Object>> updateExperiment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "实验更新成功");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/experiments/{id}/results")
    public ResponseEntity<Map<String, Object>> getExperimentResults(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        response.put("experiment_id", id);
        response.put("control_conversion_rate", 2.5);
        response.put("treatment_conversion_rate", 3.2);
        response.put("improvement", 28.0);
        response.put("confidence", 95.0);
        response.put("sample_size_control", 1000);
        response.put("sample_size_treatment", 1000);
        return ResponseEntity.ok(response);
    }
}
