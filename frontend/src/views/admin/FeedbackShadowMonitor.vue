<template>
  <div class="feedback-shadow-monitor">
    <h1>Feedback Shadow 监控中心</h1>
    
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card">
        <div class="stat-value">{{ stats.totalValidations }}</div>
        <div class="stat-label">总验证次数</div>
      </el-card>
      <el-card class="stat-card success">
        <div class="stat-value">{{ stats.allowCount }}</div>
        <div class="stat-label">ALLOW (通过)</div>
      </el-card>
      <el-card class="stat-card warning">
        <div class="stat-value">{{ stats.repairCount }}</div>
        <div class="stat-label">REPAIR (需修复)</div>
      </el-card>
      <el-card class="stat-card danger">
        <div class="stat-value">{{ stats.rejectCount }}</div>
        <div class="stat-label">REJECT (拒绝)</div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-value">{{ stats.avgResponseTime }}ms</div>
        <div class="stat-label">平均响应时间</div>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <el-card class="chart-card">
        <template #header>
          <span>验证决策分布</span>
        </template>
        <div ref="decisionChart" class="chart"></div>
      </el-card>
      
      <el-card class="chart-card">
        <template #header>
          <span>验证趋势（近7天）</span>
        </template>
        <div ref="trendChart" class="chart"></div>
      </el-card>
    </div>

    <!-- 验证记录列表 -->
    <el-card class="records-card">
      <template #header>
        <div class="card-header">
          <span>验证记录</span>
          <div class="header-actions">
            <el-select v-model="filterDecision" placeholder="筛选决策" clearable>
              <el-option label="ALLOW" value="ALLOW" />
              <el-option label="REPAIR" value="REPAIR" />
              <el-option label="REJECT" value="REJECT" />
            </el-select>
            <el-button type="primary" @click="fetchRecords">刷新</el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="filteredRecords" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="projectId" label="项目ID" width="120" />
        <el-table-column prop="taskId" label="任务ID" width="120" />
        <el-table-column prop="documentType" label="文档类型" width="120" />
        <el-table-column prop="decision" label="决策" width="100">
          <template #default="{ row }">
            <el-tag :type="getDecisionType(row.decision)">
              {{ row.decision }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="score" label="质量评分" width="100">
          <template #default="{ row }">
            <el-progress 
              :percentage="row.score || 0" 
              :status="getScoreStatus(row.score)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="tokenUsage" label="Token使用" width="100" />
        <el-table-column prop="responseTimeMs" label="响应时间" width="100">
          <template #default="{ row }">
            {{ row.responseTimeMs }}ms
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 自我修复记录 -->
    <el-card class="repair-card">
      <template #header>
        <span>自我修复记录</span>
      </template>
      <el-timeline>
        <el-timeline-item
          v-for="record in repairRecords"
          :key="record.id"
          :type="record.success ? 'success' : 'danger'"
          :timestamp="record.createdAt"
        >
          <h4>{{ record.projectId }} - {{ record.taskId }}</h4>
          <p>尝试次数: {{ record.attempts }}</p>
          <p>状态: {{ record.success ? '成功' : '失败' }}</p>
          <p v-if="record.errorMessage" class="error-text">{{ record.errorMessage }}</p>
        </el-timeline-item>
      </el-timeline>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="验证详情" width="800px">
      <div v-if="selectedRecord" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="项目ID">{{ selectedRecord.projectId }}</el-descriptions-item>
          <el-descriptions-item label="任务ID">{{ selectedRecord.taskId }}</el-descriptions-item>
          <el-descriptions-item label="验证类型">{{ selectedRecord.validationType }}</el-descriptions-item>
          <el-descriptions-item label="文档类型">{{ selectedRecord.documentType }}</el-descriptions-item>
          <el-descriptions-item label="决策">
            <el-tag :type="getDecisionType(selectedRecord.decision)">
              {{ selectedRecord.decision }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="质量评分">{{ selectedRecord.score }}/100</el-descriptions-item>
          <el-descriptions-item label="Token使用">{{ selectedRecord.tokenUsage }}</el-descriptions-item>
          <el-descriptions-item label="响应时间">{{ selectedRecord.responseTimeMs }}ms</el-descriptions-item>
        </el-descriptions>
        
        <div class="detail-section">
          <h4>发现的问题</h4>
          <pre>{{ formatIssues(selectedRecord.issues) }}</pre>
        </div>
        
        <div class="detail-section">
          <h4>改进建议</h4>
          <pre>{{ selectedRecord.suggestions }}</pre>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

// 统计数据
const stats = ref({
  totalValidations: 0,
  allowCount: 0,
  repairCount: 0,
  rejectCount: 0,
  avgResponseTime: 0
})

// 记录列表
const records = ref([])
const repairRecords = ref([])
const loading = ref(false)
const filterDecision = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 详情对话框
const detailVisible = ref(false)
const selectedRecord = ref(null)

// 图表引用
const decisionChart = ref(null)
const trendChart = ref(null)
let decisionChartInstance = null
let trendChartInstance = null

// 筛选后的记录
const filteredRecords = computed(() => {
  if (!filterDecision.value) return records.value
  return records.value.filter(r => r.decision === filterDecision.value)
})

// 获取决策标签类型
const getDecisionType = (decision) => {
  const types = {
    'ALLOW': 'success',
    'REPAIR': 'warning',
    'REJECT': 'danger'
  }
  return types[decision] || 'info'
}

// 获取评分状态
const getScoreStatus = (score) => {
  if (score >= 80) return 'success'
  if (score >= 60) return 'warning'
  return 'exception'
}

// 格式化问题列表
const formatIssues = (issues) => {
  if (!issues) return '无'
  try {
    const list = JSON.parse(issues)
    return list.join('\n')
  } catch {
    return issues
  }
}

// 获取统计数据
const fetchStats = async () => {
  try {
    const response = await fetch('/api/v1/feedback-shadow/stats')
    if (response.ok) {
      const data = await response.json()
      if (data.success) {
        stats.value = data.data
        updateCharts()
      }
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 获取验证记录
const fetchRecords = async () => {
  loading.value = true
  try {
    const response = await fetch(`/api/v1/feedback-shadow/records?page=${currentPage.value}&size=${pageSize.value}`)
    if (response.ok) {
      const data = await response.json()
      if (data.success) {
        records.value = data.data.records
        total.value = data.data.total
      }
    }
  } catch (error) {
    console.error('获取验证记录失败:', error)
    ElMessage.error('获取验证记录失败')
  } finally {
    loading.value = false
  }
}

// 获取修复记录
const fetchRepairRecords = async () => {
  try {
    const response = await fetch('/api/v1/feedback-shadow/repair-records')
    if (response.ok) {
      const data = await response.json()
      if (data.success) {
        repairRecords.value = data.data
      }
    }
  } catch (error) {
    console.error('获取修复记录失败:', error)
  }
}

// 查看详情
const viewDetail = (record) => {
  selectedRecord.value = record
  detailVisible.value = true
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchRecords()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchRecords()
}

// 初始化决策分布图表
const initDecisionChart = () => {
  if (!decisionChart.value) return
  
  decisionChartInstance = echarts.init(decisionChart.value)
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {c}'
        },
        data: [
          { value: 0, name: 'ALLOW', itemStyle: { color: '#67C23A' } },
          { value: 0, name: 'REPAIR', itemStyle: { color: '#E6A23C' } },
          { value: 0, name: 'REJECT', itemStyle: { color: '#F56C6C' } }
        ]
      }
    ]
  }
  decisionChartInstance.setOption(option)
}

// 初始化趋势图表
const initTrendChart = () => {
  if (!trendChart.value) return
  
  trendChartInstance = echarts.init(trendChart.value)
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['ALLOW', 'REPAIR', 'REJECT']
    },
    xAxis: {
      type: 'category',
      data: []
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: 'ALLOW',
        type: 'line',
        data: [],
        itemStyle: { color: '#67C23A' }
      },
      {
        name: 'REPAIR',
        type: 'line',
        data: [],
        itemStyle: { color: '#E6A23C' }
      },
      {
        name: 'REJECT',
        type: 'line',
        data: [],
        itemStyle: { color: '#F56C6C' }
      }
    ]
  }
  trendChartInstance.setOption(option)
}

// 更新图表
const updateCharts = () => {
  if (decisionChartInstance) {
    decisionChartInstance.setOption({
      series: [{
        data: [
          { value: stats.value.allowCount, name: 'ALLOW', itemStyle: { color: '#67C23A' } },
          { value: stats.value.repairCount, name: 'REPAIR', itemStyle: { color: '#E6A23C' } },
          { value: stats.value.rejectCount, name: 'REJECT', itemStyle: { color: '#F56C6C' } }
        ]
      }]
    })
  }
}

// 窗口大小改变时重新渲染图表
const handleResize = () => {
  decisionChartInstance?.resize()
  trendChartInstance?.resize()
}

onMounted(() => {
  fetchStats()
  fetchRecords()
  fetchRepairRecords()
  
  // 延迟初始化图表，确保DOM已渲染
  setTimeout(() => {
    initDecisionChart()
    initTrendChart()
  }, 100)
  
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  decisionChartInstance?.dispose()
  trendChartInstance?.dispose()
})
</script>

<style scoped>
.feedback-shadow-monitor {
  padding: 20px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

.stat-card.success {
  border-left: 4px solid #67C23A;
}

.stat-card.warning {
  border-left: 4px solid #E6A23C;
}

.stat-card.danger {
  border-left: 4px solid #F56C6C;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.chart-card {
  min-height: 350px;
}

.chart {
  height: 280px;
}

.records-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.repair-card {
  margin-bottom: 20px;
}

.error-text {
  color: #F56C6C;
}

.detail-content {
  .detail-section {
    margin-top: 20px;
    
    h4 {
      margin-bottom: 10px;
      color: #303133;
    }
    
    pre {
      background: #f5f7fa;
      padding: 15px;
      border-radius: 4px;
      overflow-x: auto;
      white-space: pre-wrap;
      word-wrap: break-word;
    }
  }
}

:deep(.el-pagination) {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
