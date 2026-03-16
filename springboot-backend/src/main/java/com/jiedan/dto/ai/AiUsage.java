package com.jiedan.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI Token使用量DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiUsage {

    /**
     * 提示token数
     */
    private Integer promptTokens;

    /**
     * 完成token数
     */
    private Integer completionTokens;

    /**
     * 总token数
     */
    private Integer totalTokens;
}
