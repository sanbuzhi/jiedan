package com.kidswear.pos.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuDTO {
    private Long id;
    private String name;
    private String code;
    private Long categoryId;
    private String categoryName;
    private BigDecimal price;
    private BigDecimal costPrice;
    private Integer stock;
    private String color;
    private String size;
    private String image;
    private String description;
    private Integer status;
}