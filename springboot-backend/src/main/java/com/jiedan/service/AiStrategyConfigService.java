package com.jiedan.service;

import com.jiedan.dto.AiStrategyConfigDTO;
import com.jiedan.entity.AiStrategyConfig;
import com.jiedan.repository.AiStrategyConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * AI策略配置Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiStrategyConfigService {

    private final AiStrategyConfigRepository configRepository;

    /**
     * 获取所有配置
     *
     * @return 配置列表
     */
    public List<AiStrategyConfigDTO> getAllConfigs() {
        return configRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有启用的配置
     *
     * @return 配置列表
     */
    public List<AiStrategyConfigDTO> getAllEnabledConfigs() {
        return configRepository.findAllByEnabledTrueOrderBySortOrderAsc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取配置
     *
     * @param id ID
     * @return 配置对象
     */
    public Optional<AiStrategyConfigDTO> getConfigById(Long id) {
        return configRepository.findById(id)
                .map(this::convertToDTO);
    }

    /**
     * 根据接口代码获取配置
     *
     * @param apiCode 接口代码
     * @return 配置对象
     */
    public Optional<AiStrategyConfigDTO> getConfigByApiCode(String apiCode) {
        return configRepository.findByApiCode(apiCode)
                .map(this::convertToDTO);
    }

    /**
     * 创建配置
     *
     * @param dto 配置DTO
     * @return 创建后的配置
     */
    @Transactional
    public AiStrategyConfigDTO createConfig(AiStrategyConfigDTO dto) {
        if (configRepository.existsByApiCode(dto.getApiCode())) {
            throw new RuntimeException("接口代码已存在: " + dto.getApiCode());
        }

        AiStrategyConfig config = new AiStrategyConfig();
        BeanUtils.copyProperties(dto, config);
        config.setId(null); // 确保是新建

        AiStrategyConfig saved = configRepository.save(config);
        log.info("创建AI策略配置: {}", saved.getApiCode());
        return convertToDTO(saved);
    }

    /**
     * 更新配置
     *
     * @param id  ID
     * @param dto 配置DTO
     * @return 更新后的配置
     */
    @Transactional
    public AiStrategyConfigDTO updateConfig(Long id, AiStrategyConfigDTO dto) {
        AiStrategyConfig config = configRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("配置不存在: " + id));

        // 如果修改了apiCode，检查是否与其他配置冲突
        if (!config.getApiCode().equals(dto.getApiCode())
                && configRepository.existsByApiCode(dto.getApiCode())) {
            throw new RuntimeException("接口代码已存在: " + dto.getApiCode());
        }

        BeanUtils.copyProperties(dto, config, "id", "createdAt");
        AiStrategyConfig saved = configRepository.save(config);
        log.info("更新AI策略配置: {}", saved.getApiCode());
        return convertToDTO(saved);
    }

    /**
     * 删除配置
     *
     * @param id ID
     */
    @Transactional
    public void deleteConfig(Long id) {
        AiStrategyConfig config = configRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("配置不存在: " + id));
        configRepository.delete(config);
        log.info("删除AI策略配置: {}", config.getApiCode());
    }

    /**
     * 批量更新配置
     *
     * @param dtos 配置列表
     * @return 更新后的配置列表
     */
    @Transactional
    public List<AiStrategyConfigDTO> batchUpdateConfigs(List<AiStrategyConfigDTO> dtos) {
        return dtos.stream()
                .map(dto -> {
                    if (dto.getId() != null) {
                        return updateConfig(dto.getId(), dto);
                    } else if (configRepository.existsByApiCode(dto.getApiCode())) {
                        // 如果存在则更新
                        AiStrategyConfig existing = configRepository.findByApiCode(dto.getApiCode()).orElseThrow();
                        return updateConfig(existing.getId(), dto);
                    } else {
                        // 不存在则创建
                        return createConfig(dto);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 初始化默认配置
     */
    @Transactional
    public void initDefaultConfigs() {
        if (configRepository.count() > 0) {
            log.info("AI策略配置已存在，跳过初始化");
            return;
        }

        List<AiStrategyConfig> defaultConfigs = List.of(
                createDefaultConfig("clarify-requirement", "AI明确需求", "分析并完善用户需求描述，输出需求概述、功能模块清单、用户角色定义等", 1),
                createDefaultConfig("split-tasks", "AI拆分任务", "将需求拆分为可执行的具体任务，包含任务名称、描述、预估工时、优先级等", 2),
                createDefaultConfig("generate-code", "AI开发", "基于需求生成高质量的代码实现，包含完整的类定义和业务方法", 3),
                createDefaultConfig("functional-test", "AI功能测试", "为代码生成功能测试用例，包含正常场景、边界条件、异常场景测试", 4),
                createDefaultConfig("security-test", "AI安全测试", "扫描代码中的安全漏洞，输出漏洞类型、严重程度、修复建议等", 5)
        );

        configRepository.saveAll(defaultConfigs);
        log.info("初始化AI策略配置完成，共 {} 条", defaultConfigs.size());
    }

    /**
     * 创建默认配置
     */
    private AiStrategyConfig createDefaultConfig(String apiCode, String apiName, String description, int sortOrder) {
        AiStrategyConfig config = new AiStrategyConfig();
        config.setApiCode(apiCode);
        config.setApiName(apiName);
        config.setProvider("huoshan");
        config.setModel("doubao-seed-2.0-code");
        config.setTemperature(0.7);
        config.setMaxTokens(2000);
        config.setEnabled(true);
        config.setDescription(description);
        config.setIcon("Document");
        config.setSortOrder(sortOrder);
        return config;
    }

    /**
     * 转换为DTO
     */
    private AiStrategyConfigDTO convertToDTO(AiStrategyConfig config) {
        AiStrategyConfigDTO dto = new AiStrategyConfigDTO();
        BeanUtils.copyProperties(config, dto);
        return dto;
    }
}
