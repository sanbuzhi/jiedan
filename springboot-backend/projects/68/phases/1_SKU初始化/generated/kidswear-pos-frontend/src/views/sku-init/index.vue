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