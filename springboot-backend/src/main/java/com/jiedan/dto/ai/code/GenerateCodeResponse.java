package com.jiedan.dto.ai.code;

import com.jiedan.dto.ai.AiUsage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 代码生成响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateCodeResponse {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 生成的文件列表
     */
    private List<GeneratedFile> files;

    /**
     * README内容
     */
    private String readmeContent;

    // ========== 编译验证结果 ==========
    /**
     * 是否编译通过
     */
    private Boolean compilationPassed;

    /**
     * 编译错误列表
     */
    private List<CompilationError> compilationErrors;

    // ========== 代码摘要（用于后续任务） ==========
    /**
     * 代码摘要
     */
    private CodeSummary codeSummary;

    // ========== AI调用元数据 ==========
    /**
     * Token使用情况
     */
    private AiUsage usage;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTimeMs;
}
