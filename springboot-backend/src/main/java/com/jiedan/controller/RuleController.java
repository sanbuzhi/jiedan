package com.jiedan.controller;

import com.jiedan.entity.Rule;
import com.jiedan.service.RuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleService ruleService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getRules() {
        List<Rule> rules = ruleService.getRules();
        Map<String, Object> response = new HashMap<>();
        response.put("items", rules);
        response.put("total", rules.size());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/init")
    public ResponseEntity<Map<String, Object>> initializeRules() {
        ruleService.initializeRules();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "规则初始化成功");
        return ResponseEntity.ok(response);
    }
}
