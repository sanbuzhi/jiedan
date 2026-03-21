package com.kidswear.pos.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kidswear.pos.entity.Sku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SkuMapper extends BaseMapper<Sku> {
    IPage<Sku> selectPageByCondition(Page<Sku> page, @Param("spuName") String spuName, @Param("categoryId") Long categoryId, @Param("status") Integer status);
}