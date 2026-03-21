<template>
  <el-row :gutter="20">
    <el-col :xs="24" :sm="12" :md="6" v-for="stat in statList" :key="stat.title">
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value || 0 }}</div>
            <div class="stat-title">{{ stat.title }}</div>
          </div>
        </div>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
import { computed } from 'vue'
import {
  ShoppingCart,
  User,
  Goods,
  Money
} from '@element-plus/icons-vue'
import { useDashboardStore } from '@/stores/dashboard'

const dashboardStore = useDashboardStore()

const statList = computed(() => [
  {
    title: '今日订单',
    value: dashboardStore.stats?.todayOrders,
    icon: ShoppingCart,
    color: '#409EFF'
  },
  {
    title: '总用户数',
    value: dashboardStore.stats?.totalUsers,
    icon: User,
    color: '#67C23A'
  },
  {
    title: '商品总数',
    value: dashboardStore.stats?.totalProducts,
    icon: Goods,
    color: '#E6A23C'
  },
  {
    title: '今日销售额',
    value: dashboardStore.stats?.todayRevenue ? `¥${dashboardStore.stats.todayRevenue.toFixed(2)}` : '¥0.00',
    icon: Money,
    color: '#F56C6C'
  }
])
</script>

<style scoped>
.stat-card {
  margin-bottom: 20px;
}
.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.stat-info {
  flex: 1;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}
.stat-title {
  font-size: 14px;
  color: #909399;
}
</style>