<template>
  <div class="order-detail">
    <PageHeader title="订单详情" :show-back="true" />
    <el-card v-loading="loading" class="order-card">
      <template v-if="orderInfo.id">
        <!-- 订单基本信息 -->
        <div class="order-section">
          <h4>订单信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单编号">{{ orderInfo.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="getStatusType(orderInfo.status)">{{ getStatusText(orderInfo.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="下单时间">{{ formatTime(orderInfo.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="支付时间">{{ formatTime(orderInfo.paidAt) }}</el-descriptions-item>
            <el-descriptions-item label="发货时间">{{ formatTime(orderInfo.shippedAt) }}</el-descriptions-item>
            <el-descriptions-item label="完成时间">{{ formatTime(orderInfo.completedAt) }}</el-descriptions-item>
            <el-descriptions-item label="订单金额" :span="2">
              <span class="price">¥{{ orderInfo.totalAmount?.toFixed(2) }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 收货地址 -->
        <div class="order-section">
          <h4>收货地址</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="收货人">{{ orderInfo.address?.name }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ orderInfo.address?.phone }}</el-descriptions-item>
            <el-descriptions-item label="收货地址">{{ orderInfo.address?.fullAddress }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 商品列表 -->
        <div class="order-section">
          <h4>商品信息</h4>
          <el-table :data="orderInfo.items" border>
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="productImage" label="商品图片" width="100">
              <template #default="{ row }">
                <el-image
                  :src="row.productImage"
                  fit="cover"
                  style="width: 60px; height: 60px"
                  :preview-src-list="[row.productImage]"
                />
              </template>
            </el-table-column>
            <el-table-column prop="specs" label="商品规格" width="150" />
            <el-table-column prop="price" label="单价" width="120">
              <template #default="{ row }">
                ¥{{ row.price?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="100" />
            <el-table-column prop="subtotal" label="小计" width="120">
              <template #default="{ row }">
                ¥{{ row.subtotal?.toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 操作按钮 -->
        <div class="order-actions">
          <el-button @click="$router.back()">返回</el-button>
          <template v-if="orderInfo.status === 1">
            <el-button type="primary" @click="handleShip">发货</el-button>
          </template>
        </div>
      </template>
      <template v-else>
        <el-empty description="订单不存在" />
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { getOrderDetail, updateOrderStatus } from '@/api/order'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const orderInfo = ref({})

const getStatusType = (status) => {
  const map = {
    0: 'info',    // 待支付
    1: 'warning', // 待发货
    2: 'primary', // 待收货
    3: 'success', // 已完成
    4: 'danger'   // 已取消
  }
  return map[status] || 'info'
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
      ElMessage.error(res.message || '获取订单详情失败')
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleShip = async () => {
  try {
    await ElMessageBox.confirm('确认发货吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await updateOrderStatus(orderInfo.value.id, 2)
    if (res.code === 200) {
      ElMessage.success('发货成功')
      fetchOrderDetail()
    } else {
      ElMessage.error(res.message || '发货失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

onMounted(() => {
  fetchOrderDetail()
})
</script>

<style scoped>
.order-detail {
  padding: 20px;
}

.order-card {
  margin-top: 20px;
}

.order-section {
  margin-bottom: 24px;
}

.order-section h4 {
  margin-bottom: 12px;
  color: #333;
}

.price {
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
}

.order-actions {
  margin-top: 24px;
  text-align: right;
}
</style>