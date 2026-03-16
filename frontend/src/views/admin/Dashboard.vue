<template>
  <div class="dashboard">
    <!-- 第一行：流量统计 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409EFF;">
              <el-icon :size="30"><View /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboardStats.total_views || 0 }}</div>
              <div class="stat-label">总浏览量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67C23A;">
              <el-icon :size="30"><Pointer /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboardStats.total_clicks || 0 }}</div>
              <div class="stat-label">总点击量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #E6A23C;">
              <el-icon :size="30"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboardStats.total_registrations || 0 }}</div>
              <div class="stat-label">总注册数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #F56C6C;">
              <el-icon :size="30"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboardStats.total_conversions || 0 }}</div>
              <div class="stat-label">总转化数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第二行：业务统计 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #9B59B6;">
              <el-icon :size="30"><ShoppingCart /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboardStats.total_orders || 0 }}</div>
              <div class="stat-label">总成交量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #1ABC9C;">
              <el-icon :size="30"><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatAmount(dashboardStats.total_revenue) }}</div>
              <div class="stat-label">总收入 (元)</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第三行：图表 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>转化率趋势</span>
            </div>
          </template>
          <v-chart :option="conversionChartOption" style="height: 300px;" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>平台分布</span>
            </div>
          </template>
          <v-chart :option="platformChartOption" style="height: 300px;" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 第四行：最近需求和订单 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近需求</span>
              <el-button type="primary" link @click="$router.push('/admin/requirements')">
                查看更多
              </el-button>
            </div>
          </template>
          <el-table :data="dashboardStats.recent_requirements || []" stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="user_type" label="用户类型" width="100">
              <template #default="{ row }">
                <el-tag :type="getUserTypeTag(row.user_type)" size="small">
                  {{ formatUserType(row.user_type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="project_type" label="项目类型" min-width="120" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getRequirementStatusTag(row.status)" size="small">
                  {{ formatRequirementStatus(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="160">
              <template #default="{ row }">
                {{ formatDate(row.created_at) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近订单</span>
              <el-button type="primary" link @click="$router.push('/admin/orders')">
                查看更多
              </el-button>
            </div>
          </template>
          <el-table :data="dashboardStats.recent_orders || []" stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="amount" label="金额" width="100">
              <template #default="{ row }">
                <span style="color: #F56C6C; font-weight: bold;">¥{{ row.amount }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getOrderStatusTag(row.status)" size="small">
                  {{ formatOrderStatus(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="payment_type" label="支付方式" width="100">
              <template #default="{ row }">
                {{ formatPaymentType(row.payment_type) }}
              </template>
            </el-table-column>
            <el-table-column prop="created_at" label="创建时间" width="160">
              <template #default="{ row }">
                {{ formatDate(row.created_at) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第五行：最近事件 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近事件</span>
            </div>
          </template>
          <el-table :data="dashboardStats.recent_events || []" stripe>
            <el-table-column prop="event_type" label="事件类型" width="120">
              <template #default="{ row }">
                <el-tag :type="getEventTypeTag(row.event_type)">{{ row.event_type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="platform" label="平台" width="120" />
            <el-table-column prop="user_id" label="用户ID" width="100" />
            <el-table-column prop="created_at" label="时间">
              <template #default="{ row }">
                {{ formatDate(row.created_at) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { View, Pointer, User, TrendCharts, ShoppingCart, Money } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'

use([
  CanvasRenderer,
  LineChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

const dashboardStats = ref({
  total_views: 0,
  total_clicks: 0,
  total_registrations: 0,
  total_conversions: 0,
  total_orders: 0,
  total_revenue: 0,
  overall_click_rate: 0,
  overall_conversion_rate: 0,
  top_platforms: [],
  recent_events: [],
  recent_requirements: [],
  recent_orders: []
})

const conversionChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['点击率', '转化率'] },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  },
  yAxis: { type: 'value', axisLabel: { formatter: '{value}%' } },
  series: [
    {
      name: '点击率',
      type: 'line',
      smooth: true,
      data: [3.2, 4.1, 3.8, 5.2, 4.8, 6.1, 5.5]
    },
    {
      name: '转化率',
      type: 'line',
      smooth: true,
      data: [1.2, 1.5, 1.3, 1.8, 1.6, 2.1, 1.9]
    }
  ]
}))

const platformChartOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { orient: 'vertical', left: 'left' },
  series: [
    {
      name: '平台分布',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 20, fontWeight: 'bold' }
      },
      data: dashboardStats.value.top_platforms?.map(p => ({
        value: p.count,
        name: p.platform
      })) || []
    }
  ]
}))

// 格式化金额
const formatAmount = (amount) => {
  if (!amount) return '0.00'
  return parseFloat(amount).toFixed(2)
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 用户类型
const formatUserType = (type) => {
  const typeMap = {
    'individual': '个人',
    'company': '企业',
    'studio': '工作室'
  }
  return typeMap[type] || type
}

const getUserTypeTag = (type) => {
  const tagMap = {
    'individual': 'primary',
    'company': 'success',
    'studio': 'warning'
  }
  return tagMap[type] || 'info'
}

// 需求状态
const formatRequirementStatus = (status) => {
  const statusMap = {
    'pending': '待处理',
    'processing': '处理中',
    'completed': '已完成',
    'cancelled': '已取消'
  }
  return statusMap[status] || status
}

const getRequirementStatusTag = (status) => {
  const tagMap = {
    'pending': 'warning',
    'processing': 'primary',
    'completed': 'success',
    'cancelled': 'info'
  }
  return tagMap[status] || 'info'
}

// 订单状态
const formatOrderStatus = (status) => {
  const statusMap = {
    'pending': '待支付',
    'paid': '已支付',
    'processing': '处理中',
    'completed': '已完成',
    'cancelled': '已取消',
    'refunded': '已退款'
  }
  return statusMap[status] || status
}

const getOrderStatusTag = (status) => {
  const tagMap = {
    'pending': 'warning',
    'paid': 'success',
    'processing': 'primary',
    'completed': 'success',
    'cancelled': 'info',
    'refunded': 'danger'
  }
  return tagMap[status] || 'info'
}

// 支付方式
const formatPaymentType = (type) => {
  const typeMap = {
    'alipay': '支付宝',
    'wechat': '微信支付',
    'bank': '银行转账'
  }
  return typeMap[type] || type
}

// 事件类型
const getEventTypeTag = (type) => {
  const tagMap = {
    'view': 'info',
    'click': 'success',
    'register': 'warning',
    'convert': 'danger'
  }
  return tagMap[type] || 'info'
}

const fetchDashboardData = async () => {
  try {
    console.log('开始获取仪表盘数据...')
    const response = await api.getDashboardStats()
    console.log('获取仪表盘数据成功:', response)
    dashboardStats.value = {
      ...dashboardStats.value,
      ...response
    }
  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
    ElMessage.error('获取仪表盘数据失败: ' + (error.message || '未知错误'))
  }
}

onMounted(() => {
  fetchDashboardData()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-tag) {
  font-size: 12px;
}
</style>
