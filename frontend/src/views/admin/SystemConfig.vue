<template>
  <div class="system-config">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统配置</span>
          <el-button type="primary" @click="handleSave">
            <el-icon><Check /></el-icon>
            保存配置
          </el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="AI服务配置" name="ai">
          <el-form :model="config.ai_service" label-width="150px">
            <el-form-item label="启用AI服务">
              <el-switch v-model="config.ai_service.enabled" />
            </el-form-item>
            <el-form-item label="AI服务提供商">
              <el-select v-model="config.ai_service.provider">
                <el-option label="OpenAI" value="openai" />
                <el-option label="文心一言" value="wenxin" />
                <el-option label="通义千问" value="tongyi" />
              </el-select>
            </el-form-item>
            <el-form-item label="API密钥">
              <el-input v-model="config.ai_service.api_key" type="password" show-password />
            </el-form-item>
            <el-form-item label="API基础URL">
              <el-input v-model="config.ai_service.base_url" />
            </el-form-item>
            <el-form-item label="超时时间(秒)">
              <el-input-number v-model="config.ai_service.timeout" :min="10" :max="120" />
            </el-form-item>
            <el-form-item label="模型名称">
              <el-input v-model="config.ai_service.model" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="发布配置" name="publishing">
          <el-form :model="config.publishing" label-width="150px">
            <el-form-item label="最大重试次数">
              <el-input-number v-model="config.publishing.max_retries" :min="1" :max="10" />
            </el-form-item>
            <el-form-item label="重试延迟(秒)">
              <el-input-number v-model="config.publishing.retry_delay" :min="10" :max="300" />
            </el-form-item>
            <el-form-item label="批量发布数量">
              <el-input-number v-model="config.publishing.batch_size" :min="1" :max="50" />
            </el-form-item>
            <el-form-item label="并发发布数">
              <el-input-number v-model="config.publishing.concurrent_limit" :min="1" :max="20" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="数据分析配置" name="analytics">
          <el-form :model="config.analytics" label-width="150px">
            <el-form-item label="数据保留天数">
              <el-input-number v-model="config.analytics.retention_days" :min="7" :max="365" />
            </el-form-item>
            <el-form-item label="A/B测试置信度">
              <el-slider
                v-model="config.analytics.ab_test_confidence"
                :min="0.8"
                :max="0.99"
                :step="0.01"
                :format-tooltip="(val) => `${(val * 100).toFixed(0)}%`"
              />
            </el-form-item>
            <el-form-item label="自动优化周期(天)">
              <el-input-number v-model="config.analytics.optimization_cycle" :min="1" :max="30" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="积分规则配置" name="points">
          <el-card style="margin-bottom: 20px;">
            <template #header>
              <span>推荐奖励规则</span>
            </template>
            <el-form :model="config.points.referral" label-width="150px">
              <el-form-item label="一级推荐奖励">
                <el-input-number v-model="config.points.referral.level1" :min="0" />
              </el-form-item>
              <el-form-item label="二级推荐奖励">
                <el-input-number v-model="config.points.referral.level2" :min="0" />
              </el-form-item>
              <el-form-item label="三级推荐奖励">
                <el-input-number v-model="config.points.referral.level3" :min="0" />
              </el-form-item>
            </el-form>
          </el-card>

          <el-card>
            <template #header>
              <span>任务奖励规则</span>
            </template>
            <el-form :model="config.points.tasks" label-width="150px">
              <el-form-item label="发布内容奖励">
                <el-input-number v-model="config.points.tasks.publish" :min="0" />
              </el-form-item>
              <el-form-item label="生成物料奖励">
                <el-input-number v-model="config.points.tasks.generate_material" :min="0" />
              </el-form-item>
              <el-form-item label="每日签到奖励">
                <el-input-number v-model="config.points.tasks.daily_checkin" :min="0" />
              </el-form-item>
            </el-form>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Check } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'

const activeTab = ref('ai')
const config = ref({
  ai_service: {
    enabled: true,
    provider: 'openai',
    api_key: '',
    base_url: 'https://api.openai.com/v1',
    timeout: 30,
    model: 'gpt-4'
  },
  publishing: {
    max_retries: 3,
    retry_delay: 60,
    batch_size: 10,
    concurrent_limit: 5
  },
  analytics: {
    retention_days: 90,
    ab_test_confidence: 0.95,
    optimization_cycle: 7
  },
  points: {
    referral: {
      level1: 100,
      level2: 30,
      level3: 10
    },
    tasks: {
      publish: 10,
      generate_material: 5,
      daily_checkin: 1
    }
  }
})

const fetchConfig = async () => {
  try {
    const response = await api.getSystemConfig()
    if (response) {
      config.value = { ...config.value, ...response }
    }
  } catch (error) {
    console.error('获取配置失败:', error)
  }
}

const handleSave = async () => {
  try {
    await api.updateSystemConfig(config.value)
    ElMessage.success('配置保存成功')
  } catch (error) {
    ElMessage.error('配置保存失败')
  }
}

onMounted(() => {
  fetchConfig()
})
</script>

<style scoped>
.system-config {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}

:deep(.el-tabs__content) {
  padding-top: 20px;
}
</style>
