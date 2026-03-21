import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Category } from '@/types/category'
import { getCategoryList, getEnabledCategoryList } from '@/api/category'

export const useCategoryStore = defineStore('category', () => {
  // 全部分类列表（含禁用）
  const allCategories = ref<Category[]>([])
  // 启用的分类列表
  const enabledCategories = ref<Category[]>([])
  // 加载状态
  const loading = ref(false)

  // 获取全部分类
  const fetchAllCategories = async () => {
    loading.value = true
    try {
      const res = await getCategoryList({ pageNum: 1, pageSize: 9999 })
      allCategories.value = res.data.records || []
    } catch (error) {
      console.error('获取全部分类失败', error)
    } finally {
      loading.value = false
    }
  }

  // 获取启用的分类
  const fetchEnabledCategories = async () => {
    loading.value = true
    try {
      const res = await getEnabledCategoryList()
      enabledCategories.value = res.data || []
    } catch (error) {
      console.error('获取启用分类失败', error)
    } finally {
      loading.value = false
    }
  }

  // 重置分类数据
  const resetCategories = () => {
    allCategories.value = []
    enabledCategories.value = []
  }

  return {
    allCategories,
    enabledCategories,
    loading,
    fetchAllCategories,
    fetchEnabledCategories,
    resetCategories
  }
})