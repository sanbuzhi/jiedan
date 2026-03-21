package com.kidswear.pos.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kidswear.pos.enums.GenderType;
import com.kidswear.pos.enums.SizeGroup;
import com.kidswear.pos.enums.SkuStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SKU视图对象
 */
@Data
@Schema(description = "SKU视图对象")
public class SkuVO {

    @Schema(description = "SKU ID")
    private Long id;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "SKU编码")
    private String skuCode;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "商品主图URL")
    private String mainImage;

    @Schema(description = "商品图片URL列表，逗号分隔")
    private String images;

    @Schema(description = "尺码组")
    private SizeGroup sizeGroup;

    @Schema(description = "尺码组名称")
    private String sizeGroupName;

    @Schema(description = "具体尺码")
    private String size;

    @Schema(description = "颜色")
    private String color;

    @Schema(description = "性别类型")
    private GenderType genderType;

    @Schema(description = "性别类型名称")
    private String genderTypeName;

    @Schema(description = "适用年龄段描述")
    private String ageRange;

    @Schema(description = "成本价")
    private BigDecimal costPrice;

    @Schema(description = "零售价")
    private BigDecimal retailPrice;

    @Schema(description = "库存数量")
    private Integer stock;

    @Schema(description = "预警库存")
    private Integer warningStock;

    @Schema(description = "SKU状态")
    private SkuStatus status;

    @Schema(description = "SKU状态名称")
    private String statusName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}