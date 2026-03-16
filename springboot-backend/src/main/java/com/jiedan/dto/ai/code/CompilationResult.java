package com.jiedan.dto.ai.code;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 编译结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompilationResult {

    /**
     * 是否编译成功
     */
    private boolean success;

    /**
     * 编译错误列表
     */
    private List<CompilationError> errors;

    /**
     * 编译输出信息
     */
    private String output;

    /**
     * 创建成功的编译结果
     */
    public static CompilationResult success() {
        return CompilationResult.builder()
                .success(true)
                .errors(List.of())
                .build();
    }

    /**
     * 创建失败的编译结果
     */
    public static CompilationResult failure(List<CompilationError> errors) {
        return CompilationResult.builder()
                .success(false)
                .errors(errors)
                .build();
    }
}
