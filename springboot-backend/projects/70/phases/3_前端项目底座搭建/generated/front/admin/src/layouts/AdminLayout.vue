<template>
  <el-container class="admin-layout">
    <el-aside width="200px">
      <div class="logo">电商后台</div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <template v-for="route in menuRoutes" :key="route.path">
          <el-menu-item :index="route.path">
            <el-icon><component :is="route.meta.icon" /></el-icon>
            <span>{{ route.meta.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ name: 'Dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute.meta.title">{{ currentRoute.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo.avatar || ''">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="username">{{ userStore.userInfo.username || '管理员' }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Fold, Expand, User } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)

const menuRoutes = computed(() => {
  return router.options.routes.find(r => r.path === '/')?.children.filter(r => r.meta?.icon) || []
})

const activeMenu = computed(() => route.path)
const currentRoute = computed(() => route)

function toggleCollapse() {
  isCollapse.value = !isCollapse.value
}

async function handleCommand(command) {
  if (command === 'logout') {
    await userStore.logout()
    router.push({ name: 'Login' })
  }
}
</script>

<style scoped>
.admin-layout {
  width: 100%;
  height: 100%;
}

.el-aside {
  background-color: #304156;
  overflow-x: hidden;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  color: white;
  background-color: #2b3a4a;
}

.el-header {
  background-color: white;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.username {
  font-size: 14px;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>