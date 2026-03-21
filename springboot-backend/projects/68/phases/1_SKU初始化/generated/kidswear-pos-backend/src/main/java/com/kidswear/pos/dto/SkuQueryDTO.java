package com.kidswear.pos.dto;

import lombok.Data;

@Data
public class SkuQueryDTO {
    private String keyword;
    private Long categoryId;
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}