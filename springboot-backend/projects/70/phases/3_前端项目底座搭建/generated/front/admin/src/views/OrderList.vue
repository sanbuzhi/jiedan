<template>
  <div class="order-list">
    <PageHeader title="订单管理" />
    <el-card style="margin-top: 20px">
      <el-form :inline="true" :model="queryForm" style="margin-bottom: 20px">
        <el-form-item label="订单号">
          <el-input v-model="queryForm.orderNo" placeholder="请输入订单号" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
            <el-option label="待付款" :value="0" />
            <el-option label="待发货" :value="1" />
            <el-option label="已发货" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
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
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="username" label="下单用户" width="120" />
        <el-table-column prop="totalAmount" label="总金额" width="120">
          <template #default="{ row }">
            ¥{{ row.totalAmount.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTypeMap[row.status]">
              {{ statusTextMap[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small">查看</el-button>
            <el-button v-if="row.status === 1" type="success" link size="small">发货</el-button>
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
import { Search, RefreshLeft } from '@element-plus/icons-vue'
import { getOrderList } from '@/api/order'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const statusTypeMap = {
  0: 'info',
  1: 'warning',
  2: 'primary',
  3: 'success',
  4: 'danger'
}

const statusTextMap = {
  0: '待付款',
  1: '待发货',
  2: '已发货',
  3: '已完成',
  4: '已取消'
}

const queryForm = reactive({
  orderNo: '',
  status: '',
  page: 1,
  pageSize: 10
})

async function handleQuery() {
  loading.value = true
  try {
    const res = await getOrderList(queryForm)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

function handleReset() {
  queryForm.orderNo = ''
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