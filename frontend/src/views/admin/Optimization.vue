<template>
  <div class="optimization">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>策略优化规则</span>
              <div>
                <el-select v-model="optimizationStrategy" style="width: 150px; margin-right: 10px;">
                  <el-option label="贝叶斯优化" value="bayesian" />
                  <el-option label="启发式算法" value="heuristic" />
                  <el-option label="汤普森采样" value="thompson_sampling" />
                </el-select>
                <el-button type="success" @click="handleOptimize" :loading="optimizing">
                  <el-icon><Refresh /></el-icon>
                  执行优化
                </el-button>
                <el-button type="primary" @click="handleCreateRule">
                  <el-icon><Plus /></el-icon>
                  创建规则
                </el-button>
              </div>
            </div>
          </template>

          <el-table :data="rules" stripe style="width: 100%">
            <el-table-column prop="name" label="规则名称" width="200" />
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column prop="rule_type" label="规则类型" width="140">
              <template #default="{ row }">
                <el-tag>{{ getRuleTypeText(row.rule_type) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="platform" label="适用平台" width="120" />
            <el-table-column prop="priority" label="优先级" width="80" sortable />
            <el-table-column prop="performance_score" label="性能评分" width="100">
              <template #default="{ row }">
                <el-progress
                  :percentage="Math.round(row.performance_score * 100)"
                  :color="getScoreColor(row.performance_score)"
                />
              </template>
            </el-table-column>
            <el-table-column prop="is_active" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.is_active ? 'success' : 'info'">
                  {{ row.is_active ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="last_evaluated_at" label="上次评估" width="180">
              <template #default="{ row }">
                {{ row.last_evaluated_at ? formatDate(row.last_evaluated_at) : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button size="small" @click="handleEdit(row)">编辑</el-button>
                <el-button
                  size="small"
                  :type="row.is_active ? 'warning' : 'success'"
                  @click="handleToggleActive(row)"
                >
                  {{ row.is_active ? '禁用' : '启用' }}
                </el-button>
                <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 优化结果展示 -->
    <el-row :gutter="20" style="margin-top: 20px;" v-if="optimizationResult">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>优化分析结果</span>
              <el-tag type="success">{{ optimizationResult.strategy }} 策略</el-tag>
            </div>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <h4>平台权重分布</h4>
              <el-table :data="platformWeightData" stripe size="small">
                <el-table-column prop="platform" label="平台" width="120" />
                <el-table-column prop="weight" label="权重" width="100">
                  <template #default="{ row }">
                    <el-progress :percentage="row.weight" :color="getWeightColor(row.weight)" />
                  </template>
                </el-table-column>
                <el-table-column prop="confidence" label="置信度" width="100">
                  <template #default="{ row }">
                    <el-tag :type="getConfidenceType(row.confidence)" size="small">
                      {{ row.confidenceText }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="expectedRate" label="预期转化率" />
              </el-table>
            </el-col>
            <el-col :span="12">
              <h4>最佳发布时段 TOP5</h4>
              <el-timeline>
                <el-timeline-item
                  v-for="(slot, index) in optimizationResult.optimal_time_slots"
                  :key="index"
                  :type="index < 3 ? 'primary' : 'info'"
                >
                  <span style="font-weight: bold;">{{ slot.hour }}:00 - {{ slot.hour + 1 }}:00</span>
                  <span style="margin-left: 10px; color: #67C23A;">转化率 {{ slot.conversion_rate }}%</span>
                </el-timeline-item>
              </el-timeline>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>平台转化率统计</span>
          </template>
          <el-table :data="conversionStats" stripe>
            <el-table-column prop="platform" label="平台" width="120" />
            <el-table-column prop="views" label="浏览量" width="100" />
            <el-table-column prop="clicks" label="点击量" width="100" />
            <el-table-column prop="click_rate" label="点击率" width="100">
              <template #default="{ row }">
                {{ row.click_rate }}%
              </template>
            </el-table-column>
            <el-table-column prop="conversions" label="转化数" width="100" />
            <el-table-column prop="conversion_rate" label="转化率" width="100">
              <template #default="{ row }">
                {{ row.conversion_rate }}%
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>智能优化建议</span>
              <el-tag v-if="suggestions.length > 0" type="warning">{{ suggestions.length }} 条建议</el-tag>
            </div>
          </template>
          <div v-if="suggestions.length === 0" class="empty-suggestions">
            <el-empty description="暂无优化建议，请执行优化分析" />
          </div>
          <el-timeline v-else>
            <el-timeline-item
              v-for="(item, index) in suggestions"
              :key="index"
              :type="getSuggestionType(item.priority)"
              :timestamp="`优先级: ${item.priority}/5`"
            >
              <el-card :body-style="{ padding: '10px' }" :class="['suggestion-card', `priority-${item.priority}`]">
                <div class="suggestion-title">{{ item.title }}</div>
                <div class="suggestion-desc">{{ item.description }}</div>
                <div class="suggestion-impact">
                  <el-icon><InfoFilled /></el-icon>
                  预期效果: {{ item.expected_impact }}
                </div>
                <div v-if="item.actionable" class="suggestion-action">
                  <el-button type="primary" size="small" @click="applySuggestion(item)">
                    应用建议
                  </el-button>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="规则名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="规则类型">
          <el-select v-model="form.rule_type">
            <el-option label="时段优化" value="time_slot" />
            <el-option label="平台优先级" value="platform_priority" />
            <el-option label="物料类型" value="material_type" />
            <el-option label="发布频率" value="frequency" />
          </el-select>
        </el-form-item>
        <el-form-item label="适用平台">
          <el-select v-model="form.platform" placeholder="全部平台" clearable>
            <el-option
              v-for="platform in platforms"
              :key="platform"
              :label="platform"
              :value="platform"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="form.priority" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="条件配置">
          <el-input
            v-model="conditionsJson"
            type="textarea"
            :rows="4"
            placeholder='{"min_hour": 9, "max_hour": 21}'
          />
        </el-form-item>
        <el-form-item label="动作配置">
          <el-input
            v-model="actionsJson"
            type="textarea"
            :rows="4"
            placeholder='{"weight": 1.5}'
          />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="form.is_active" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Plus, Refresh, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/utils/api'

const rules = ref([])
const conversionStats = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const conditionsJson = ref('')
const actionsJson = ref('')
const optimizing = ref(false)
const optimizationStrategy = ref('bayesian')
const optimizationResult = ref(null)

const form = ref({
  id: null,
  name: '',
  description: '',
  rule_type: '',
  platform: '',
  priority: 0,
  conditions: null,
  actions: null,
  is_active: true
})

const platforms = [
  'wechat', 'xiaohongshu', 'zhihu', 'douyin', 'bilibili',
  'weibo', 'xianyu', 'wuba', 'tieba', 'douban'
]

const suggestions = computed(() => {
  return optimizationResult.value?.suggestions || []
})

const platformWeightData = computed(() => {
  if (!optimizationResult.value?.optimized_weights) return []
  
  return Object.entries(optimizationResult.value.optimized_weights).map(([platform, data]) => ({
    platform,
    weight: typeof data === 'object' ? data.weight || 0 : data,
    confidence: typeof data === 'object' ? data.confidence || 'medium' : 'medium',
    confidenceText: typeof data === 'object' ? 
      (data.confidence === 'high' ? '高' : data.confidence === 'medium' ? '中' : '低') : '中',
    expectedRate: typeof data === 'object' ? `${data.expected_conversion_rate || 0}%` : '-'
  })).sort((a, b) => b.weight - a.weight)
})

const getRuleTypeText = (type) => {
  const map = {
    'time_slot': '时段优化',
    'platform_priority': '平台优先级',
    'material_type': '物料类型',
    'frequency': '发布频率'
  }
  return map[type] || type
}

const getScoreColor = (score) => {
  if (score >= 0.8) return '#67C23A'
  if (score >= 0.5) return '#E6A23C'
  return '#F56C6C'
}

const getWeightColor = (weight) => {
  if (weight >= 70) return '#67C23A'
  if (weight >= 40) return '#E6A23C'
  return '#F56C6C'
}

const getConfidenceType = (confidence) => {
  const map = {
    'high': 'success',
    'medium': 'warning',
    'low': 'info'
  }
  return map[confidence] || 'info'
}

const getSuggestionType = (priority) => {
  if (priority >= 5) return 'danger'
  if (priority >= 4) return 'warning'
  if (priority >= 3) return 'primary'
  return 'info'
}

const formatDate = (dateStr) => {
  return new Date(dateStr).toLocaleString('zh-CN')
}

const fetchRules = async () => {
  try {
    const response = await api.getOptimizationRules()
    rules.value = response
  } catch (error) {
    ElMessage.error('获取规则列表失败')
  }
}

const fetchConversionStats = async () => {
  try {
    const response = await api.getConversionStats()
    conversionStats.value = response
  } catch (error) {
    ElMessage.error('获取转化统计失败')
  }
}

const handleCreateRule = () => {
  isEdit.value = false
  dialogTitle.value = '创建规则'
  form.value = {
    id: null,
    name: '',
    description: '',
    rule_type: '',
    platform: '',
    priority: 0,
    conditions: null,
    actions: null,
    is_active: true
  }
  conditionsJson.value = ''
  actionsJson.value = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑规则'
  form.value = { ...row }
  conditionsJson.value = row.conditions ? JSON.stringify(row.conditions, null, 2) : ''
  actionsJson.value = row.actions ? JSON.stringify(row.actions, null, 2) : ''
  dialogVisible.value = true
}

const handleToggleActive = async (row) => {
  try {
    await api.updateOptimizationRule(row.id, { is_active: !row.is_active })
    ElMessage.success('操作成功')
    fetchRules()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该规则吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await api.deleteOptimizationRule(row.id)
    ElMessage.success('删除成功')
    fetchRules()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleOptimize = async () => {
  try {
    optimizing.value = true
    const loading = ElMessage.loading('正在执行策略优化分析...', 0)
    const response = await api.runOptimization(optimizationStrategy.value, 30)
    optimizationResult.value = response
    loading.close()
    ElMessage.success('策略优化完成')
    fetchRules()
    fetchConversionStats()
  } catch (error) {
    ElMessage.error('优化失败')
    console.error(error)
  } finally {
    optimizing.value = false
  }
}

const applySuggestion = async (suggestion) => {
  try {
    await ElMessageBox.confirm(
      `确定要应用"${suggestion.title}"这个建议吗？`,
      '确认应用',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' }
    )
    
    // 根据建议类型执行相应操作
    if (suggestion.type === 'platform_priority' && suggestion.action_params) {
      // 更新平台优先级规则
      for (const platform of suggestion.action_params.platforms) {
        await api.createOptimizationRule({
          name: `${platform} 平台优先`,
          description: `自动优化：${suggestion.description}`,
          rule_type: 'platform_priority',
          platform: platform,
          priority: 10,
          is_active: true,
          actions: { weight: suggestion.action_params.weights[suggestion.action_params.platforms.indexOf(platform)] }
        })
      }
    }
    
    ElMessage.success('建议已应用')
    fetchRules()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('应用失败')
    }
  }
}

const handleSubmit = async () => {
  try {
    const data = { ...form.value }
    if (conditionsJson.value) {
      data.conditions = JSON.parse(conditionsJson.value)
    }
    if (actionsJson.value) {
      data.actions = JSON.parse(actionsJson.value)
    }
    
    if (isEdit.value) {
      await api.updateOptimizationRule(data.id, data)
    } else {
      await api.createOptimizationRule(data)
    }
    
    ElMessage.success('操作成功')
    dialogVisible.value = false
    fetchRules()
  } catch (error) {
    if (error instanceof SyntaxError) {
      ElMessage.error('JSON格式错误')
    } else {
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  fetchRules()
  fetchConversionStats()
})
</script>

<style scoped>
.optimization {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}

.empty-suggestions {
  padding: 40px 0;
}

.suggestion-card {
  margin-bottom: 5px;
  border-left: 4px solid #909399;
}

.suggestion-card.priority-5 {
  border-left-color: #F56C6C;
}

.suggestion-card.priority-4 {
  border-left-color: #E6A23C;
}

.suggestion-card.priority-3 {
  border-left-color: #409EFF;
}

.suggestion-card.priority-2,
.suggestion-card.priority-1 {
  border-left-color: #67C23A;
}

.suggestion-title {
  font-weight: bold;
  font-size: 14px;
  margin-bottom: 8px;
  color: #303133;
}

.suggestion-desc {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
  line-height: 1.5;
}

.suggestion-impact {
  font-size: 12px;
  color: #67C23A;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.suggestion-action {
  margin-top: 10px;
}

h4 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 16px;
}
</style>
