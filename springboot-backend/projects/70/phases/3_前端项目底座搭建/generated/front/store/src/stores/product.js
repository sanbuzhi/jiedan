import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getProductList, getProductDetail, getHotProducts, getNewProducts } from '@/api/product'

export const useProductStore = defineStore('product', () => {
  const list = ref([])
  const total = ref(0)
  const loading = ref(false)
  const currentProduct = ref(null)
  const hotProducts = ref([])
  const newProducts = ref([])
  const categories = ref([])

  const fetchList = async (params = {}) => {
    loading.value = true
    try {
      const res = await getProductList(params)
      list.value = res.data.list || res.data || []
      total.value = res.data.total || 0
    } catch (error) {
      console.error('获取商品列表失败', error)
    } finally {
      loading.value = false
    }
  }

  const fetchDetail = async (id) => {
    loading.value = true
    try {
      const res = await getProductDetail(id)
      currentProduct.value = res.data
      return res.data
    } catch (error) {
      console.error('获取商品详情失败', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const fetchHotProducts = async () => {
    try {
      const res = await getHotProducts()
      hotProducts.value = res.data || []
    } catch (error) {
      console.error('获取热门商品失败', error)
    }
  }

  const fetchNewProducts = async () => {
    try {
      const res = await getNewProducts()
      newProducts.value = res.data || []
    } catch (error) {
      console.error('获取新品失败', error)
    }
  }

  return {
    list,
    total,
    loading,
    currentProduct,
    hotProducts,
    newProducts,
    categories,
    fetchList,
    fetchDetail,
    fetchHotProducts,
    fetchNewProducts
  }
})