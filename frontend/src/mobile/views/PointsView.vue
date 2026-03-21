<template>
  <MobileLayout>
    <div class="points-view">
      <div class="points-header">
        <div class="points-balance">
          <span class="balance-label">我的积分</span>
          <span class="balance-value">{{ totalPoints }}</span>
        </div>
      </div>

      <div class="points-list">
        <div class="list-title">积分明细</div>

        <div v-if="loading" class="loading-state">
          <div class="loading-text">加载中...</div>
        </div>

        <div v-else-if="pointsList.length === 0" class="empty-state">
          <div class="empty-icon">📋</div>
          <div class="empty-text">暂无积分记录</div>
        </div>

        <div v-else class="records-container">
          <div
            v-for="record in pointsList"
            :key="record.id"
            class="record-item"
          >
            <div class="record-left">
              <div class="record-desc">{{ record.description }}</div>
              <div class="record-time">{{ formatTime(record.created_at) }}</div>
            </div>
            <div class="record-amount" :class="record.amount >= 0 ? 'positive' : 'negative'">
              {{ record.amount >= 0 ? '+' : '' }}{{ record.amount }}
            </div>
          </div>
        </div>

        <div v-if="hasMore && !loading" class="load-more" @click="loadMore">
          加载更多
        </div>
      </div>
    </div>
  </MobileLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import MobileLayout from '../components/MobileLayout.vue'
import api from '@/utils/api'

const totalPoints = ref(0)
const pointsList = ref([])
const loading = ref(false)
const skip = ref(0)
const limit = 20
const hasMore = ref(true)

const fetchPoints = async (append = false) => {
  if (loading.value) return
  loading.value = true

  try {
    const res = await api.getMyPoints(skip.value, limit)
    let items = res.items || res.content || res

    if (!Array.isArray(items)) {
      items = []
    }

    if (append) {
      pointsList.value = [...pointsList.value, ...items]
    } else {
      pointsList.value = items
    }

    totalPoints.value = res.total || res.totalPoints || calculateTotal(items)

    if (items.length < limit) {
      hasMore.value = false
    }
  } catch (err) {
    console.error('获取积分记录失败:', err)
    if (!append) {
      pointsList.value = []
    }
  } finally {
    loading.value = false
  }
}

const calculateTotal = (items) => {
  return items.reduce((sum, item) => sum + (item.amount || 0), 0)
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

const loadMore = () => {
  skip.value += limit
  fetchPoints(true)
}

onMounted(() => {
  fetchPoints()
})
</script>

<style scoped>
.points-view {
  min-height: 100vh;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  padding: 16px;
  padding-bottom: 80px;
}

.points-header {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.points-balance {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.balance-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.balance-value {
  font-size: 36px;
  font-weight: bold;
  color: #667eea;
}

.points-list {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.list-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.loading-state {
  padding: 40px 0;
  text-align: center;
}

.loading-text {
  color: #999;
  font-size: 14px;
}

.empty-state {
  padding: 40px 0;
  text-align: center;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-text {
  color: #999;
  font-size: 14px;
}

.records-container {
  max-height: 400px;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #f0f0f0;
}

.record-item:last-child {
  border-bottom: none;
}

.record-left {
  flex: 1;
}

.record-desc {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.record-time {
  font-size: 12px;
  color: #999;
}

.record-amount {
  font-size: 16px;
  font-weight: bold;
}

.record-amount.positive {
  color: #07c160;
}

.record-amount.negative {
  color: #ff4d4f;
}

.load-more {
  text-align: center;
  padding: 16px;
  color: #667eea;
  font-size: 14px;
  cursor: pointer;
  margin-top: 8px;
}

.load-more:active {
  opacity: 0.7;
}
</style>
