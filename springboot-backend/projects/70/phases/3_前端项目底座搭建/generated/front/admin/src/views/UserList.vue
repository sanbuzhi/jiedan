<template>
  <div class="user-list">
    <PageHeader title="用户管理" />
    <el-card style="margin-top: 20px">
      <el-form :inline="true" :model="queryForm" style="margin-bottom: 20px">
        <el-form-item label="用户名">
          <el-input v-model="queryForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
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
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button :type="row.status === 1 ? 'warning' : 'success'" link size="small">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
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
import { getUserList } from '@/api/user'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryForm = reactive({
  username: '',
  status: '',
  page: 1,
  pageSize: 10
})

async function handleQuery() {
  loading.value = true
  try {
    const res = await getUserList(queryForm)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

function handleReset() {
  queryForm.username = ''
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