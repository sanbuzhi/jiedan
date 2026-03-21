===FILE:front/admin/src/api/dashboard.js===
```javascript
import request from '@/utils/request'

// 获取仪表盘统计数据
export function getDashboardStats() {
  return request({
    url: '/admin/dashboard/stats',
    method: 'get'
  })
}

// 获取最近订单列表
export function getRecentOrders(params) {
  return request({
    url: '/admin/dashboard/recent-orders',
    method: 'get',
    params
  })
}
```

===FILE:front/admin/src/stores/dashboard.js===
```javascript
import { defineStore } from 'pinia'
import { getDashboardStats, getRecentOrders } from '@/api/dashboard'

export const useDashboardStore = defineStore('dashboard', {
  state: () => ({
    stats: null,
    recentOrders: [],
    loading: false
  }),
  actions: {
    async fetchStats() {
      this.loading = true
      try {
        const res = await getDashboardStats()
        this.stats = res.data
      } finally {
        this.loading = false
      }
    },
    async fetchRecentOrders(params = { limit: 10 }) {
      try {
        const res = await getRecentOrders(params)
        this.recentOrders = res.data
      } catch (error) {
        console.error('获取最近订单失败', error)
      }
    }
  }
})
```

===FILE:front/admin/src/components/DashboardStats.vue===
```vue
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
```

===FILE:front/store/src/api/home.js===
```javascript
import request from '@/utils/request'

// 获取首页轮播图
export function getHomeBanners() {
  return request({
    url: '/store/home/banners',
    method: 'get'
  })
}

// 获取首页热门商品
export function getHotProducts(params) {
  return request({
    url: '/store/home/hot-products',
    method: 'get',
    params
  })
}

// 获取首页新品推荐
export function getNewProducts(params) {
  return request({
    url: '/store/home/new-products',
    method: 'get',
    params
  })
}
```

===FILE:front/store/src/components/HomeSwiper.vue===
```vue
<template>
  <van-swipe :autoplay="3000" indicator-color="#ff6034">
    <van-swipe-item v-for="banner in banners" :key="banner.id">
      <img :src="banner.imageUrl || placeholderImg" alt="banner" class="banner-img" />
    </van-swipe-item>
  </van-swipe>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getHomeBanners } from '@/api/home'
import placeholderImg from '@/assets/images/placeholder.jpg'

const banners = ref([])

onMounted(async () => {
  try {
    const res = await getHomeBanners()
    banners.value = res.data || []
  } catch (error) {
    console.error('获取轮播图失败', error)
  }
})
</script>

<style scoped>
.banner-img {
  width: 100%;
  height: 180px;
  object-fit: cover;
}
</style>
```

===FILE:front/store/src/components/CartItemQuantitySelector.vue===
```vue
<template>
  <div class="quantity-selector">
    <van-button
      size="small"
      icon="minus"
      :disabled="quantity <= min"
      @click="handleChange(quantity - 1)"
    />
    <input
      type="number"
      :min="min"
      :max="max"
      :value="quantity"
      class="quantity-input"
      @input="handleInputChange"
    />
    <van-button
      size="small"
      icon="plus"
      :disabled="quantity >= max"
      @click="handleChange(quantity + 1)"
    />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Number,
    default: 1
  },
  min: {
    type: Number,
    default: 1
  },
  max: {
    type: Number,
    default: 999
  }
})

const emit = defineEmits(['update:modelValue'])

const quantity = ref(props.modelValue)

watch(() => props.modelValue, (val) => {
  quantity.value = val
})

const handleChange = (val) => {
  let newVal = val
  if (newVal < props.min) newVal = props.min
  if (newVal > props.max) newVal = props.max
  quantity.value = newVal
  emit('update:modelValue', newVal)
}

const handleInputChange = (e) => {
  let newVal = parseInt(e.target.value) || props.min
  if (newVal < props.min) newVal = props.min
  if (newVal > props.max) newVal = props.max
  quantity.value = newVal
  emit('update:modelValue', newVal)
}
</script>

<style scoped>
.quantity-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}
.quantity-input {
  width: 50px;
  height: 32px;
  text-align: center;
  border: 1px solid #ebedf0;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
}
</style>
```

===FILE:front/admin/src/assets/images/placeholder.jpg===
```svg
<svg xmlns="http://www.w3.org/2000/svg" width="400" height="400" viewBox="0 0 400 400">
  <rect fill="#f0f0f0" width="400" height="400"/>
  <text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" fill="#999" font-size="24">暂无图片</text>
</svg>
```

===FILE:front/store/src/assets/images/placeholder.jpg===
```svg
<svg xmlns="http://www.w3.org/2000/svg" width="400" height="400" viewBox="0 0 400 400">
  <rect fill="#f7f8fa" width="400" height="400"/>
  <text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" fill="#969799" font-size="28">暂无图片</text>
</svg>
```