```
===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/common/Result.java===
```
package com.kidswear.pos.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.message = "操作成功";
        result.data = data;
        return result;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.code = 500;
        result.message = message;
        return result;
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        return result;
    }
}
```

===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/common/PageResult.java===
```
package com.kidswear.pos.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private Long total;
    private List<T> records;
}
```

===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/entity/Category.java===
```
package com.kidswear.pos.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("category")
public class Category {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer sortOrder;
    private Integer status; // 0-禁用 1-启用
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/entity/Sku.java===
```
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
```

===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/mapper/CategoryMapper.java===
```
package com.kidswear.pos.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kidswear.pos.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
```

===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/mapper/SkuMapper.java===
```
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
```

===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/service/CategoryService.java===
```
package com.kidswear.pos.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kidswear.pos.common.Result;
import com.kidswear.pos.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {
    Result<List<Category>> getEnabledList();
}
```

===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/service/SkuService.java===
```
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
```

===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/service/impl/CategoryServiceImpl.java===
```
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
```

===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/service/impl/SkuServiceImpl.java===
```
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
```

===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/controller/CategoryController.java===
```
package com.kidswear.pos.controller;

import com.kidswear.pos.common.Result;
import com.kidswear.pos.entity.Category;
import com.kidswear.pos.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@CrossOrigin
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list/enabled")
    public Result<List<Category>> getEnabledList() {
        return categoryService.getEnabledList();
    }

    @PostMapping
    public Result<Void> add(@RequestBody Category category) {
        categoryService.save(category);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Category category) {
        categoryService.updateById(category);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.removeById(id);
        return Result.success();
    }
}
```

===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/controller/SkuController.java===
```
package com.kidswear.pos.controller;

import com.kidswear.pos.common.PageResult;
import com.kidswear.pos.common.Result;
import com.kidswear.pos.entity.Sku;
import com.kidswear.pos.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sku")
@CrossOrigin
public class SkuController {

    @Autowired
    private SkuService skuService;

    @GetMapping("/page")
    public Result<PageResult<Sku>> getPage(@RequestParam(defaultValue = "1") Integer current,
                                            @RequestParam(defaultValue = "10") Integer size,
                                            @RequestParam(required = false) String spuName,
                                            @RequestParam(required = false) Long categoryId,
                                            @RequestParam(required = false) Integer status) {
        return skuService.getPage(current, size, spuName, categoryId, status);
    }

    @GetMapping("/code")
    public Result<String> generateSkuCode() {
        return skuService.generateSkuCode();
    }

    @GetMapping("/{id}")
    public Result<Sku> getById(@PathVariable Long id) {
        return Result.success(skuService.getById(id));
    }

    @GetMapping("/code/{skuCode}")
    public Result<Sku> getBySkuCode(@PathVariable String skuCode) {
        return skuService.getBySkuCode(skuCode);
    }

    @PostMapping
    public Result<Void> add(@RequestBody Sku sku) {
        skuService.save(sku);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Sku sku) {
        skuService.updateById(sku);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        skuService.removeById(id);
        return Result.success();
    }
}
```

===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/config/MybatisPlusConfig.java===
```
package com.kidswear.pos.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/config/MyMetaObjectHandler.java===
```
package com.kidswear.pos.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
```

===FILE:kidswear-pos-backend/src/main/resources/mapper/SkuMapper.xml===
```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.kidswear.pos.mapper.SkuMapper">
    <select id="selectPageByCondition" resultType="com.kidswear.pos.entity.Sku">
        SELECT * FROM sku
        <where>
            <if test="spuName != null and spuName != ''">
                AND spu_name LIKE CONCAT('%', #{spuName}, '%')
            </if>
            <if test="categoryId != null">
                AND category_id = #{categoryId}
            </if>
            <if test="status != null">
                AND status = #{status}
            </if>
        </where>
        ORDER BY create_time DESC
    </select>
</mapper>
```

===FILE:kidswear-pos-frontend/src/api/sku.ts===
```
import request from '@/utils/request'

export interface Sku {
  id?: number
  skuCode: string
  spuName: string
  categoryId: number
  categoryName?: string
  size: string
  color: string
  image?: string
  costPrice: number
  retailPrice: number
  memberPrice: number
  stock: number
  safetyStock: number
  status: number
  createTime?: string
  updateTime?: string
}

export interface Category {
  id?: number
  name: string
  sortOrder: number
  status: number
}

export interface PageQuery {
  current: number
  size: number
  spuName?: string
  categoryId?: number
  status?: number
}

export interface PageResult<T> {
  total: number
  records: T[]
}

// 获取分类列表（启用）
export const getEnabledCategoryList = () => {
  return request.get<Category[]>('/api/category/list/enabled')
}

// 获取SKU分页
export const getSkuPage = (params: PageQuery) => {
  return request.get<PageResult<Sku>>('/api/sku/page', { params })
}

// 生成SKU编码
export const generateSkuCode = () => {
  return request.get<string>('/api/sku/code')
}

// 根据ID获取SKU
export const getSkuById = (id: number) => {
  return request.get<Sku>(`/api/sku/${id}`)
}

// 新增SKU
export const addSku = (data: Sku) => {
  return request.post('/api/sku', data)
}

// 修改SKU
export const updateSku = (data: Sku) => {
  return request.put('/api/sku', data)
}

// 删除SKU
export const deleteSku = (id: number) => {
  return request.delete(`/api/sku/${id}`)
}
```

===FILE:kidswear-pos-frontend/src/store/sku.ts===
```
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getEnabledCategoryList, getSkuPage, Sku, Category, PageQuery, PageResult } from '@/api/sku'

export const useSkuStore = defineStore('sku', () => {
  // 分类列表
  const categoryList = ref<Category[]>([])
  // SKU分页结果
  const skuPageResult = ref<PageResult<Sku>>({ total: 0, records: [] })
  // 加载状态
  const loading = ref(false)

  // 获取分类列表
  const fetchCategoryList = async () => {
    try {
      const res = await getEnabledCategoryList()
      categoryList.value = res.data || []
    } catch (error) {
      console.error('获取分类列表失败', error)
    }
  }

  // 获取SKU分页
  const fetchSkuPage = async (params: PageQuery) => {
    loading.value = true
    try {
      const res = await getSkuPage(params)
      skuPageResult.value = res.data || { total: 0, records: [] }
    } catch (error) {
      console.error('获取SKU分页失败', error)
    } finally {
      loading.value = false
    }
  }

  return {
    categoryList,
    skuPageResult,
    loading,
    fetchCategoryList,
    fetchSkuPage
  }
})
```

===FILE:kidswear-pos-frontend/src/views/sku-init/index.vue===
```
<template>
  <div class="sku-init-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>SKU初始化</span>
          <el-button type="primary" @click="handleAdd">新增SKU</el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="商品名称">
          <el-input v-model="queryParams.spuName" placeholder="请输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="queryParams.categoryId" placeholder="请选择分类" clearable>
            <el-option
              v-for="item in skuStore.categoryList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 表格区域 -->
      <el-table v-loading="skuStore.loading" :data="skuStore.skuPageResult.records" border stripe>
        <el-table-column prop="skuCode" label="SKU编码" width="160" />
        <el-table-column prop="spuName" label="商品名称" min-width="180" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="size" label="尺码" width="100" />
        <el-table-column prop="color" label="颜色" width="100" />
        <el-table-column prop="costPrice" label="成本价" width="100">
          <template #default="{ row }">
            ¥{{ row.costPrice.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="retailPrice" label="零售价" width="100">
          <template #default="{ row }">
            ¥{{ row.retailPrice.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="safetyStock" label="安全库存" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页区域 -->
      <el-pagination
        v-model:current-page="queryParams.current"
        v-model:page-size="queryParams.size"
        :total="skuStore.skuPageResult.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSearch"
        @current-change="handleSearch"
        class="pagination"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑SKU' : '新增SKU'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="skuFormRef" :model="skuForm" :rules="skuRules" label-width="100px">
        <el-form-item label="SKU编码" prop="skuCode">
          <el-input v-model="skuForm.skuCode" :disabled="true" />
        </el-form-item>
        <el-form-item label="商品名称" prop="spuName">
          <el-input v-model="skuForm.spuName" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="skuForm.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="item in skuStore.categoryList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="尺码" prop="size">
              <el-input v-model="skuForm.size" placeholder="请输入尺码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="颜色" prop="color">
              <el-input v-model="skuForm.color" placeholder="请输入颜色" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="图片" prop="image">
          <el-input v-model="skuForm.image" placeholder="请输入图片URL" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="成本价" prop="costPrice">
              <el-input-number v-model="skuForm.costPrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="零售价" prop="retailPrice">
              <el-input-number v-model="skuForm.retailPrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="会员价" prop="memberPrice">
              <el-input-number v-model="skuForm.memberPrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="skuForm.stock" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="安全库存" prop="safetyStock">
              <el-input-number v-model="skuForm.safetyStock" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="skuForm.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useSkuStore } from '@/store/sku'
import { generateSkuCode, addSku, updateSku, deleteSku, getSkuById, type Sku } from '@/api/sku'

const skuStore = useSkuStore()

// 搜索参数
const queryParams = reactive<PageQuery>({
  current: 1,
  size: 10
})

// 弹窗相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const skuFormRef = ref<FormInstance>()
const skuForm = reactive<Sku>({
  skuCode: '',
  spuName: '',
  categoryId: 0,
  size: '',
  color: '',
  image: '',
  costPrice: 0,
  retailPrice: 0,
  memberPrice: 0,
  stock: 0,
  safetyStock: 0,
  status: 1
})

// 表单校验
const skuRules = reactive<FormRules<Sku>>({
  spuName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  size: [{ required: true, message: '请输入尺码', trigger: 'blur' }],
  color: [{ required: true, message: '请输入颜色', trigger: 'blur' }],
  costPrice: [{ required: true, message: '请输入成本价', trigger: 'blur' }],
  retailPrice: [{ required: true, message: '请输入零售价', trigger: 'blur' }],
  memberPrice: [{ required: true, message: '请输入会员价', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
  safetyStock: [{ required: true, message: '请输入安全库存', trigger: 'blur' }]
})

// 初始化
onMounted(() => {
  skuStore.fetchCategoryList()
  handleSearch()
})

// 搜索
const handleSearch = () => {
  skuStore.fetchSkuPage(queryParams)
}

// 重置
const handleReset = () => {
  queryParams.current = 1
  queryParams.spuName = ''
  queryParams.categoryId = undefined
  queryParams.status = undefined
  handleSearch()
}

// 新增
const handleAdd = async () => {
  isEdit.value = false
  resetForm()
  // 生成SKU编码
  const res = await generateSkuCode()
  skuForm.skuCode = res.data || ''
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row: Sku) => {
  isEdit.value = true
  resetForm()
  const res = await getSkuById(row.id!)
  Object.assign(skuForm, res.data)
  dialogVisible.value = true
}

// 删除
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该SKU吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteSku(id)
    ElMessage.success('删除成功')
    handleSearch()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 提交
const handleSubmit = async () => {
  if (!skuFormRef.value) return
  await skuFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await updateSku(skuForm)
          ElMessage.success('修改成功')
        } else {
          await addSku(skuForm)
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        handleSearch()
      } catch (error) {
        ElMessage.error(isEdit.value ? '修改失败' : '新增失败')
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  if (skuFormRef.value) {
    skuFormRef.value.resetFields()
  }
  Object.assign(skuForm, {
    id: undefined,
    skuCode: '',
    spuName: '',
    categoryId: 0,
    categoryName: '',
    size: '',
    color: '',
    image: '',
    costPrice: 0,
    retailPrice: 0,
    memberPrice: 0,
    stock: 0,
    safetyStock: 0,
    status: 1,
    createTime: '',
    updateTime: ''
  })
}
</script>

<style scoped lang="scss">
.sku-init-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
```

===FILE:kidswear-pos-frontend/src/router/index.ts===
```
import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue')
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/sku-init',
    children: [
      {
        path: 'sku-init',
        name: 'SkuInit',
        component: () => import('@/views/sku-init/index.vue'),
        meta: { title: 'SKU初始化' }
      }
    ]
  },
  {
    path: '/pos',
    name: 'PosLayout',
    component: () => import('@/views/pos-layout/index.vue')
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = userStore.token
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
```

===FILE:kidswear-pos-frontend/src/main.ts===
```
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)
const pinia = createPinia()

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus)

app.mount('#app')
```

===FILE:kidswear-pos-frontend/package.json===
```
{
  "name": "kidswear-pos-frontend",
  "private": true,
  "version": "0.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vue-tsc && vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.4.21",
    "vue-router": "^4.3.0",
    "pinia": "^2.1.7",
    "element-plus": "^2.6.3",
    "@element-plus/icons-vue": "^2.3.1",
    "axios": "^1.6.8"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.4",
    "typescript": "^5.2.2",
    "vite": "^5.2.0",
    "vue-tsc": "^2.0.6",
    "sass": "^1.72.0"
  }
}
```