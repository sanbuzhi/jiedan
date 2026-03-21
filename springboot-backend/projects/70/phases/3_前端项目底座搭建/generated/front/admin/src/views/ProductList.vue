<template>
  <div class="product-list">
    <PageHeader title="商品管理">
      <template #extra>
        <el-button type="primary">
          <el-icon><Plus /></el-icon>
          新增商品
        </el-button>
      </template>
    </PageHeader>
    <el-card style="margin-top: 20px">
      <el-form :inline="true" :model="queryForm" style="margin-bottom: 20px">
        <el-form-item label="商品名称">
          <el-input v-model="queryForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="商品名称" />
        <el-table-column prop="price" label="价格" width="120">
          <template #default="{ row }">
            ¥{{ row.price.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small">编辑</el-button>
            <el-button type="warning" link size="small">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button type="danger" link size="small">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        @change="handleQuery"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import Pagination from '@/components/Pagination.vue'
import { Plus, Search, RefreshLeft } from '@element-plus/icons-vue'
import { getProductList } from '@/api/product'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryForm = reactive({
  name: '',
  status: '',
  page: 1,
  pageSize: 10
})

async function handleQuery() {
  loading.value = true
  try {
    const res = await getProductList(queryForm)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

function handleReset() {
  queryForm.name = ''
  queryForm.status = ''
  queryForm.page = 1
  handleQuery()
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped>
</style>