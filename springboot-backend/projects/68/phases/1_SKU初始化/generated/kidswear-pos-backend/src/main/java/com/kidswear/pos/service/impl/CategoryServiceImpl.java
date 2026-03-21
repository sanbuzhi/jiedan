package com.kidswear.pos.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kidswear.pos.common.Result;
import com.kidswear.pos.entity.Category;
import com.kidswear.pos.mapper.CategoryMapper;
import com.kidswear.pos.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Override
    public Result<List<Category>> getEnabledList() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, 1).orderByAsc(Category::getSortOrder);
        List<Category> list = this.list(wrapper);
        return Result.success(list);
    }
}