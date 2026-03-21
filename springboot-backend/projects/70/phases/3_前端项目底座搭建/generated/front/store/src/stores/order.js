import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getOrderList, getOrderDetail, createOrder, cancelOrder, confirmReceiveOrder, payOrder } from '@/api/order'

export const useOrderStore = defineStore('order', () => {
  // 订单列表
  const orderList = ref([])
  // 订单总数
  const total = ref(0)
  // 订单详情
  const orderDetail = ref(null)
  // 加载状态
  const loading = ref(false)

  // 获取订单列表
  const fetchOrderList = async (params) => {
    loading.value = true
    try {
      const res = await getOrderList(params)
      orderList.value = res.data.list || []
      total.value = res.data.total || 0
    } catch (error) {
      console.error('获取订单列表失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 获取订单详情
  const fetchOrderDetail = async (id) => {
    loading.value = true
    try {
      const res = await getOrderDetail(id)
      orderDetail.value = res.data
    } catch (error) {
      console.error('获取订单详情失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 创建订单
  const addOrder = async (data) => {
    try {
      const res = await createOrder(data)
      return res.data
    } catch (error) {
      console.error('创建订单失败:', error)
      throw error
    }
  }

  // 取消订单
  const cancelOrderItem = async (id) => {
    try {
      await cancelOrder(id)
      // 刷新列表或详情
      if (orderDetail.value && orderDetail.value.id === id) {
        await fetchOrderDetail(id)
      }
    } catch (error) {
      console.error('取消订单失败:', error)
      throw error
    }
  }

  // 确认收货
  const confirmReceive = async (id) => {
    try {
      await confirmReceiveOrder(id)
      // 刷新列表或详情
      if (orderDetail.value && orderDetail.value.id === id) {
        await fetchOrderDetail(id)
      }
    } catch (error) {
      console.error('确认收货失败:', error)
      throw error
    }
  }

  // 支付订单
  const payOrderItem = async (id) => {
    try {
      await payOrder(id)
      // 刷新列表或详情
      if (orderDetail.value && orderDetail.value.id === id) {
        await fetchOrderDetail(id)
      }
    } catch (error) {
      console.error('支付订单失败:', error)
      throw error
    }
  }

  // 重置订单详情
  const resetOrderDetail = () => {
    orderDetail.value = null
  }

  return {
    orderList,
    total,
    orderDetail,
    loading,
    fetchOrderList,
    fetchOrderDetail,
    addOrder,
    cancelOrderItem,
    confirmReceive,
    payOrderItem,
    resetOrderDetail
  }
})