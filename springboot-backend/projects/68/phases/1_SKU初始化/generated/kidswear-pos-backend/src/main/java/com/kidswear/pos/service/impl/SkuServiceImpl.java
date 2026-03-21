package com.kidswear.pos.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kidswear.pos.common.PageResult;
import com.kidswear.pos.common.Result;
import com.kidswear.pos.entity.Category;
import com.kidswear.pos.entity.Sku;
import com.kidswear.pos.mapper.CategoryMapper;
import com.kidswear.pos.mapper.SkuMapper;
import com.kidswear.pos.service.CategoryService;
import com.kidswear.pos.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class SkuServiceImpl extends ServiceImpl<SkuMapper, Sku> implements SkuService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public Result<PageResult<Sku>> getPage(Integer current, Integer size, String spuName, Long categoryId, Integer status) {
        Page<Sku> page = new Page<>(current, size);
        IPage<Sku> skuIPage = baseMapper.selectPageByCondition(page, spuName, categoryId, status);
        PageResult<Sku> pageResult = new PageResult<>(skuIPage.getTotal(), skuIPage.getRecords());
        return Result.success(pageResult);
    }

    @Override
    public Result<Sku> getBySkuCode(String skuCode) {
        LambdaQueryWrapper<Sku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Sku::getSkuCode, skuCode);
        Sku sku = this.getOne(wrapper);
        return Result.success(sku);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> generateSkuCode() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "SK" + dateStr;
        LambdaQueryWrapper<Sku> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(Sku::getSkuCode, prefix).orderByDesc(Sku::getSkuCode).last("LIMIT 1");
        Sku lastSku = this.getOne(wrapper);
        String newSkuCode;
        if (lastSku == null) {
            newSkuCode = prefix + "0001";
        } else {
            String suffix = lastSku.getSkuCode().substring(prefix.length());
            int newSuffix = Integer.parseInt(suffix) + 1;
            newSkuCode = prefix + String.format("%04d", newSuffix);
        }
        return Result.success(newSkuCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(Sku entity) {
        if (entity.getCategoryId() != null) {
            Category category = categoryMapper.selectById(entity.getCategoryId());
            if (category != null) {
                entity.setCategoryName(category.getName());
            }
        }
        return super.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(Sku entity) {
        if (entity.getCategoryId() != null) {
            Category category = categoryMapper.selectById(entity.getCategoryId());
            if (category != null) {
                entity.setCategoryName(category.getName());
            }
        }
        return super.updateById(entity);
    }
}