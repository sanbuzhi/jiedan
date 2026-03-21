package com.kidswear.pos.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@ApiModel(value = "分类DTO", description = "分类新增/编辑请求参数")
public class CategoryDTO {

    @ApiModelProperty(value = "分类ID（新增时为空，编辑时必填）")
    private Long id;

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50")
    @ApiModelProperty(value = "分类名称", required = true)
    private String name;

    @ApiModelProperty(value = "排序号，数字越小越靠前")
    private Integer sortOrder;

    @ApiModelProperty(value = "是否启用：true-启用，false-禁用")
    private Boolean enabled;
}