package com.tongquyouyi.base;

import com.tongquyouyi.constant.SystemConstant;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询基础DTO
 */
@Data
public class PageQueryDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 页码
     */
    @ApiModelProperty(value = "页码，默认1")
    private Integer pageNum = SystemConstant.DEFAULT_PAGE_NUM;
    
    /**
     * 每页条数
     */
    @ApiModelProperty(value = "每页条数，默认10")
    private Integer pageSize = SystemConstant.DEFAULT_PAGE_SIZE;
    
    /**
     * 排序字段
     */
    @ApiModelProperty(value = "排序字段")
    private String sortField;
    
    /**
     * 排序方式：asc-升序，desc-降序
     */
    @ApiModelProperty(value = "排序方式：asc-升序，desc-降序")
    private String sortOrder;
}