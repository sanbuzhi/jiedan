===FILE:kidswear-pos-backend/src/main/resources/mapper/CategoryMapper.xml===
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.kidswear.pos.mapper.CategoryMapper">
    <!-- 可在此添加Category的自定义SQL查询 -->
</mapper>
```
===FILE:kidswear-pos-backend/src/main/resources/mapper/UserMapper.xml===
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.kidswear.pos.mapper.UserMapper">
    <!-- 可在此添加User的自定义SQL查询，例如权限查询 -->
</mapper>
```
===FILE:kidswear-pos-frontend/src/styles/variables.scss===
```scss
// 主题色
$primary-color: #409eff;
$success-color: #67c23a;
$warning-color: #e6a23c;
$danger-color: #f56c6c;
$info-color: #909399;

// 字体色
$text-primary: #303133;
$text-regular: #606266;
$text-secondary: #909399;
$text-placeholder: #c0c4cc;

// 边框色
$border-color: #dcdfe6;
$border-light: #e4e7ed;
$border-lighter: #ebeef5;

// 背景色
$bg-color: #f5f7fa;
$bg-white: #ffffff;

// 间距
$spacing-xs: 4px;
$spacing-sm: 8px;
$spacing-md: 16px;
$spacing-lg: 24px;
$spacing-xl: 32px;

// 圆角
$border-radius-sm: 2px;
$border-radius-md: 4px;
$border-radius-lg: 8px;
$border-radius-circle: 50%;
```
===FILE:kidswear-pos-frontend/src/views/layout/components/Sidebar.vue===
```vue
<template>
  <el-container class="sidebar-container">
    <el-menu
      :default-active="activeMenu"
      :collapse="isCollapse"
      :unique-opened="true"
      router
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409eff"
    >
      <template v-for="item in menuList" :key="item.path">
        <el-menu-item v-if="!item.children" :index="item.path">
          <el-icon v-if="item.meta?.icon"><component :is="item.meta.icon" /></el-icon>
          <template #title>{{ item.meta?.title }}</template>
        </el-menu-item>
        <el-sub-menu v-else :index="item.path">
          <template #title>
            <el-icon v-if="item.meta?.icon"><component :is="item.meta.icon" /></el-icon>
            <span>{{ item.meta?.title }}</span>
          </template>
          <el-menu-item v-for="child in item.children" :key="child.path" :index="item.path + child.path">
            <el-icon v-if="child.meta?.icon"><component :is="child.meta.icon" /></el-icon>
            <template #title>{{ child.meta?.title }}</template>
          </el-menu-item>
        </el-sub-menu>
      </template>
    </el-menu>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'
import { Layout as LayoutIcon, Box, ShoppingCart } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

const isCollapse = computed(() => appStore.sidebarCollapsed)

const activeMenu = computed(() => route.path)

const menuList = computed(() => [
  {
    path: '/pos',
    meta: { title: '收银台', icon: ShoppingCart },
    children: [
      { path: '/checkout', meta: { title: '商品收银' } }
    ]
  },
  {
    path: '/product',
    meta: { title: '商品管理', icon: Box },
    children: [
      { path: '/sku-init', meta: { title: 'SKU初始化' } }
    ]
  },
  {
    path: '/system',
    meta: { title: '系统设置', icon: LayoutIcon },
    children: []
  }
])
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.sidebar-container {
  height: 100%;
  background-color: #304156;
  transition: width 0.3s;

  .el-menu {
    border-right: none;
  }
}
</style>
```
===FILE:kidswear-pos-frontend/src/views/layout/components/Header.vue===
```vue
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
```
===FILE:kidswear-pos-frontend/src/store/app.ts===
```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 侧边栏折叠状态
  const sidebarCollapsed = ref(false)

  // 切换侧边栏
  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  return {
    sidebarCollapsed,
    toggleSidebar
  }
})
```
===FILE:kidswear-pos-frontend/src/views/pos-layout/index.vue===
```vue
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
```
===FILE:kidswear-pos-frontend/src/views/pos/checkout/index.vue===
```vue
<template>
  <el-row :gutter="20" class="checkout-page">
    <el-col :span="16">
      <el-card class="product-card">
        <template #header>
          <div class="card-header">
            <span>商品列表</span>
          </div>
        </template>
        <el-empty description="收银台功能待完善" />
      </el-card>
    </el-col>
    <el-col :span="8">
      <el-card class="cart-card">
        <template #header>
          <div class="card-header">
            <span>购物车</span>
          </div>
        </template>
        <el-empty description="暂无商品" />
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
// 收银台待完善核心功能
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.checkout-page {
  height: 100%;

  .product-card, .cart-card {
    height: 100%;

    .card-header {
      font-weight: bold;
      font-size: 16px;
    }

    :deep(.el-card__body) {
      height: calc(100% - 57px);
      overflow-y: auto;
    }
  }
}
</style>
```