<template>
  <div class="ab-test">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>A/B测试管理</span>
          <el-button type="primary" @click="handleCreateExperiment">
            <el-icon><Plus /></el-icon>
            创建实验
          </el-button>
        </div>
      </template>

      <el-table :data="experiments" stripe style="width: 100%">
        <el-table-column prop="name" label="实验名称" width="200" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="platform" label="平台" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="变体数" width="100">
          <template #default="{ row }">
            {{ row.variants?.length || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="start_date" label="开始时间" width="180">
          <template #default="{ row }">
            {{ row.start_date ? formatDate(row.start_date) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="end_date" label="结束时间" width="180">
          <template #default="{ row }">
            {{ row.end_date ? formatDate(row.end_date) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button size="small" type="primary" @click="handleViewStats(row)">统计分析</el-button>
            <el-button
              v-if="row.status === 'draft'"
              size="small"
              type="success"
              @click="handleStart(row)"
            >
              启动
            </el-button>
            <el-button
              v-if="row.status === 'running'"
              size="small"
              type="warning"
              @click="handlePause(row)"
            >
              暂停
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑实验对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="实验名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="平台">
          <el-select v-model="form.platform" placeholder="请选择平台">
            <el-option
              v-for="platform in platforms"
              :key="platform"
              :label="platform"
              :value="platform"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="form.start_date"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="form.end_date"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="最小样本量">
          <el-input-number v-model="form.min_sample_size" :min="100" />
        </el-form-item>
        <el-form-item label="置信度">
          <el-slider
            v-model="form.confidence_level"
            :min="0.8"
            :max="0.99"
            :step="0.01"
            :format-tooltip="(val) => `${(val * 100).toFixed(0)}%`"
          />
        </el-form-item>

        <el-divider content-position="left">实验变体</el-divider>
        
        <el-button type="primary" size="small" @click="handleAddVariant" style="margin-bottom: 20px;">
          <el-icon><Plus /></el-icon>
          添加变体
        </el-button>

        <el-table :data="form.variants" border>
          <el-table-column prop="name" label="变体名称" width="150">
            <template #default="{ row, $index }">
              <el-input v-model="row.name" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="对照组" width="100">
            <template #default="{ row }">
              <el-checkbox v-model="row.is_control" size="small" />
            </template>
          </el-table-column>
          <el-table-column prop="traffic_ratio" label="流量比例" width="150">
            <template #default="{ row }">
              <el-input-number
                v-model="row.traffic_ratio"
                size="small"
                :min="0"
                :max="1"
                :step="0.1"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ $index }">
              <el-button
                type="danger"
                size="small"
                link
                @click="handleRemoveVariant($index)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 实验详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="实验详情" width="900px">
      <el-descriptions :column="2" border v-if="currentExperiment">
        <el-descriptions-item label="实验名称">{{ currentExperiment.name }}</el-descriptions-item>
        <el-descriptions-item label="平台">{{ currentExperiment.platform }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentExperiment.status)">
            {{ getStatusText(currentExperiment.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="置信度">{{ (currentExperiment.confidence_level * 100).toFixed(0) }}%</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ currentExperiment.start_date ? formatDate(currentExperiment.start_date) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ currentExperiment.end_date ? formatDate(currentExperiment.end_date) : '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider />
      <h4>变体数据</h4>
      <el-table :data="currentExperiment?.variants || []" stripe>
        <el-table-column prop="name" label="变体名称" width="150" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.is_control" type="success">对照组</el-tag>
            <el-tag v-else type="primary">实验组</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="traffic_ratio" label="流量比例" width="120">
          <template #default="{ row }">
            {{ (row.traffic_ratio * 100).toFixed(0) }}%
          </template>
        </el-table-column>
        <el-table-column prop="views" label="浏览量" width="100" />
        <el-table-column prop="clicks" label="点击量" width="100" />
        <el-table-column prop="registrations" label="注册数" width="100" />
        <el-table-column prop="conversions" label="转化数" width="100" />
        <el-table-column label="转化率" width="120">
          <template #default="{ row }">
            {{ row.clicks > 0 ? ((row.conversions / row.clicks) * 100).toFixed(2) : 0 }}%
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 统计分析对话框 -->
    <el-dialog v-model="statsDialogVisible" title="A/B测试统计分析" width="1000px">
      <div v-if="statsLoading" class="stats-loading">
        <el-skeleton :rows="6" animated />
      </div>
      <div v-else-if="statsResult">
        <el-alert
          :title="`实验: ${statsResult.experiment_name}`"
          :type="hasSignificantResult ? 'success' : 'info'"
          :description="`置信水平: ${(statsResult.confidence_level * 100).toFixed(0)}% | 对照组: ${statsResult.control_variant}`"
          show-icon
          :closable="false"
          style="margin-bottom: 20px;"
        />

        <el-table :data="statsResult.results" stripe>
          <el-table-column prop="variant_name" label="变体名称" width="150" />
          <el-table-column label="对照组数据" width="150">
            <template #default="{ row }">
              <div>转化: {{ row.control_conversions }}/{{ row.control_clicks }}</div>
              <div>转化率: {{ row.control_rate }}%</div>
            </template>
          </el-table-column>
          <el-table-column label="实验组数据" width="150">
            <template #default="{ row }">
              <div>转化: {{ row.variant_conversions }}/{{ row.variant_clicks }}</div>
              <div>转化率: {{ row.variant_rate }}%</div>
            </template>
          </el-table-column>
          <el-table-column label="相对提升" width="120">
            <template #default="{ row }">
              <span :class="row.relative_lift > 0 ? 'positive' : 'negative'">
                {{ row.relative_lift > 0 ? '+' : '' }}{{ row.relative_lift }}%
              </span>
            </template>
          </el-table-column>
          <el-table-column label="P值" width="120">
            <template #default="{ row }">
              <el-tag :type="getPValueType(row.p_value, statsResult.confidence_level)" size="small">
                {{ row.p_value.toFixed(4) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="置信区间" width="150">
            <template #default="{ row }">
              [{{ row.confidence_interval[0] }}%, {{ row.confidence_interval[1] }}%]
            </template>
          </el-table-column>
          <el-table-column label="显著性" width="100">
            <template #default="{ row }">
              <el-tag :type="row.is_significant ? 'success' : 'info'" size="small">
                {{ row.is_significant ? '显著' : '不显著' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>

        <el-divider />
        <h4>统计结论与建议</h4>
        <el-timeline>
          <el-timeline-item
            v-for="(result, index) in statsResult.results"
            :key="index"
            :type="result.is_significant ? 'success' : 'warning'"
          >
            <el-card :body-style="{ padding: '15px' }">
              <div class="stats-result-title">
                {{ result.variant_name }} vs {{ statsResult.control_variant }}
              </div>
              <div class="stats-result-detail">
                <p><strong>统计检验：</strong>双比例Z检验</p>
                <p><strong>P值：</strong>{{ result.p_value.toFixed(4) }}</p>
                <p><strong>置信区间：</strong>[{{ result.confidence_interval[0] }}%, {{ result.confidence_interval[1] }}%]</p>
                <p><strong>结论：</strong>{{ result.recommendation }}</p>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>

        <el-divider />
        <h4>统计说明</h4>
        <el-collapse>
          <el-collapse-item title="P值解释">
            <p>P值表示在原假设（两组无差异）成立的情况下，观察到当前或更极端结果的概率。</p>
            <ul>
              <li>P < 0.05：结果在95%置信水平下显著</li>
              <li>P < 0.01：结果在99%置信水平下显著</li>
              <li>P ≥ 0.05：结果不显著，差异可能是随机波动</li>
            </ul>
          </el-collapse-item>
          <el-collapse-item title="置信区间解释">
            <p>置信区间表示真实转化率可能落入的范围。如果对照组和实验组的置信区间不重叠，说明差异显著。</p>
          </el-collapse-item>
          <el-collapse-item title="相对提升计算">
            <p>相对提升 = (实验组转化率 - 对照组转化率) / 对照组转化率 × 100%</p>
          </el-collapse-item>
        </el-collapse>
      </div>
      <div v-else class="stats-empty">
        <el-empty description="暂无统计数据" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/utils/api'

const experiments = ref([])
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const statsDialogVisible = ref(false)
const dialogTitle = ref('')
const currentExperiment = ref(null)
const statsResult = ref(null)
const statsLoading = ref(false)

const form = ref({
  name: '',
  description: '',
  platform: '',
  start_date: null,
  end_date: null,
  min_sample_size: 1000,
  confidence_level: 0.95,
  variants: [
    { name: '对照组', is_control: true, traffic_ratio: 0.5 },
    { name: '实验组', is_control: false, traffic_ratio: 0.5 }
  ]
})

const platforms = [
  'wechat', 'xiaohongshu', 'zhihu', 'douyin', 'bilibili',
  'weibo', 'xianyu', 'wuba', 'tieba', 'douban'
]

const hasSignificantResult = computed(() => {
  return statsResult.value?.results?.some(r => r.is_significant)
})

const getStatusType = (status) => {
  const typeMap = {
    'draft': 'info',
    'running': 'success',
    'paused': 'warning',
    'completed': ''
  }
  return typeMap[status] || ''
}

const getStatusText = (status) => {
  const textMap = {
    'draft': '草稿',
    'running': '运行中',
    'paused': '已暂停',
    'completed': '已完成'
  }
  return textMap[status] || status
}

const getPValueType = (pValue, confidenceLevel) => {
  const alpha = 1 - confidenceLevel
  if (pValue < alpha) return 'success'
  if (pValue < 0.1) return 'warning'
  return 'info'
}

const formatDate = (dateStr) => {
  return new Date(dateStr).toLocaleString('zh-CN')
}

const fetchExperiments = async () => {
  try {
    const response = await api.getExperiments()
    experiments.value = response
  } catch (error) {
    ElMessage.error('获取实验列表失败')
  }
}

const handleCreateExperiment = () => {
  dialogTitle.value = '创建实验'
  form.value = {
    name: '',
    description: '',
    platform: '',
    start_date: null,
    end_date: null,
    min_sample_size: 1000,
    confidence_level: 0.95,
    variants: [
      { name: '对照组', is_control: true, traffic_ratio: 0.5 },
      { name: '实验组', is_control: false, traffic_ratio: 0.5 }
    ]
  }
  dialogVisible.value = true
}

const handleView = async (row) => {
  try {
    const response = await api.getExperiment(row.id)
    currentExperiment.value = response
    viewDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取实验详情失败')
  }
}

const handleViewStats = async (row) => {
  statsDialogVisible.value = true
  statsLoading.value = true
  statsResult.value = null
  
  try {
    const response = await api.getExperimentResults(row.id)
    statsResult.value = response
  } catch (error) {
    ElMessage.error('获取统计结果失败：' + (error.response?.data?.detail || '数据不足'))
  } finally {
    statsLoading.value = false
  }
}

const handleStart = async (row) => {
  try {
    await api.updateExperiment(row.id, { status: 'running' })
    ElMessage.success('实验已启动')
    fetchExperiments()
  } catch (error) {
    ElMessage.error('启动失败')
  }
}

const handlePause = async (row) => {
  try {
    await api.updateExperiment(row.id, { status: 'paused' })
    ElMessage.success('实验已暂停')
    fetchExperiments()
  } catch (error) {
    ElMessage.error('暂停失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该实验吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ElMessage.success('删除成功')
    fetchExperiments()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleAddVariant = () => {
  form.value.variants.push({
    name: `变体${form.value.variants.length + 1}`,
    is_control: false,
    traffic_ratio: 0
  })
}

const handleRemoveVariant = (index) => {
  form.value.variants.splice(index, 1)
}

const handleSubmit = async () => {
  try {
    await api.createExperiment(form.value)
    ElMessage.success('创建成功')
    dialogVisible.value = false
    fetchExperiments()
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

onMounted(() => {
  fetchExperiments()
})
</script>

<style scoped>
.ab-test {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}

.stats-loading {
  padding: 20px;
}

.stats-empty {
  padding: 40px 0;
}

.stats-result-title {
  font-weight: bold;
  font-size: 16px;
  margin-bottom: 10px;
  color: #303133;
}

.stats-result-detail {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
}

.stats-result-detail p {
  margin: 5px 0;
}

.positive {
  color: #67C23A;
  font-weight: bold;
}

.negative {
  color: #F56C6C;
  font-weight: bold;
}

h4 {
  margin: 20px 0 15px 0;
  color: #303133;
  font-size: 16px;
}
</style>
