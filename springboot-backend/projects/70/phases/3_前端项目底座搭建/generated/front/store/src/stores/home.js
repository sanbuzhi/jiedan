import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getHomeData } from '@/api/home'

export const useHomeStore = defineStore('home', () => {
  // 状态
  const banners = ref([])
  const hotCategories = ref([])
  const hotProducts = ref([])
  const newProducts = ref([])
  const loading = ref(false)
  const hasLoaded = ref(false) // 标记是否已加载过首页数据，避免重复请求

  // 加载首页数据
  const loadHomeData = async (force = false) => {
    // 如果不是强制刷新且已加载过，则直接返回
    if (!force && hasLoaded.value) return

    loading.value = true
    try {
      const res = await getHomeData()
      if (res.code === 200) {
        const data = res.data
        banners.value = data.banners || []
        hotCategories.value = data.hotCategories || []
        hotProducts.value = data.hotProducts || []
        newProducts.value = data.newProducts || []
        hasLoaded.value = true
      }
    } catch (error) {
      console.error('加载首页数据失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 重置状态（比如退出登录时）
  const resetHome = () => {
    banners.value = []
    hotCategories.value = []
    hotProducts.value = []
    newProducts.value = []
    hasLoaded.value = false
  }

  return {
    banners,
    hotCategories,
    hotProducts,
    newProducts,
    loading,
    hasLoaded,
    loadHomeData,
    resetHome
  }
})