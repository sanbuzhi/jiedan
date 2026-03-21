package com.tongquyouyi.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页响应类")
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总条数
     */
    @Schema(description = "总条数")
    private Long total;

    /**
     * 数据列表
     */
    @Schema(description = "数据列表")
    private List<T> list;

}