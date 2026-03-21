<template>
  <div class="layout-container">
    <el-container>
      <el-header>
        <div class="header-left">
          <span class="logo">童趣衣橱POS</span>
        </div>
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <el-icon><User /></el-icon>
              {{ userStore.userInfo?.realName || '管理员' }}
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-container>
        <el-aside width="200px">
          <el-menu
            :default-active="activeMenu"
            router
            background-color="#304156"
            text-color="#bfcbd9"
            active-text-color="#409EFF"
          >
            <el-menu-item index="/layout/dashboard">
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.clearUser()
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.layout-container {
  width: 100%;
  height: 100%;
}

.el-header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header-left .logo {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.header-right .user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  color: #333;
}

.el-aside {
  background-color: #304156;
  height: calc(100vh - 60px);
  overflow-y: auto;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>