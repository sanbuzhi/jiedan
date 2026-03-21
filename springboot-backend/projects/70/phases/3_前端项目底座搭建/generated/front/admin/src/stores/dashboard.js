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