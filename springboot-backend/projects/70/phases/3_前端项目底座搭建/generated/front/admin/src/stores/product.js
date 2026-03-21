import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getProductList, getProduct, createProduct, updateProduct, deleteProduct, updateProductStatus } from '@/api/product'

export const useProductStore = defineStore('product', () => {
  const list = ref([])
  const total = ref(0)
  const loading = ref(false)
  const currentProduct = ref(null)

  const fetchList = async (params = {}) => {
    loading.value = true
    try {
      const res = await getProductList(params)
      list.value = res.data.list || res.data || []
      total.value = res.data.total || 0
    } catch (error) {
      console.error('获取产品列表失败', error)
    } finally {
      loading.value = false
    }
  }

  const fetchDetail = async (id) => {
    loading.value = true
    try {
      const res = await getProduct(id)
      currentProduct.value = res.data
      return res.data
    } catch (error) {
      console.error('获取产品详情失败', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const addProduct = async (data) => {
    const res = await createProduct(data)
    return res
  }

  const editProduct = async (id, data) => {
    const res = await updateProduct(id, data)
    return res
  }

  const removeProduct = async (id) => {
    const res = await deleteProduct(id)
    return res
  }

  const changeStatus = async (id, status) => {
    const res = await updateProductStatus(id, status)
    return res
  }

  return {
    list,
    total,
    loading,
    currentProduct,
    fetchList,
    fetchDetail,
    addProduct,
    editProduct,
    removeProduct,
    changeStatus
  }
})