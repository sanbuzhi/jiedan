package com.jiedan.controller;

import com.jiedan.dto.AiStrategyConfigDTO;
import com.jiedan.dto.ApiResponse;
import com.jiedan.service.AiStrategyConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI策略配置Controller
 */
@RestController
@RequestMapping("/v1/ai/strategy")
@RequiredArgsConstructor
@Slf4j
public class AiStrategyConfigController {

    private final AiStrategyConfigService configService;

    /**
     * 获取所有配置
     */
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<List<AiStrategyConfigDTO>>> getAllConfigs() {
        log.info("获取所有AI策略配置");
        List<AiStrategyConfigDTO> configs = configService.getAllConfigs();
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    /**
     * 获取所有启用的配置
     */
    @GetMapping("/config/enabled")
    public ResponseEntity<ApiResponse<List<AiStrategyConfigDTO>>> getAllEnabledConfigs() {
        log.info("获取所有启用的AI策略配置");
        List<AiStrategyConfigDTO> configs = configService.getAllEnabledConfigs();
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    /**
     * 根据ID获取配置
     */
    @GetMapping("/config/{id}")
    public ResponseEntity<ApiResponse<AiStrategyConfigDTO>> getConfigById(@PathVariable Long id) {
        log.info("获取AI策略配置, id: {}", id);
        return configService.getConfigById(id)
                .map(config -> ResponseEntity.ok(ApiResponse.success(config)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("配置不存在")));
    }

    /**
     * 根据接口代码获取配置
     */
    @GetMapping("/config/code/{apiCode}")
    public ResponseEntity<ApiResponse<AiStrategyConfigDTO>> getConfigByApiCode(@PathVariable String apiCode) {
        log.info("获取AI策略配置, apiCode: {}", apiCode);
        return configService.getConfigByApiCode(apiCode)
                .map(config -> ResponseEntity.ok(ApiResponse.success(config)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("配置不存在")));
    }

    /**
     * 创建配置
     */
    @PostMapping("/config")
    public ResponseEntity<ApiResponse<AiStrategyConfigDTO>> createConfig(@RequestBody AiStrategyConfigDTO dto) {
        log.info("创建AI策略配置: {}", dto.getApiCode());
        try {
            AiStrategyConfigDTO created = configService.createConfig(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
        } catch (RuntimeException e) {
            log.error("创建配置失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 更新配置
     */
    @PutMapping("/config/{id}")
    public ResponseEntity<ApiResponse<AiStrategyConfigDTO>> updateConfig(
            @PathVariable Long id,
            @RequestBody AiStrategyConfigDTO dto) {
        log.info("更新AI策略配置, id: {}", id);
        try {
            AiStrategyConfigDTO updated = configService.updateConfig(id, dto);
            return ResponseEntity.ok(ApiResponse.success(updated));
        } catch (RuntimeException e) {
            log.error("更新配置失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 删除配置
     */
    @DeleteMapping("/config/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteConfig(@PathVariable Long id) {
        log.info("删除AI策略配置, id: {}", id);
        try {
            configService.deleteConfig(id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (RuntimeException e) {
            log.error("删除配置失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 批量更新配置
     */
    @PostMapping("/config/batch")
    public ResponseEntity<ApiResponse<List<AiStrategyConfigDTO>>> batchUpdateConfigs(
            @RequestBody List<AiStrategyConfigDTO> dtos) {
        log.info("批量更新AI策略配置, 数量: {}", dtos.size());
        try {
            List<AiStrategyConfigDTO> updated = configService.batchUpdateConfigs(dtos);
            return ResponseEntity.ok(ApiResponse.success(updated));
        } catch (RuntimeException e) {
            log.error("批量更新配置失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 初始化默认配置
     */
    @PostMapping("/config/init")
    public ResponseEntity<ApiResponse<Void>> initDefaultConfigs() {
        log.info("初始化默认AI策略配置");
        configService.initDefaultConfigs();
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 获取可用模型列表
     */
    @GetMapping("/models")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getAvailableModels() {
        log.info("获取可用AI模型列表");
        List<Map<String, String>> models = List.of(
                Map.of("value", "doubao-seed-2.0-code", "label", "豆包 Seed 2.0 Code"),
                Map.of("value", "doubao-seed-2.0-pro", "label", "豆包 Seed 2.0 Pro"),
                Map.of("value", "kimi-k2.5", "label", "Kimi K2.5"),
                Map.of("value", "deepseek-coder", "label", "DeepSeek Coder"),
                Map.of("value", "glm-4", "label", "GLM-4")
        );
        return ResponseEntity.ok(ApiResponse.success(models));
    }

    /**
     * 获取AI提供商列表
     */
    @GetMapping("/providers")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getProviders() {
        log.info("获取AI提供商列表");
        List<Map<String, String>> providers = List.of(
                Map.of("value", "huoshan", "label", "火山引擎"),
                Map.of("value", "openai", "label", "OpenAI"),
                Map.of("value", "wenxin", "label", "文心一言"),
                Map.of("value", "qwen", "label", "通义千问")
        );
        return ResponseEntity.ok(ApiResponse.success(providers));
    }
}
