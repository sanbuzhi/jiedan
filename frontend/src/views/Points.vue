<template>
  <div class="points-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>积分记录</span>
        </div>
      </template>
      <div class="points-content">
        <div class="total-points">
          <el-statistic title="当前总积分" :value="userStore.userInfo?.total_points || 0">
            <template #suffix>分</template>
          </el-statistic>
        </div>
        
        <el-divider />
        
        <el-table :data="pointRecords" stripe style="width: 100%" v-loading="loading">
          <el-table-column prop="type" label="类型" width="120">
            <template #default="{ row }">
              <el-tag :type="row.amount > 0 ? 'success' : 'danger'">
                {{ row.type === 'referral_bonus' ? '推荐奖励' : row.type }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="变化" width="120">
            <template #default="{ row }">
              <span :class="{ 'positive': row.amount > 0, 'negative': row.amount < 0 }">
                {{ row.amount > 0 ? '+' : '' }}{{ row.amount }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="balance" label="余额" width="120" />
          <el-table-column prop="description" label="描述" />
          <el-table-column prop="created_at" label="时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.created_at) }}
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination" v-if="pointRecords.length > 0">
          <el-button :disabled="skip === 0" @click="loadMore(-1)">上一页</el-button>
          <span>第 {{ Math.floor(skip / limit) + 1 }} 页</span>
          <el-button :disabled="pointRecords.length < limit" @click="loadMore(1)">下一页</el-button>
        </div>
        
        <el-empty v-if="pointRecords.length === 0 && !loading" description="暂无积分记录" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import api from '@/utils/api'

const userStore = useUserStore()
const pointRecords = ref([])
const loading = ref(false)
const skip = ref(0)
const limit = ref(20)

function formatDate(dateStr) {
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

async function loadPoints() {
  loading.value = true
  try {
    pointRecords.value = await api.getMyPoints(skip.value, limit.value)
  } catch (error) {
    console.error('获取积分记录失败:', error)
  } finally {
    loading.value = false
  }
}

function loadMore(direction) {
  skip.value += direction * limit.value
  if (skip.value < 0) skip.value = 0
  loadPoints()
}

onMounted(() => {
  loadPoints()
})
</script>

<style scoped>
.points-container {
  max-width: 900px;
}

.card-header {
  font-weight: 500;
  font-size: 16px;
}

.points-content {
  padding: 20px 0;
}

.total-points {
  text-align: center;
  margin-bottom: 20px;
}

.positive {
  color: #67c23a;
  font-weight: bold;
}

.negative {
  color: #f56c6c;
  font-weight: bold;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 20px;
}
</style>
