<template>
  <el-header class="header-container">
    <div class="left">
      <el-icon class="collapse-btn" @click="toggleSidebar">
        <Fold v-if="!appStore.sidebarCollapsed" />
        <Expand v-else />
      </el-icon>
    </div>
    <div class="right">
      <el-dropdown @command="handleCommand">
        <div class="user-info">
          <el-avatar :size="32" :src="userStore.avatar || ''">
            {{ userStore.username?.charAt(0).toUpperCase() }}
          </el-avatar>
          <span class="username">{{ userStore.username }}</span>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-header>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'
import { Fold, Expand, SwitchButton } from '@element-plus/icons-vue'
import { removeToken } from '@/utils/auth'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

const toggleSidebar = () => {
  appStore.toggleSidebar()
}

const handleCommand = async (command: string) => {
  if (command === 'logout') {
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
}
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  background-color: $bg-white;
  border-bottom: 1px solid $border-light;
  padding: 0 $spacing-lg;

  .left {
    display: flex;
    align-items: center;

    .collapse-btn {
      font-size: 20px;
      cursor: pointer;
      color: $text-regular;
      transition: color 0.3s;

      &:hover {
        color: $primary-color;
      }
    }
  }

  .right {
    .user-info {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      cursor: pointer;

      .username {
        color: $text-regular;
      }
    }
  }
}
</style>