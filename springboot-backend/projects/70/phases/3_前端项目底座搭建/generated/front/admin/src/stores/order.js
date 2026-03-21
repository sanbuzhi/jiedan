import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getOrderList, getOrderDetail, updateOrderStatus, shipOrder } from '@/api/order'

export const useOrderStore = defineStore('order', () => {
  const list = ref([])
  const total = ref(0)
  const loading = ref(false)
  const currentOrder = ref(null)

  const fetchList = async (params = {}) => {
    loading.value = true
    try {
      const res = await getOrderList(params)
      list.value = res.data.list || res.data || []
      total.value = res.data.total || 0
    } catch (error) {
      console.error('获取订单列表失败', error)
    } finally {
      loading.value = false
    }
  }

  const fetchDetail = async (id) => {
    loading.value = true
    try {
      const res = await getOrderDetail(id)
      currentOrder.value = res.data
      return res.data
    } catch (error) {
      console.error('获取订单详情失败', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const changeStatus = async (id, status) => {
    const res = await updateOrderStatus(id, status)
    return res
  }

  const ship = async (id, data) => {
    const res = await shipOrder(id, data)
    return res
  }

  return {
    list,
    total,
    loading,
    currentOrder,
    fetchList,
    fetchDetail,
    changeStatus,
    ship
  }
})