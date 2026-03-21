<template>
  <div class="order-detail">
    <van-nav-bar title="订单详情" left-arrow @click-left="$router.back()" />
    
    <div v-loading="loading" class="content">
      <template v-if="orderInfo.id">
        <!-- 订单状态 -->
        <div class="status-card">
          <van-icon :name="getStatusIcon(orderInfo.status)" size="48" :color="getStatusColor(orderInfo.status)" />
          <div class="status-text" :style="{ color: getStatusColor(orderInfo.status) }">
            {{ getStatusText(orderInfo.status) }}
          </div>
        </div>

        <!-- 收货地址 -->
        <div class="section">
          <van-cell-group inset>
            <van-cell title="收货人" :value="orderInfo.address?.name" />
            <van-cell title="联系电话" :value="orderInfo.address?.phone" />
            <van-cell title="收货地址" :value="orderInfo.address?.fullAddress" />
          </van-cell-group>
        </div>

        <!-- 商品列表 -->
        <div class="section">
          <van-cell-group inset>
            <van-cell title="商品信息" />
            <div v-for="item in orderInfo.items" :key="item.id" class="product-item">
              <img :src="item.productImage" alt="" class="product-img" />
              <div class="product-info">
                <div class="product-name">{{ item.productName }}</div>
                <div class="product-specs">{{ item.specs }}</div>
                <div class="product-bottom">
                  <span class="price">¥{{ item.price?.toFixed(2) }}</span>
                  <span class="quantity">x{{ item.quantity }}</span>
                </div>
              </div>
            </div>
            <van-cell title="订单金额" :value="`¥${orderInfo.totalAmount?.toFixed(2)}`" />
          </van-cell-group>
        </div>

        <!-- 订单信息 -->
        <div class="section">
          <van-cell-group inset>
            <van-cell title="订单编号" :value="orderInfo.orderNo" />
            <van-cell title="下单时间" :value="formatTime(orderInfo.createdAt)" />
          </van-cell-group>
        </div>

        <!-- 底部操作栏 -->
        <div class="bottom-actions">
          <template v-if="orderInfo.status === 0">
            <van-button type="danger" size="small" @click="handleCancel">取消订单</van-button>
            <van-button type="primary" size="small" @click="handlePay">立即支付</van-button>
          </template>
          <template v-else-if="orderInfo.status === 2">
            <van-button type="primary" size="small" @click="handleConfirm">确认收货</van-button>
          </template>
        </div>
      </template>
      <template v-else>
        <van-empty description="订单不存在" />
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { getOrderDetail, cancelOrder, confirmOrder, payOrder } from '@/api/order'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const orderInfo = ref({})

const getStatusIcon = (status) => {
  const map = {
    0: 'clock-o',
    1: 'logistics',
    2: 'description',
    3: 'checked',
    4: 'cross'
  }
  return map[status] || 'info-o'
}

const getStatusColor = (status) => {
  const map = {
    0: '#ff976a',
    1: '#1989fa',
    2: '#07c160',
    3: '#07c160',
    4: '#969799'
  }
  return map[status] || '#969799'
}

const getStatusText = (status) => {
  const map = {
    0: '待支付',
    1: '待发货',
    2: '待收货',
    3: '已完成',
    4: '已取消'
  }
  return map[status] || '未知'
}

const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'
}

const fetchOrderDetail = async () => {
  loading.value = true
  try {
    const res = await getOrderDetail(route.params.id)
    if (res.code === 200) {
      orderInfo.value = res.data
    } else {
      showToast(res.message || '获取订单详情失败')
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleCancel = async () => {
  try {
    await showConfirmDialog({
      title: '提示',
      message: '确认取消订单吗？'
    })
    const res = await cancelOrder(orderInfo.value.id)
    if (res.code === 200) {
      showToast('取消成功')
      fetchOrderDetail()
    } else {
      showToast(res.message || '取消失败')
    }
  } catch (error) {
    // 取消操作
  }
}

const handlePay = async () => {
  try {
    const res = await payOrder(orderInfo.value.id)
    if (res.code === 200) {
      showToast('支付成功')
      fetchOrderDetail()
    } else {
      showToast(res.message || '支付失败')
    }
  } catch (error) {
    console.error(error)
  }
}

const handleConfirm = async () => {
  try {
    await showConfirmDialog({
      title: '提示',
      message: '确认收货吗？'
    })
    const res = await confirmOrder(orderInfo.value.id)
    if (res.code === 200) {
      showToast('确认成功')
      fetchOrderDetail()
    } else {
      showToast(res.message || '确认失败')
    }
  } catch (error) {
    // 取消操作
  }
}

onMounted(() => {
  fetchOrderDetail()
})
</script>

<style scoped>
.order-detail {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-bottom: 80px;
}

.content {
  padding: 12px 0;
}

.status-card {
  background-color: #fff;
  padding: 30px 0;
  text-align: center;
  margin-bottom: 12px;
}

.status-text {
  margin-top: 12px;
  font-size: 16px;
  font-weight: 500;
}

.section {
  margin-bottom: 12px;
}

.product-item {
  display: flex;
  padding: 12px;
  gap: 12px;
}

.product-img {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  object-fit: cover;
  flex-shrink: 0;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.product-name {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-specs {
  font-size: 12px;
  color: #969799;
  margin-top: 4px;
}

.product-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  font-size: 16px;
  font-weight: bold;
  color: #f56c6c;
}

.quantity {
  font-size: 14px;
  color: #969799;
}

.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #fff;
  padding: 12px 16px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}
</style>