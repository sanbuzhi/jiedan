<template>
  <div class="home">
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
            v-if="userStore.userInfo"
            :default-active="activeMenu"
            background-color="#545c64"
            text-color="#fff"
            active-text-color="#ffd04b"
            @select="handleMenuSelect"
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
          <el-card class="welcome-card">
            <template #header>
              <div class="card-header">
                <span>欢迎使用智能获客系统</span>
              </div>
            </template>
            <div class="welcome-content">
              <el-statistic title="当前积分" :value="userStore.userInfo?.total_points || 0">
                <template #suffix>分</template>
              </el-statistic>
              <div class="quick-actions">
                <el-button type="primary" @click="$router.push('/referral')">分享推荐码</el-button>
                <el-button @click="$router.push('/points')">查看积分记录</el-button>
              </div>
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
import { trackView, trackLogout } from '@/utils/analytics'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

onMounted(async () => {
  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }
  try {
    await api.initializeRules()
  } catch (error) {
    console.error('初始化规则失败:', error)
  }
  trackView('home_page', { totalPoints: userStore.userInfo?.total_points || 0 })
})

function handleMenuSelect(index) {
  router.push(index)
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await trackLogout()
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {
  }
}
</script>

<style scoped>
.home {
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

.welcome-card {
  max-width: 800px;
}

.card-header {
  font-weight: 500;
  font-size: 16px;
}

.welcome-content {
  text-align: center;
  padding: 20px 0;
}

.quick-actions {
  margin-top: 30px;
  display: flex;
  gap: 15px;
  justify-content: center;
}
</style>
