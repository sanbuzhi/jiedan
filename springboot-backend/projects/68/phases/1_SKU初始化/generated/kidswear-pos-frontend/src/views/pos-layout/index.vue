<template>
  <el-container class="pos-layout">
    <el-header class="pos-header">
      <div class="logo">
        <span>童装POS收银</span>
      </div>
      <div class="user-area">
        <el-button type="text" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回管理后台
        </el-button>
        <span class="username">{{ userStore.username }}</span>
        <el-button type="danger" size="small" @click="handleLogout">
          退出
        </el-button>
      </div>
    </el-header>
    <el-main class="pos-main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { removeToken } from '@/utils/auth'
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const goBack = () => {
  router.push('/')
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    removeToken()
    userStore.resetUser()
    ElMessage.success('退出登录成功')
    router.push('/login')
  } catch {
    // 用户取消
  }
}
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.pos-layout {
  height: 100vh;
  background-color: $bg-color;

  .pos-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 60px;
    background-color: $primary-color;
    color: $bg-white;
    padding: 0 $spacing-lg;

    .logo {
      font-size: 20px;
      font-weight: bold;
    }

    .user-area {
      display: flex;
      align-items: center;
      gap: $spacing-md;

      .username {
        font-size: 14px;
      }

      :deep(.el-button) {
        color: $bg-white;
        border-color: $bg-white;
      }
    }
  }

  .pos-main {
    padding: $spacing-lg;
    overflow-y: auto;
  }
}
</style>