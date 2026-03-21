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