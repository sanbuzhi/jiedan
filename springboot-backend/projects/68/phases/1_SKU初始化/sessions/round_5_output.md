===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/dto/CategoryDTO.java===
```java
package com.kidswear.pos.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@ApiModel(value = "分类DTO", description = "分类新增/编辑请求参数")
public class CategoryDTO {

    @ApiModelProperty(value = "分类ID（新增时为空，编辑时必填）")
    private Long id;

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50")
    @ApiModelProperty(value = "分类名称", required = true)
    private String name;

    @ApiModelProperty(value = "排序号，数字越小越靠前")
    private Integer sortOrder;

    @ApiModelProperty(value = "是否启用：true-启用，false-禁用")
    private Boolean enabled;
}
```

===FILE:kidswear-pos-backend/src/main/java/com/kidswear/pos/dto/CategoryQueryDTO.java===
```java
package com.kidswear.pos.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "分类查询DTO", description = "分类分页列表查询参数")
public class CategoryQueryDTO {

    @ApiModelProperty(value = "分类名称模糊搜索")
    private String name;

    @ApiModelProperty(value = "是否启用筛选：true-启用，false-禁用，null-全部")
    private Boolean enabled;

    @ApiModelProperty(value = "页码，从1开始", example = "1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页条数", example = "10")
    private Integer pageSize = 10;
}
```

===FILE:kidswear-pos-frontend/src/store/category.ts===
```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Category } from '@/types/category'
import { getCategoryList, getEnabledCategoryList } from '@/api/category'

export const useCategoryStore = defineStore('category', () => {
  // 全部分类列表（含禁用）
  const allCategories = ref<Category[]>([])
  // 启用的分类列表
  const enabledCategories = ref<Category[]>([])
  // 加载状态
  const loading = ref(false)

  // 获取全部分类
  const fetchAllCategories = async () => {
    loading.value = true
    try {
      const res = await getCategoryList({ pageNum: 1, pageSize: 9999 })
      allCategories.value = res.data.records || []
    } catch (error) {
      console.error('获取全部分类失败', error)
    } finally {
      loading.value = false
    }
  }

  // 获取启用的分类
  const fetchEnabledCategories = async () => {
    loading.value = true
    try {
      const res = await getEnabledCategoryList()
      enabledCategories.value = res.data || []
    } catch (error) {
      console.error('获取启用分类失败', error)
    } finally {
      loading.value = false
    }
  }

  // 重置分类数据
  const resetCategories = () => {
    allCategories.value = []
    enabledCategories.value = []
  }

  return {
    allCategories,
    enabledCategories,
    loading,
    fetchAllCategories,
    fetchEnabledCategories,
    resetCategories
  }
})
```

===FILE:kidswear-pos-frontend/src/components/SkuTable.vue===
```vue
<template>
  <div class="sku-table-wrapper">
    <el-table
      v-loading="loading"
      :data="tableData"
      border
      stripe
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="code" label="SKU编码" width="180" />
      <el-table-column prop="name" label="商品名称" min-width="200" />
      <el-table-column prop="categoryName" label="所属分类" width="150" />
      <el-table-column prop="size" label="尺码" width="120" />
      <el-table-column prop="color" label="颜色" width="120" />
      <el-table-column prop="purchasePrice" label="进价" width="120">
        <template #default="{ row }">
          ¥{{ row.purchasePrice?.toFixed(2) || '0.00' }}
        </template>
      </el-table-column>
      <el-table-column prop="retailPrice" label="零售价" width="120">
        <template #default="{ row }">
          ¥{{ row.retailPrice?.toFixed(2) || '0.00' }}
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="100">
        <template #default="{ row }">
          <el-tag :type="row.stock < 10 ? 'danger' : 'success'">
            {{ row.stock }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="enabled" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'info'">
            {{ row.enabled ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button type="warning" link @click="handleToggleStatus(row)">
            {{ row.enabled ? '禁用' : '启用' }}
          </el-button>
          <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { Sku, SkuQueryParams } from '@/types/sku'

// Props
interface Props {
  loading: boolean
  tableData: Sku[]
  pagination: {
    pageNum: number
    pageSize: number
    total: number
  }
}
const props = defineProps<Props>()

// Emits
interface Emits {
  (e: 'selection-change', selections: Sku[]): void
  (e: 'edit', row: Sku): void
  (e: 'toggle-status', row: Sku): void
  (e: 'delete', row: Sku): void
  (e: 'update:pagination', pagination: Partial<Props['pagination']>): void
}
const emit = defineEmits<Emits>()

// 本地选中状态
const localSelections = ref<Sku[]>([])

// 监听父组件tableData变化清空选中（可选优化）
watch(() => props.tableData, () => {
  localSelections.value = []
}, { deep: true })

// 表格操作
const handleSelectionChange = (selections: Sku[]) => {
  localSelections.value = selections
  emit('selection-change', selections)
}

const handleEdit = (row: Sku) => {
  emit('edit', row)
}

const handleToggleStatus = (row: Sku) => {
  emit('toggle-status', row)
}

const handleDelete = (row: Sku) => {
  emit('delete', row)
}

// 分页操作
const handleSizeChange = (pageSize: number) => {
  emit('update:pagination', { pageSize, pageNum: 1 })
}

const handleCurrentChange = (pageNum: number) => {
  emit('update:pagination', { pageNum })
}
</script>

<style scoped lang="scss">
.sku-table-wrapper {
  .pagination-wrapper {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
```