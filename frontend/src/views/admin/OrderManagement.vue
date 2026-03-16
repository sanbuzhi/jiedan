<template>
  <div class="order-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleRefresh">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-form :model="filterForm" inline class="filter-form">
        <el-form-item label="订单状态">
          <el-select v-model="filterForm.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="待支付" value="pending" />
            <el-option label="已支付" value="paid" />
            <el-option label="已确认" value="confirmed" />
            <el-option label="已退款" value="refunded" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付类型">
          <el-select v-model="filterForm.payment_type" placeholder="全部类型" clearable style="width: 120px">
            <el-option label="支付宝" value="alipay" />
            <el-option label="微信支付" value="wechat" />
            <el-option label="银行转账" value="bank" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额范围">
          <el-input-number v-model="filterForm.min_amount" :min="0" :precision="2" placeholder="最小金额" style="width: 120px" />
          <span class="range-separator">-</span>
          <el-input-number v-model="filterForm.max_amount" :min="0" :precision="2" placeholder="最大金额" style="width: 120px" />
        </el-form-item>
        <el-form-item label="订单号">
          <el-input v-model="filterForm.order_no" placeholder="输入订单号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="orders" stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="order_no" label="订单号" width="180" />
        <el-table-column prop="user_phone" label="用户手机号" width="130" />
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">
            <span class="amount">¥{{ row.amount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payment_type" label="支付方式" width="100">
          <template #default="{ row }">
            {{ getPaymentTypeText(row.payment_type) }}
          </template>
        </el-table-column>
        <el-table-column prop="created_at" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.created_at) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleViewDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 'paid'"
              size="small"
              type="success"
              @click="handleConfirmPayment(row)"
            >
              确认收款
            </el-button>
            <el-button
              v-if="row.status === 'paid' || row.status === 'confirmed'"
              size="small"
              type="warning"
              @click="handleRefund(row)"
            >
              退款
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: flex-end;"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <OrderDetailDialog
      v-model:visible="detailDialogVisible"
      :order="currentOrder"
      @confirm-payment="handleConfirmPaymentFromDialog"
      @refund="handleRefundFromDialog"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/utils/api'
import OrderDetailDialog from '@/components/OrderDetailDialog.vue'

const orders = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const detailDialogVisible = ref(false)
const currentOrder = ref(null)

const filterForm = reactive({
  status: '',
  payment_type: '',
  min_amount: null,
  max_amount: null,
  order_no: ''
})

const getStatusType = (status) => {
  const typeMap = {
    pending: 'info',
    paid: 'warning',
    confirmed: 'success',
    refunded: 'danger',
    cancelled: 'info'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    pending: '待支付',
    paid: '已支付',
    confirmed: '已确认',
    refunded: '已退款',
    cancelled: '已取消'
  }
  return textMap[status] || status
}

const getPaymentTypeText = (type) => {
  const textMap = {
    alipay: '支付宝',
    wechat: '微信支付',
    bank: '银行转账'
  }
  return textMap[type] || type
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const fetchOrders = async () => {
  loading.value = true
  try {
    const params = {
      skip: (currentPage.value - 1) * pageSize.value,
      limit: pageSize.value,
      ...filterForm
    }
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })
    const response = await api.getOrders(params)
    orders.value = response.items || []
    total.value = response.total || 0
  } catch (error) {
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchOrders()
}

const handleReset = () => {
  filterForm.status = ''
  filterForm.payment_type = ''
  filterForm.min_amount = null
  filterForm.max_amount = null
  filterForm.order_no = ''
  currentPage.value = 1
  fetchOrders()
}

const handleRefresh = () => {
  fetchOrders()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  fetchOrders()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchOrders()
}

const handleViewDetail = async (row) => {
  try {
    const orderDetail = await api.getOrderDetail(row.id)
    currentOrder.value = orderDetail
    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取订单详情失败')
  }
}

const handleConfirmPayment = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要确认订单 ${row.order_no} 的收款吗？`,
      '确认收款',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await api.confirmPayment(row.id)
    ElMessage.success('收款确认成功')
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleRefund = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要对订单 ${row.order_no} 进行退款吗？`,
      '确认退款',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await api.refundOrder(row.id)
    ElMessage.success('退款成功')
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleConfirmPaymentFromDialog = async (orderId) => {
  try {
    await api.confirmPayment(orderId)
    ElMessage.success('收款确认成功')
    detailDialogVisible.value = false
    fetchOrders()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleRefundFromDialog = async (orderId) => {
  try {
    await api.refundOrder(orderId)
    ElMessage.success('退款成功')
    detailDialogVisible.value = false
    fetchOrders()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.order-management {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.filter-form {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.range-separator {
  margin: 0 10px;
  color: #909399;
}

.amount {
  color: #f56c6c;
  font-weight: 500;
}
</style>
