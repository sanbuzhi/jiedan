package com.beauty.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 美妆小店统一分页响应结果
 *
 * @param <T> 分页数据类型
 * @author beauty-shop
 * @since 2024-06-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BeautyPageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 分页数据列表
     */
    private List<T> records;

    /**
     * 当前页码
     */
    private Long current;

    /**
     * 每页条数
     */
    private Long size;

}