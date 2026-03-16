package com.jiedan.controller;

import com.jiedan.dto.SystemTemplateDetailResponse;
import com.jiedan.dto.SystemTemplateSearchResponse;
import com.jiedan.service.SystemTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/v1/system-templates")
@Slf4j
public class SystemTemplateController {

    private final SystemTemplateService systemTemplateService;

    public SystemTemplateController(SystemTemplateService systemTemplateService) {
        this.systemTemplateService = systemTemplateService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<SystemTemplateSearchResponse>> searchTemplates(
            @RequestParam(name = "keyword") String keyword) {
        log.info("Search templates request received with keyword: {}", keyword);

        if (!StringUtils.hasText(keyword)) {
            log.warn("Search keyword is empty or null");
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        try {
            List<SystemTemplateSearchResponse> results = systemTemplateService.searchByKeyword(keyword);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error searching templates with keyword: {}", keyword, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SystemTemplateDetailResponse> getTemplateDetail(@PathVariable Long id) {
        log.info("Get template detail request received for id: {}", id);

        if (id == null || id <= 0) {
            log.warn("Invalid template id: {}", id);
            return ResponseEntity.badRequest().build();
        }

        try {
            return systemTemplateService.getTemplateDetail(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error getting template detail for id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
