import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getEnabledCategoryList, getSkuPage, Sku, Category, PageQuery, PageResult } from '@/api/sku'

export const useSkuStore = defineStore('sku', () => {
  // 分类列表
  const categoryList = ref<Category[]>([])
  // SKU分页结果
  const skuPageResult = ref<PageResult<Sku>>({ total: 0, records: [] })
  // 加载状态
  const loading = ref(false)

  // 获取分类列表
  const fetchCategoryList = async () => {
    try {
      const res = await getEnabledCategoryList()
      categoryList.value = res.data || []
    } catch (error) {
      console.error('获取分类列表失败', error)
    }
  }

  // 获取SKU分页
  const fetchSkuPage = async (params: PageQuery) => {
    loading.value = true
    try {
      const res = await getSkuPage(params)
      skuPageResult.value = res.data || { total: 0, records: [] }
    } catch (error) {
      console.error('获取SKU分页失败', error)
    } finally {
      loading.value = false
    }
  }

  return {
    categoryList,
    skuPageResult,
    loading,
    fetchCategoryList,
    fetchSkuPage
  }
})