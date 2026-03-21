package com.kidswear.pos.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类视图对象
 */
@Data
@Schema(description = "分类视图对象")
public class CategoryVO {

    @Schema(description = "分类ID")
    private Long id;

    @Schema(description = "父分类ID，顶级分类为0")
    private Long parentId;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "分类编码")
    private String code;

    @Schema(description = "分类图标")
    private String icon;

    @Schema(description = "排序值")
    private Integer sort;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "是否叶子节点")
    private Boolean isLeaf;

    @Schema(description = "关联SKU数量")
    private Integer skuCount;

    @Schema(description = "子分类列表")
    private List<CategoryVO> children;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}