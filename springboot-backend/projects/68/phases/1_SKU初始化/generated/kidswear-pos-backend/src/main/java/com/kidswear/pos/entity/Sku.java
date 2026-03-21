package com.kidswear.pos.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sku")
public class Sku {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String skuCode;
    private String spuName;
    private Long categoryId;
    private String categoryName;
    private String size;
    private String color;
    private String image;
    private BigDecimal costPrice;
    private BigDecimal retailPrice;
    private BigDecimal memberPrice;
    private Integer stock;
    private Integer safetyStock;
    private Integer status; // 0-下架 1-上架
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}