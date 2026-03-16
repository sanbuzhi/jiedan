<template>
  <el-dialog
    v-model="dialogVisible"
    title="订单详情"
    width="700px"
    :close-on-click-modal="false"
  >
    <div v-if="order" class="order-detail">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单ID">{{ order.id }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{ order.order_no }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">
          <span class="amount">¥{{ order.amount?.toFixed(2) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusType(order.status)">
            {{ getStatusText(order.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">
          {{ getPaymentTypeText(order.payment_type) }}
        </el-descriptions-item>
        <el-descriptions-item label="支付时间">
          {{ formatDate(order.paid_at) || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDate(order.created_at) }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ formatDate(order.updated_at) }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <h4>用户信息</h4>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户ID">{{ order.user_id }}</el-descriptions-item>
        <el-descriptions-item label="用户手机号">{{ order.user_phone }}</el-descriptions-item>
        <el-descriptions-item label="用户昵称">{{ order.user_nickname || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <h4>关联需求信息</h4>
      <el-descriptions v-if="order.requirement" :column="2" border>
        <el-descriptions-item label="需求ID">{{ order.requirement.id }}</el-descriptions-item>
        <el-descriptions-item label="需求标题">{{ order.requirement.title }}</el-descriptions-item>
        <el-descriptions-item label="需求类型">
          {{ getRequirementTypeText(order.requirement.type) }}
        </el-descriptions-item>
        <el-descriptions-item label="需求状态">
          <el-tag :type="getRequirementStatusType(order.requirement.status)">
            {{ getRequirementStatusText(order.requirement.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="预算金额">
          ¥{{ order.requirement.budget?.toFixed(2) }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDate(order.requirement.created_at) }}
        </el-descriptions-item>
        <el-descriptions-item label="需求描述" :span="2">
          {{ order.requirement.description || '-' }}
        </el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="暂无关联需求信息" />

      <el-divider />

      <h4>支付信息</h4>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="支付流水号">
          {{ order.payment_no || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="第三方支付单号">
          {{ order.third_party_no || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="退款金额">
          {{ order.refund_amount ? `¥${order.refund_amount.toFixed(2)}` : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="退款时间">
          {{ formatDate(order.refunded_at) || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ order.remark || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button
          v-if="order?.status === 'paid'"
          type="success"
          @click="handleConfirmPayment"
        >
          确认收款
        </el-button>
        <el-button
          v-if="order?.status === 'paid' || order?.status === 'confirmed'"
          type="warning"
          @click="handleRefund"
        >
          退款
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessageBox } from 'element-plus'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  order: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:visible', 'confirm-payment', 'refund'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
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

const getRequirementTypeText = (type) => {
  const textMap = {
    design: '设计',
    development: '开发',
    marketing: '营销',
    content: '内容',
    other: '其他'
  }
  return textMap[type] || type
}

const getRequirementStatusType = (status) => {
  const typeMap = {
    draft: 'info',
    published: 'primary',
    in_progress: 'warning',
    completed: 'success',
    cancelled: 'danger'
  }
  return typeMap[status] || 'info'
}

const getRequirementStatusText = (status) => {
  const textMap = {
    draft: '草稿',
    published: '已发布',
    in_progress: '进行中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return textMap[status] || status
}

const formatDate = (dateStr) => {
  if (!dateStr) return null
  return new Date(dateStr).toLocaleString('zh-CN')
}

const handleConfirmPayment = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要确认订单 ${props.order.order_no} 的收款吗？`,
      '确认收款',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    emit('confirm-payment', props.order.id)
  } catch (error) {
    // 用户取消
  }
}

const handleRefund = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要对订单 ${props.order.order_no} 进行退款吗？`,
      '确认退款',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    emit('refund', props.order.id)
  } catch (error) {
    // 用户取消
  }
}
</script>

<style scoped>
.order-detail {
  max-height: 60vh;
  overflow-y: auto;
}

.order-detail h4 {
  margin: 16px 0 12px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 500;
}

.amount {
  color: #f56c6c;
  font-weight: 500;
  font-size: 16px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
