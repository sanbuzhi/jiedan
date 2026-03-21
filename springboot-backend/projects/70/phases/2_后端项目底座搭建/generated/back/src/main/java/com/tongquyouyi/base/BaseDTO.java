package com.tongquyouyi.base;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 基础DTO类
 */
@Data
public class BaseDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @ApiModelProperty(value = "主键ID")
    private Long id;
}