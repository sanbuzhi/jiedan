package com.jiedan.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 测试用例DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCase {

    /**
     * 用例ID
     */
    private String id;

    /**
     * 用例名称
     */
    private String name;

    /**
     * 测试场景
     */
    private String scenario;

    /**
     * 输入数据
     */
    private String input;

    /**
     * 预期输出
     */
    private String expectedOutput;

    /**
     * 测试类型：正常/边界/异常
     */
    private String type;
}
