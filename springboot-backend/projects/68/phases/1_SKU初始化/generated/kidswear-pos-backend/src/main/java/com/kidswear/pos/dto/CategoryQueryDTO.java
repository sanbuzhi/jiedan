package com.kidswear.pos.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "分类查询DTO", description = "分类分页列表查询参数")
public class CategoryQueryDTO {

    @ApiModelProperty(value = "分类名称模糊搜索")
    private String name;

    @ApiModelProperty(value = "是否启用筛选：true-启用，false-禁用，null-全部")
    private Boolean enabled;

    @ApiModelProperty(value = "页码，从1开始", example = "1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页条数", example = "10")
    private Integer pageSize = 10;
}