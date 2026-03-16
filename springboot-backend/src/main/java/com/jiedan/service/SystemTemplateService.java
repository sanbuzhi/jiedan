package com.jiedan.service;

import com.jiedan.dto.SystemFunctionDto;
import com.jiedan.dto.SystemRoleDto;
import com.jiedan.dto.SystemTemplateDetailResponse;
import com.jiedan.dto.SystemTemplateSearchResponse;
import com.jiedan.entity.SystemFunction;
import com.jiedan.entity.SystemRole;
import com.jiedan.entity.SystemTemplate;
import com.jiedan.repository.SystemFunctionRepository;
import com.jiedan.repository.SystemRoleRepository;
import com.jiedan.repository.SystemTemplateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SystemTemplateService {

    private final SystemTemplateRepository systemTemplateRepository;
    private final SystemRoleRepository systemRoleRepository;
    private final SystemFunctionRepository systemFunctionRepository;

    public SystemTemplateService(SystemTemplateRepository systemTemplateRepository,
                                 SystemRoleRepository systemRoleRepository,
                                 SystemFunctionRepository systemFunctionRepository) {
        this.systemTemplateRepository = systemTemplateRepository;
        this.systemRoleRepository = systemRoleRepository;
        this.systemFunctionRepository = systemFunctionRepository;
    }

    public List<SystemTemplateSearchResponse> searchByKeyword(String keyword) {
        log.info("Searching templates by keyword: {}", keyword);

        List<SystemTemplate> templates;

        if (!StringUtils.hasText(keyword)) {
            log.debug("Empty keyword provided, returning active templates");
            templates = systemTemplateRepository.findActiveTemplatesOrderByCreatedAtDesc();
        } else {
            String trimmedKeyword = keyword.trim();
            templates = systemTemplateRepository.searchByKeyword(trimmedKeyword);
        }

        if (templates == null || templates.isEmpty()) {
            log.debug("No templates found for keyword: {}", keyword);
            return Collections.emptyList();
        }

        return templates.stream()
                .map(this::convertToSearchResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<SystemTemplateDetailResponse> getTemplateDetail(Long templateId) {
        log.info("Getting template detail for id: {}", templateId);

        if (templateId == null) {
            log.warn("Template id is null");
            return Optional.empty();
        }

        Optional<SystemTemplate> templateOpt = systemTemplateRepository.findById(templateId);
        if (templateOpt.isEmpty()) {
            log.warn("Template not found with id: {}", templateId);
            return Optional.empty();
        }

        SystemTemplate template = templateOpt.get();
        List<SystemRole> roles = systemRoleRepository.findByTemplateIdWithFunctions(templateId);

        return Optional.of(convertToDetailResponse(template, roles));
    }

    private SystemTemplateSearchResponse convertToSearchResponse(SystemTemplate template) {
        SystemTemplateSearchResponse response = new SystemTemplateSearchResponse();
        response.setId(template.getId());
        response.setName(template.getName());
        response.setCode(template.getCode());
        response.setDescription(template.getDescription());
        response.setCategory(template.getCategory());
        response.setMatchScore(1.0);
        return response;
    }

    private SystemTemplateDetailResponse convertToDetailResponse(SystemTemplate template, List<SystemRole> roles) {
        SystemTemplateDetailResponse response = new SystemTemplateDetailResponse();
        response.setId(template.getId());
        response.setName(template.getName());
        response.setCode(template.getCode());
        response.setDescription(template.getDescription());
        response.setCategory(template.getCategory());

        if (roles != null) {
            List<SystemRoleDto> roleDtos = roles.stream()
                    .map(this::convertToRoleDto)
                    .collect(Collectors.toList());
            response.setRoles(roleDtos);
        } else {
            response.setRoles(Collections.emptyList());
        }

        return response;
    }

    private SystemRoleDto convertToRoleDto(SystemRole role) {
        SystemRoleDto dto = new SystemRoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setCode(role.getCode());
        dto.setDescription(role.getDescription());
        dto.setResponsibilities(role.getResponsibilities());
        dto.setSortOrder(role.getSortOrder());

        if (role.getFunctions() != null) {
            List<SystemFunctionDto> functionDtos = role.getFunctions().stream()
                    .map(this::convertToFunctionDto)
                    .collect(Collectors.toList());
            dto.setFunctions(functionDtos);
        } else {
            dto.setFunctions(Collections.emptyList());
        }

        return dto;
    }

    private SystemFunctionDto convertToFunctionDto(SystemFunction function) {
        SystemFunctionDto dto = new SystemFunctionDto();
        dto.setId(function.getId());
        dto.setName(function.getName());
        dto.setCode(function.getCode());
        dto.setDescription(function.getDescription());
        dto.setComplexity(function.getComplexity());
        dto.setEstimatedHours(function.getEstimatedHours());
        dto.setBasePrice(function.getBasePrice());
        dto.setPriority(function.getPriority());
        return dto;
    }
}
