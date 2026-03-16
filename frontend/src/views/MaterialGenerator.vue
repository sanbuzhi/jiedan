<template>
  <div class="material-generator">
    <el-container class="container">
      <el-header class="header">
        <div class="header-left">
          <h1>智能获客系统</h1>
        </div>
        <div class="header-right">
          <span v-if="userStore.userInfo" class="user-info">
            <el-icon><User /></el-icon>
            {{ userStore.userInfo.nickname || userStore.userInfo.phone }}
          </span>
          <el-button type="primary" link @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-container>
        <el-aside width="200px" class="aside">
          <el-menu
            :default-active="activeMenu"
            router
            background-color="#545c64"
            text-color="#fff"
            active-text-color="#ffd04b"
          >
            <el-menu-item index="/">
              <el-icon><House /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/material-generator">
              <el-icon><MagicStick /></el-icon>
              <span>物料生成</span>
            </el-menu-item>
            <el-menu-item index="/material-library">
              <el-icon><Files /></el-icon>
              <span>物料库</span>
            </el-menu-item>
            <el-menu-item index="/profile">
              <el-icon><User /></el-icon>
              <span>个人中心</span>
            </el-menu-item>
            <el-menu-item index="/referral">
              <el-icon><Share /></el-icon>
              <span>推荐分享</span>
            </el-menu-item>
            <el-menu-item index="/referral-tree">
              <el-icon><Connection /></el-icon>
              <span>推荐关系</span>
            </el-menu-item>
            <el-menu-item index="/points">
              <el-icon><Coin /></el-icon>
              <span>积分记录</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main class="main">
          <el-card class="generator-card">
            <template #header>
              <div class="card-header">
                <span>智能物料生成器</span>
              </div>
            </template>
            
            <el-form :model="form" label-width="100px" class="generator-form">
              <el-form-item label="关键词">
                <el-input
                  v-model="form.keywords"
                  placeholder="请输入关键词，多个关键词用逗号分隔"
                  clearable
                />
              </el-form-item>
              
              <el-form-item label="平台">
                <el-select v-model="form.platform" placeholder="请选择平台">
                  <el-option label="微信" value="wechat" />
                  <el-option label="抖音" value="douyin" />
                  <el-option label="小红书" value="xiaohongshu" />
                  <el-option label="知乎" value="zhihu" />
                  <el-option label="B站" value="bilibili" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="物料类型">
                <el-select v-model="form.material_type" placeholder="请选择物料类型">
                  <el-option label="文案" value="copy" />
                  <el-option label="图片描述" value="image_prompt" />
                  <el-option label="视频脚本" value="video_script" />
                  <el-option label="成功案例" value="case_study" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="语气">
                <el-select v-model="form.tone" placeholder="请选择语气">
                  <el-option label="友好亲切" value="friendly" />
                  <el-option label="专业严谨" value="professional" />
                  <el-option label="幽默风趣" value="humorous" />
                  <el-option label="充满激情" value="passionate" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="长度">
                <el-select v-model="form.length" placeholder="请选择长度">
                  <el-option label="简短" value="short" />
                  <el-option label="中等" value="medium" />
                  <el-option label="详细" value="long" />
                </el-select>
              </el-form-item>
              
              <el-form-item>
                <el-button
                  type="primary"
                  @click="handleGenerate"
                  :loading="generating"
                  :disabled="!form.keywords || !form.platform || !form.material_type"
                >
                  生成物料
                </el-button>
              </el-form-item>
            </el-form>
            
            <div v-if="generatedContent" class="result-section">
              <el-divider content-position="left">生成结果</el-divider>
              
              <el-alert
                v-if="generatedContent.is_fallback"
                title="使用预设模板"
                type="warning"
                :closable="false"
                class="fallback-alert"
              >
                AI 服务暂不可用，已使用预设模板生成内容
              </el-alert>
              
              <el-form :model="resultForm" label-width="80px">
                <el-form-item label="标题">
                  <el-input v-model="resultForm.title" />
                </el-form-item>
                
                <el-form-item label="内容">
                  <el-input
                    v-model="resultForm.content"
                    type="textarea"
                    :rows="12"
                  />
                </el-form-item>
                
                <el-form-item label="标签">
                  <el-select
                    v-model="resultForm.tags"
                    multiple
                    filterable
                    allow-create
                    placeholder="请选择或输入标签"
                  >
                    <el-option
                      v-for="tag in resultForm.tags"
                      :key="tag"
                      :label="tag"
                      :value="tag"
                    />
                  </el-select>
                </el-form-item>
                
                <el-form-item>
                  <el-button type="primary" @click="handleSave" :loading="saving">
                    保存到物料库
                  </el-button>
                  <el-button @click="handleCopy">复制内容</el-button>
                  <el-button @click="handleExport">导出</el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-card>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { House, User, Share, Connection, Coin, MagicStick, Files } from '@element-plus/icons-vue'
import api from '@/utils/api'
import { trackView, trackClick, trackGenerate, trackSave, trackExport } from '@/utils/analytics'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const generating = ref(false)
const saving = ref(false)
const generatedContent = ref(null)

const form = ref({
  keywords: '',
  platform: '',
  material_type: '',
  tone: 'friendly',
  length: 'medium'
})

const resultForm = ref({
  title: '',
  content: '',
  tags: []
})

onMounted(async () => {
  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }
  trackView('material_generator_page')
})

async function handleGenerate() {
  if (!form.value.keywords || !form.value.platform || !form.value.material_type) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  generating.value = true
  try {
    await trackClick('generate_material_button', { 
      platform: form.value.platform, 
      materialType: form.value.material_type,
      tone: form.value.tone,
      length: form.value.length
    })
    
    const res = await api.generateMaterial(form.value)
    generatedContent.value = res
    resultForm.value = {
      title: res.title,
      content: res.content,
      tags: res.tags
    }
    
    await trackGenerate(form.value.platform, form.value.material_type, {
      isFallback: res.is_fallback,
      keywords: form.value.keywords
    })
    
    ElMessage.success('生成成功')
  } catch (error) {
    ElMessage.error('生成失败，请稍后重试')
    console.error(error)
  } finally {
    generating.value = false
  }
}

async function handleSave() {
  if (!resultForm.value.title || !resultForm.value.content) {
    ElMessage.warning('标题和内容不能为空')
    return
  }
  
  saving.value = true
  try {
    const res = await api.createMaterial({
      ...resultForm.value,
      material_type: form.value.material_type,
      platform: form.value.platform,
      keywords: form.value.keywords
    })
    await trackSave(res.data?.id, { platform: form.value.platform, materialType: form.value.material_type })
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败，请稍后重试')
    console.error(error)
  } finally {
    saving.value = false
  }
}

async function handleCopy() {
  try {
    await navigator.clipboard.writeText(resultForm.value.content)
    ElMessage.success('已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
    console.error(error)
  }
}

async function handleExport() {
  const content = `# ${resultForm.value.title}\n\n${resultForm.value.content}\n\n标签：${resultForm.value.tags.join(', ')}`
  const blob = new Blob([content], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${resultForm.value.title}.txt`
  a.click()
  URL.revokeObjectURL(url)
  await trackExport('txt', { platform: form.value.platform, materialType: form.value.material_type })
  ElMessage.success('导出成功')
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {
  }
}
</script>

<style scoped>
.material-generator {
  width: 100%;
  height: 100%;
}

.container {
  height: 100%;
}

.header {
  background-color: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header h1 {
  font-size: 24px;
  font-weight: 500;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 5px;
}

.aside {
  background-color: #545c64;
}

.main {
  padding: 20px;
  background-color: #f5f7fa;
}

.generator-card {
  max-width: 900px;
}

.card-header {
  font-weight: 500;
  font-size: 16px;
}

.generator-form {
  max-width: 600px;
}

.result-section {
  margin-top: 20px;
}

.fallback-alert {
  margin-bottom: 20px;
}
</style>
