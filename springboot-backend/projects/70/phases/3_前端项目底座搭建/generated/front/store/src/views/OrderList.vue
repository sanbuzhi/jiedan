<template>
  <div class="order-list">
    <van-nav-bar title="我的订单" left-arrow />
    <van-tabs v-model:active="activeTab" sticky>
      <van-tab title="全部" name=""></van-tab>
      <van-tab title="待付款" name="0"></van-tab>
      <van-tab title="待发货" name="1"></van-tab>
      <van-tab title="已发货" name="2"></van-tab>
      <van-tab title="已完成" name="3"></van-tab>
    </van-tabs>
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="onLoad">
        <div v-if="orderList.length === 0" class="empty-order">
          <van-empty description="暂无订单" />
        </div>
        <div v-else class="order-card-list">
          <van-cell-group inset v-for="order in orderList" :key="order.id">
            <van-cell>
              <template #title>
                <div class="order-header">
                  <span class="order-no">订单号：{{ order.orderNo }}</span>
                  <van-tag :type="statusTypeMap[order.status]">{{ statusTextMap[order.status] }}</van-tag>
                </div>
              </template>
            </van-cell>
            <van-cell v-for="item in order.items" :key="item.id" @click="goOrderDetail(order.id)">
              <template #icon>
                <img :src="item.product?.images?.[0] || 'https://fastly.jsdelivr.net/npm/@vant/assets/apple-1.jpeg'" alt="product" class="product-img" />
              </template>
              <template #title>
                <div class="product-name">{{ item.product?.name || '商品' }}</div>
              </template>
              <template #label>
                <div class="product-meta">
                  <span>¥{{ item.price?.toFixed(2) || '0.00' }}</span>
                  <span>x{{ item.quantity }}</span>
                </div>
              </template>
            </van-cell>
            <van-cell>
              <template #title>
                <div class="order-footer">
                  <span>共{{ order.totalQuantity }}件商品，实付：</span>
                  <span class="price">¥{{ order.totalAmount?.toFixed(2) || '0.00' }}</span>
                </div>
              </template>
              <template #right-icon>
                <div class="order-actions">
                  <van-button v-if="order.status === 0" size="small" type="primary" @click="handlePay(order.id)">付款</van-button>
                  <van-button v-if="order.status === 0" size="small" plain type="default" @click="handleCancel(order.id)">取消</van-button>
                  <van-button v-if="order.status === 2" size="small" type="success" @click="handleConfirm(order.id)">确认收货</van-button>
                  <van-button size="small" plain type="primary" @click="goOrderDetail(order.id)">查看详情</van-button>
                </div>
              </template>
            </van-cell>
          </van-cell-group>
        </div>
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { getOrderList, cancelOrder, payOrder, confirmOrder } from '@/api/order'

const router = useRouter()
const activeTab = ref('')
const orderList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)

const statusTypeMap = {
  0: 'info',
  1: 'warning',
  2: 'primary',
  3: 'success',
  4: 'danger'
}

const statusTextMap = {
  0: '待付款',
  1: '待发货',
  2: '已发货',
  3: '已完成',
  4: '已取消'
}

const queryForm = reactive({
  status: '',
  page: 1,
  pageSize: 10
})

async function fetchOrderList() {
  try {
    const res = await getOrderList(queryForm)
    if (queryForm.page === 1) {
      orderList.value = res.data.list || []
    } else {
      orderList.value.push(...(res.data.list || []))
    }
    finished.value = orderList.value.length >= (res.data.total || 0)
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function onLoad() {
  fetchOrderList()
  queryForm.page++
}

function onRefresh() {
  queryForm.page = 1
  finished.value = false
  fetchOrderList()
}

async function handleCancel(id) {
  try {
    await showConfirmDialog({
      title: '提示',
      message: '确定要取消这个订单吗？'
    })
    await cancelOrder(id)
    showToast('订单已取消')
    onRefresh()
  } catch {
  }
}

async function handlePay(id) {
  try {
    await payOrder(id, { payMethod: 1 })
    showToast('支付成功')
    onRefresh()
  } catch (error) {
    console.error(error)
  }
}

async function handleConfirm(id) {
  try {
    await showConfirmDialog({
      title: '提示',
      message: '确定已收到商品？'
    })
    await confirmOrder(id)
    showToast('确认收货成功')
    onRefresh()
  } catch {
  }
}

function goOrderDetail(id) {
  // router.push({ name: 'OrderDetail', params: { id } })
  showToast('详情页开发中')
}

watch(activeTab, (val) => {
  queryForm.status = val
  queryForm.page = 1
  finished.value = false
  orderList.value = []
  onLoad()
})

onMounted(() => {
  onLoad()
})
</script>

<style scoped>
.order-list {
  width: 100%;
  min-height: 100%;
  background-color: #f5f5f5;
}

.empty-order {
  padding: 60px 20px;
  text-align: center;
}

.order-card-list {
  padding: 0 16px;
}

.order-card-list .van-cell-group {
  margin-bottom: 12px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-no {
  font-size: 12px;
  color: #999;
}

.product-img {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  object-fit: cover;
}

.product-name {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;
  font-size: 14px;
  color: #999;
}

.product-meta span:first-child {
  color: #ff4444;
  font-weight: bold;
}

.order-footer {
  font-size: 14px;
  color: #333;
}

.order-footer .price {
  font-size: 18px;
  font-weight: bold;
  color: #ff4444;
}

.order-actions {
  display: flex;
  gap: 12px;
}
</style>