<template>
  <el-container class="admin-layout">
    <el-aside width="240px">
      <div class="logo">
        <h2>智能获客系统</h2>
        <p>管理后台</p>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/admin/requirements">
          <el-icon><List /></el-icon>
          <span>需求管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/orders">
          <el-icon><DocumentChecked /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/ai-strategy">
          <el-icon><Cpu /></el-icon>
          <span>AI策略</span>
        </el-menu-item>
        <el-menu-item index="/admin/materials">
          <el-icon><Guide /></el-icon>
          <span>A/B测试</span>
        </el-menu-item>
        <el-menu-item index="/admin/optimization">
          <el-icon><Setting /></el-icon>
          <span>策略优化</span>
        </el-menu-item>
        <el-menu-item index="/admin/config">
          <el-icon><Tools /></el-icon>
          <span>系统配置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-content">
          <span class="breadcrumb">管理后台</span>
          <div class="header-right">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-icon><UserFilled /></el-icon>
                管理员
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  DataLine,
  User,
  ShoppingCart,
  Document,
  Promotion,
  Guide,
  Setting,
  Tools,
  UserFilled,
  ArrowDown,
  List,
  DocumentChecked,
  Cpu
} from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => route.path)

const handleCommand = (command) => {
  if (command === 'logout') {
    window.location.href = '/login'
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.el-aside {
  background-color: #304156;
  overflow-x: hidden;
}

.logo {
  height: 120px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: #263445;
  color: #fff;
}

.logo h2 {
  font-size: 18px;
  margin-bottom: 5px;
}

.logo p {
  font-size: 12px;
  color: #909399;
}

.el-header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.breadcrumb {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #606266;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
