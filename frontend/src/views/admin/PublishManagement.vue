<template>
  <div class="publish-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>发布管理</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            创建发布任务
          </el-button>
        </div>
      </template>

      <el-table :data="tasks" stripe style="width: 100%">
        <el-table-column prop="title" label="标题" width="200" />
        <el-table-column prop="platforms" label="发布平台" width="200">
          <template #default="{ row }">
            <el-tag v-for="p in row.platforms" :key="p" size="small" style="margin-right: 5px;">
              {{ p }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="scheduled_time" label="计划时间" width="180">
          <template #default="{ row }">
            {{ row.scheduled_time ? formatDate(row.scheduled_time) : '立即发布' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="created_at" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.created_at) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button
              v-if="row.status === 'pending' || row.status === 'failed'"
              size="small"
              type="primary"
              @click="handleRetry(row)"
            >
              重试
            </el-button>
            <el-button
              v-if="row.status === 'pending' || row.status === 'scheduled'"
              size="small"
              type="danger"
              @click="handleCancel(row)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const tasks = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const getStatusType = (status) => {
  const typeMap = {
    'pending': 'info',
    'scheduled': 'warning',
    'running': 'primary',
    'success': 'success',
    'failed': 'danger',
    'cancelled': 'info'
  }
  return typeMap[status] || ''
}

const getStatusText = (status) => {
  const textMap = {
    'pending': '待处理',
    'scheduled': '已排期',
    'running': '运行中',
    'success': '成功',
    'failed': '失败',
    'cancelled': '已取消'
  }
  return textMap[status] || status
}

const formatDate = (dateStr) => {
  return new Date(dateStr).toLocaleString('zh-CN')
}

const fetchTasks = async () => {
  tasks.value = [
    { id: 1, title: '推广文案发布', platforms: ['xiaohongshu', 'wechat'], scheduled_time: '2024-01-20 10:00:00', status: 'scheduled', created_at: '2024-01-15 10:30:00' },
    { id: 2, title: '产品介绍', platforms: ['zhihu', 'bilibili'], scheduled_time: null, status: 'success', created_at: '2024-01-14 15:20:00' },
    { id: 3, title: '成功案例分享', platforms: ['douyin'], scheduled_time: '2024-01-18 20:00:00', status: 'failed', created_at: '2024-01-13 09:45:00' }
  ]
  total.value = 30
}

const handleView = (row) => {
  ElMessage.info('查看详情功能开发中')
}

const handleCreate = () => {
  ElMessage.info('创建任务功能开发中')
}

const handleRetry = (row) => {
  ElMessage.success('重试任务已创建')
}

const handleCancel = (row) => {
  ElMessage.success('任务已取消')
}

onMounted(() => {
  fetchTasks()
})
</script>

<style scoped>
.publish-management {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}
</style>
