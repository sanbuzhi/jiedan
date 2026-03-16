package com.jiedan.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1")
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Service is running");
        return response;
    }

    @GetMapping("/test-exchange")
    public Map<String, String> testExchange() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Exchange endpoint is accessible");
        return response;
    }
}
