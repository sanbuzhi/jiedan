package com.kidswear.pos.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kidswear.pos.common.PageResult;
import com.kidswear.pos.common.Result;
import com.kidswear.pos.entity.Sku;

public interface SkuService extends IService<Sku> {
    Result<PageResult<Sku>> getPage(Integer current, Integer size, String spuName, Long categoryId, Integer status);
    Result<Sku> getBySkuCode(String skuCode);
    Result<String> generateSkuCode();
}