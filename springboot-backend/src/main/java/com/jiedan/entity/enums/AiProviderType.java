package com.jiedan.entity.enums;

import lombok.Getter;

/**
 * AI提供商类型枚举
 */
@Getter
public enum AiProviderType {

    HUOSHAN("huoshan", "火山引擎", "doubao-seed-2.0-code"),
    OPENAI("openai", "OpenAI", "gpt-4"),
    WENXIN("wenxin", "文心一言", "ernie-bot"),
    QWEN("qwen", "通义千问", "qwen-max");

    private final String code;
    private final String name;
    private final String defaultModel;

    AiProviderType(String code, String name, String defaultModel) {
        this.code = code;
        this.name = name;
        this.defaultModel = defaultModel;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 代码
     * @return 枚举值，找不到返回null
     */
    public static AiProviderType fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (AiProviderType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断是否支持指定的模型
     *
     * @param model 模型名称
     * @return 是否支持
     */
    public boolean supportsModel(String model) {
        if (model == null || model.isEmpty()) {
            return false;
        }
        String normalizedModel = model.toLowerCase();
        return switch (this) {
            case HUOSHAN -> normalizedModel.contains("doubao") || normalizedModel.contains("kimi") ||
                    normalizedModel.contains("minimax") || normalizedModel.contains("glm") ||
                    normalizedModel.contains("deepseek");
            case OPENAI -> normalizedModel.contains("gpt") || normalizedModel.contains("text-");
            case WENXIN -> normalizedModel.contains("ernie");
            case QWEN -> normalizedModel.contains("qwen");
        };
    }
}
