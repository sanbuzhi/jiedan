package com.kidswear.pos.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kidswear.pos.common.Result;
import com.kidswear.pos.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {
    Result<List<Category>> getEnabledList();
}