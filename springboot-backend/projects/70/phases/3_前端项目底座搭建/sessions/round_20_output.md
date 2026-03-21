===FILE:front/admin/package.json===
```json
{
  "name": "mall-admin",
  "private": true,
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview",
    "lint": "eslint . --ext .vue,.js,.jsx,.cjs,.mjs --fix --ignore-path .gitignore"
  },
  "dependencies": {
    "vue": "^3.3.11",
    "vue-router": "^4.2.5",
    "pinia": "^2.1.7",
    "pinia-plugin-persistedstate": "^3.2.0",
    "axios": "^1.6.2",
    "element-plus": "^2.3.14",
    "@element-plus/icons-vue": "^2.3.1",
    "dayjs": "^1.11.10",
    "lodash-es": "^4.17.21"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^4.5.2",
    "vite": "^5.0.8",
    "unplugin-auto-import": "^0.17.3",
    "unplugin-vue-components": "^0.26.0",
    "eslint": "^8.55.0",
    "eslint-plugin-vue": "^9.19.2",
    "sass": "^1.69.5"
  }
}
```

===FILE:front/admin/vite.config.js===
```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig(({ mode }) => {
  const isDev = mode === 'development'
  return {
    plugins: [
      vue(),
      AutoImport({
        resolvers: [ElementPlusResolver()],
        imports: ['vue', 'vue-router', 'pinia'],
        dts: 'src/auto-imports.d.ts',
        eslintrc: {
          enabled: true
        }
      }),
      Components({
        resolvers: [ElementPlusResolver()],
        dts: 'src/components.d.ts'
      })
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      port: 3001,
      open: true,
      proxy: {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, '')
        }
      }
    },
    build: {
      outDir: 'dist',
      assetsDir: 'assets',
      sourcemap: !isDev,
      minify: 'terser',
      terserOptions: {
        compress: {
          drop_console: !isDev,
          drop_debugger: !isDev
        }
      }
    }
  }
})
```

===FILE:front/admin/.env.development===
```env
VITE_API_BASE_URL=/api
VITE_APP_TITLE=后台管理系统
```

===FILE:front/admin/.env.production===
```env
VITE_API_BASE_URL=https://api.yourmall.com
VITE_APP_TITLE=后台管理系统
```

===FILE:front/admin/src/main.js===
```javascript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import './assets/base.css'
import './assets/main.css'
import './assets/index.css'

const app = createApp(App)
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus, {
  locale: zhCn
})

app.mount('#app')
```

===FILE:front/admin/src/router/index.js===
```javascript
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import LoginLayout from '@/layouts/LoginLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'

const routes = [
  {
    path: '/login',
    component: LoginLayout,
    meta: { requiresAuth: false, title: '登录' },
    children: [
      {
        path: '',
        name: 'Login',
        component: () => import('@/views/Login.vue')
      }
    ]
  },
  {
    path: '/',
    component: AdminLayout,
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '数据看板', icon: 'DataAnalysis' }
      },
      {
        path: 'products',
        name: 'Products',
        redirect: '/products/list',
        meta: { title: '商品管理', icon: 'Goods' },
        children: [
          {
            path: 'list',
            name: 'ProductList',
            component: () => import('@/views/ProductList.vue'),
            meta: { title: '商品列表' }
          },
          {
            path: 'add',
            name: 'ProductAdd',
            component: () => import('@/views/ProductDetail.vue'),
            meta: { title: '添加商品' }
          },
          {
            path: 'edit/:id',
            name: 'ProductEdit',
            component: () => import('@/views/ProductDetail.vue'),
            meta: { title: '编辑商品' }
          },
          {
            path: 'categories',
            name: 'ProductCategories',
            component: () => import('@/views/ProductCategoryList.vue'),
            meta: { title: '商品分类' }
          }
        ]
      },
      {
        path: 'orders',
        name: 'Orders',
        redirect: '/orders/list',
        meta: { title: '订单管理', icon: 'Tickets' },
        children: [
          {
            path: 'list',
            name: 'OrderList',
            component: () => import('@/views/OrderList.vue'),
            meta: { title: '订单列表' }
          },
          {
            path: 'detail/:id',
            name: 'OrderDetail',
            component: () => import('@/views/OrderDetail.vue'),
            meta: { title: '订单详情' }
          }
        ]
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/UserList.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/Settings.vue'),
        meta: { title: '系统设置', icon: 'Setting' }
      }
    ]
  },
  {
    path: '/403',
    name: '403',
    component: () => import('@/views/403.vue'),
    meta: { title: '无权限' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: '404',
    component: () => import('@/views/404.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  document.title = to.meta.title ? `${to.meta.title} - ${import.meta.env.VITE_APP_TITLE}` : import.meta.env.VITE_APP_TITLE

  if (to.meta.requiresAuth && !userStore.token) {
    next('/login')
  } else if (to.path === '/login' && userStore.token) {
    next('/')
  } else {
    next()
  }
})

export default router
```

===FILE:front/admin/src/stores/user.js===
```javascript
import { defineStore } from 'pinia'
import { login, logout, getUserInfo } from '@/api/user'
import { setToken, removeToken, getToken } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    userInfo: null
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.userInfo?.username || '',
    avatar: (state) => state.userInfo?.avatar || ''
  },
  actions: {
    async login(loginData) {
      try {
        const res = await login(loginData)
        this.token = res.data.token
        setToken(res.data.token)
        await this.getUserInfo()
        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },
    async getUserInfo() {
      try {
        const res = await getUserInfo()
        this.userInfo = res.data
        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },
    async logout() {
      try {
        await logout()
      } finally {
        this.token = ''
        this.userInfo = null
        removeToken()
      }
    }
  },
  persist: {
    key: 'admin-user',
    storage: localStorage,
    paths: ['token']
  }
})
```

===FILE:front/admin/src/stores/app.js===
```javascript
import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    sidebarCollapsed: false,
    theme: 'light',
    fixedHeader: true
  }),
  getters: {},
  actions: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },
    setTheme(theme) {
      this.theme = theme
    },
    setFixedHeader(fixed) {
      this.fixedHeader = fixed
    }
  },
  persist: {
    key: 'admin-app',
    storage: localStorage
  }
})
```

===FILE:front/admin/src/stores/index.js===
```javascript
export * from './user'
export * from './app'
export * from './product'
export * from './productCategory'
export * from './order'
export * from './dashboard'
```

===FILE:front/admin/src/utils/auth.js===
```javascript
const TOKEN_KEY = 'admin-token'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}
```

===FILE:front/admin/src/utils/request.js===
```javascript
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000
})

request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      switch (status) {
        case 401:
          ElMessage.error('登录已过期，请重新登录')
          const userStore = useUserStore()
          userStore.logout()
          router.push('/login')
          break
        case 403:
          router.push('/403')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(data.message || '请求失败')
      }
    } else if (error.request) {
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      ElMessage.error(error.message || '请求失败')
    }
    return Promise.reject(error)
  }
)

export default request
```

===FILE:front/admin/src/utils/constants.js===
```javascript
export const ORDER_STATUS = {
  PENDING_PAYMENT: { value: 0, label: '待付款', type: 'warning' },
  PENDING_SHIPMENT: { value: 1, label: '待发货', type: 'primary' },
  SHIPPED: { value: 2, label: '已发货', type: 'info' },
  COMPLETED: { value: 3, label: '已完成', type: 'success' },
  CANCELLED: { value: 4, label: '已取消', type: 'danger' },
  REFUNDED: { value: 5, label: '已退款', type: 'danger' }
}

export const ORDER_STATUS_LIST = Object.values(ORDER_STATUS)

export const PRODUCT_STATUS = {
  OFF_SHELF: { value: 0, label: '下架', type: 'danger' },
  ON_SHELF: { value: 1, label: '上架', type: 'success' }
}

export const PRODUCT_STATUS_LIST = Object.values(PRODUCT_STATUS)

export const GENDER = {
  UNKNOWN: { value: 0, label: '未知' },
  MALE: { value: 1, label: '男' },
  FEMALE: { value: 2, label: '女' }
}

export const GENDER_LIST = Object.values(GENDER)
```

===FILE:front/admin/src/utils/format.js===
```javascript
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'

dayjs.locale('zh-cn')

export function formatDate(date, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!date) return ''
  return dayjs(date).format(format)
}

export function formatPrice(price) {
  if (typeof price !== 'number' || isNaN(price)) return '¥0.00'
  return `¥${price.toFixed(2)}`
}

export function formatNumber(num, decimals = 0) {
  if (typeof num !== 'number' || isNaN(num)) return '0'
  return num.toFixed(decimals).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}
```

===FILE:front/admin/src/utils/index.js===
```javascript
export * from './auth'
export * from './request'
export * from './constants'
export * from './format'
export * from './validate'
```

===FILE:front/admin/src/api/index.js===
```javascript
export * from './user'
export * from './product'
export * from './productCategory'
export * from './order'
export * from './dashboard'
export * from './settings'
```

===FILE:front/admin/src/api/user.js===
```javascript
import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/admin/auth/login',
    method: 'post',
    data
  })
}

export function logout() {
  return request({
    url: '/admin/auth/logout',
    method: 'post'
  })
}

export function getUserInfo() {
  return request({
    url: '/admin/user/info',
    method: 'get'
  })
}

export function getUserList(params) {
  return request({
    url: '/admin/user/list',
    method: 'get',
    params
  })
}

export function updateUserStatus(id, status) {
  return request({
    url: `/admin/user/${id}/status`,
    method: 'put',
    data: { status }
  })
}

export function deleteUser(id) {
  return request({
    url: `/admin/user/${id}`,
    method: 'delete'
  })
}
```

===FILE:front/admin/src/api/productCategory.js===
```javascript
import request from '@/utils/request'

export function getCategoryTree() {
  return request({
    url: '/admin/category/tree',
    method: 'get'
  })
}

export function getCategoryList(params) {
  return request({
    url: '/admin/category/list',
    method: 'get',
    params
  })
}

export function getCategoryDetail(id) {
  return request({
    url: `/admin/category/${id}`,
    method: 'get'
  })
}

export function createCategory(data) {
  return request({
    url: '/admin/category',
    method: 'post',
    data
  })
}

export function updateCategory(id, data) {
  return request({
    url: `/admin/category/${id}`,
    method: 'put',
    data
  })
}

export function deleteCategory(id) {
  return request({
    url: `/admin/category/${id}`,
    method: 'delete'
  })
}
```

===FILE:front/admin/src/api/dashboard.js===
```javascript
import request from '@/utils/request'

export function getDashboardStats() {
  return request({
    url: '/admin/dashboard/stats',
    method: 'get'
  })
}

export function getDashboardChart(params) {
  return request({
    url: '/admin/dashboard/chart',
    method: 'get',
    params
  })
}

export function getDashboardRecentOrders() {
  return request({
    url: '/admin/dashboard/recent-orders',
    method: 'get'
  })
}

export function getDashboardTopProducts() {
  return request({
    url: '/admin/dashboard/top-products',
    method: 'get'
  })
}
```

===FILE:front/admin/src/api/settings.js===
```javascript
import request from '@/utils/request'

export function getSettings() {
  return request({
    url: '/admin/settings',
    method: 'get'
  })
}

export function updateSettings(data) {
  return request({
    url: '/admin/settings',
    method: 'put',
    data
  })
}

export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/admin/upload/image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
```

===FILE:front/admin/src/components/index.js===
```javascript
export { default as PageHeader } from './PageHeader.vue'
export { default as Pagination } from './Pagination.vue'
export { default as StatusTag } from './StatusTag.vue'
export { default as ConfirmDialog } from './ConfirmDialog.vue'
export { default as Empty } from './Empty.vue'
export { default as Uploader } from './Uploader.vue'
export { default as ProductForm } from './ProductForm.vue'
export { default as ProductFilter } from './ProductFilter.vue'
export { default as ProductSkuEditor } from './ProductSkuEditor.vue'
export { default as ProductSkuTable } from './ProductSkuTable.vue'
export { default as ProductSkuForm } from './ProductSkuForm.vue'
export { default as ProductCategoryForm } from './ProductCategoryForm.vue'
export { default as DashboardStats } from './DashboardStats.vue'
```

===FILE:front/admin/src/assets/base.css===
```css
*,
*::before,
*::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

html,
body,
#app {
  width: 100%;
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

a {
  text-decoration: none;
  color: inherit;
}

ul,
ol {
  list-style: none;
}

img {
  max-width: 100%;
  height: auto;
  display: block;
}

button {
  cursor: pointer;
  border: none;
  outline: none;
  background: none;
}
```

===FILE:front/admin/src/assets/main.css===
```css
/* Element Plus 覆盖样式 */
.el-header {
  padding: 0 !important;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.el-aside {
  background-color: #001529;
  transition: width 0.3s ease;
}

.el-menu {
  border-right: none !important;
}

.el-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.el-button--primary {
  background-color: #1890ff;
  border-color: #1890ff;
}

.el-button--primary:hover {
  background-color: #40a9ff;
  border-color: #40a9ff;
}

.el-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.el-table {
  border-radius: 8px;
  overflow: hidden;
}

.el-form-item__label {
  font-weight: 500;
}
```

===FILE:front/store/package.json===
```json
{
  "name": "mall-store",
  "private": true,
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview",
    "lint": "eslint . --ext .vue,.js,.jsx,.cjs,.mjs --fix --ignore-path .gitignore"
  },
  "dependencies": {
    "vue": "^3.3.11",
    "vue-router": "^4.2.5",
    "pinia": "^2.1.7",
    "pinia-plugin-persistedstate": "^3.2.0",
    "axios": "^1.6.2",
    "vant": "^4.8.0",
    "@vant/touch-emulator": "^1.4.0",
    "dayjs": "^1.11.10",
    "lodash-es": "^4.17.21",
    "swiper": "^11.0.5",
    "vue-awesome-swiper": "^5.0.1"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^4.5.2",
    "vite": "^5.0.8",
    "unplugin-auto-import": "^0.17.3",
    "unplugin-vue-components": "^0.26.0",
    {
      "resolvers": ["VantResolver"]
    },
    "eslint": "^8.55.0",
    "eslint-plugin-vue": "^9.19.2",
    "sass": "^1.69.5",
    "postcss-px-to-viewport": "^1.1.1"
  }
}
```

===FILE:front/store/vite.config.js===
```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { VantResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig(({ mode }) => {
  const isDev = mode === 'development'
  return {
    plugins: [
      vue(),
      AutoImport({
        resolvers: [VantResolver()],
        imports: ['vue', 'vue-router', 'pinia'],
        dts: 'src/auto-imports.d.ts',
        eslintrc: {
          enabled: true
        }
      }),
      Components({
        resolvers: [VantResolver()],
        dts: 'src/components.d.ts'
      })
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      port: 3000,
      open: true,
      proxy: {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, '')
        }
      }
    },
    build: {
      outDir: 'dist',
      assetsDir: 'assets',
      sourcemap: !isDev,
      minify: 'terser',
      terserOptions: {
        compress: {
          drop_console: !isDev,
          drop_debugger: !isDev
        }
      }
    },
    css: {
      postcss: {
        plugins: [
          require('postcss-px-to-viewport')({
            viewportWidth: 375,
            viewportHeight: 667,
            unitPrecision: 5,
            viewportUnit: 'vw',
            selectorBlackList: ['.ignore', '.hairlines'],
            minPixelValue: 1,
            mediaQuery: false
          })
        ]
      }
    }
  }
})
```

===FILE:front/store/.env.development===
```env
VITE_API_BASE_URL=/api
VITE_APP_TITLE=商城
```

===FILE:front/store/.env.production===
```env
VITE_API_BASE_URL=https://api.yourmall.com
VITE_APP_TITLE=商城
```

===FILE:front/store/src/main.js===
```javascript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import Vant from 'vant'
import 'vant/lib/index.css'
import '@vant/touch-emulator'

import App from './App.vue'
import router from './router'
import './assets/base.css'
import './assets/main.css'
import './assets/index.css'

const app = createApp(App)
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

app.use(pinia)
app.use(router)
app.use(Vant)

app.mount('#app')
```

===FILE:front/store/src/router/index.js===
```javascript
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import StoreLayout from '@/layouts/StoreLayout.vue'

const routes = [
  {
    path: '/',
    component: StoreLayout,
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页', showTabBar: true }
      },
      {
        path: 'products',
        name: 'ProductList',
        component: () => import('@/views/ProductList.vue'),
        meta: { title: '商品列表', showTabBar: false }
      },
      {
        path: 'product/:id',
        name: 'ProductDetail',
        component: () => import('@/views/ProductDetail.vue'),
        meta: { title: '商品详情', showTabBar: false }
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/Cart.vue'),
        meta: { title: '购物车', showTabBar: true, requiresAuth: true }
      },
      {
        path: 'checkout',
        name: 'Checkout',
        component: () => import('@/views/Checkout.vue'),
        meta: { title: '结算', showTabBar: false, requiresAuth: true }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/User.vue'),
        meta: { title: '我的', showTabBar: true }
      },
      {
        path: 'address',
        name: 'AddressList',
        component: () => import('@/views/AddressList.vue'),
        meta: { title: '收货地址', showTabBar: false, requiresAuth: true }
      },
      {
        path: 'orders',
        name: 'OrderList',
        component: () => import('@/views/OrderList.vue'),
        meta: { title: '我的订单', showTabBar: false, requiresAuth: true }
      },
      {
        path: 'order/:id',
        name: 'OrderDetail',
        component: () => import('@/views/OrderDetail.vue'),
        meta: { title: '订单详情', showTabBar: false, requiresAuth: true }
      },
      {
        path: 'profile/edit',
        name: 'UserProfileEdit',
        component: () => import('@/views/UserProfileEdit.vue'),
        meta: { title: '编辑资料', showTabBar: false, requiresAuth: true }
      },
      {
        path: 'password/change',
        name: 'PasswordChange',
        component: () => import('@/views/PasswordChange.vue'),
        meta: { title: '修改密码', showTabBar: false, requiresAuth: true }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', showTabBar: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', showTabBar: false }
  },
  {
    path: '/password/reset',
    name: 'PasswordReset',
    component: () => import('@/views/PasswordReset.vue'),
    meta: { title: '重置密码', showTabBar: false }
  },
  {
    path: '/:pathMatch(.*)*',
    name: '404',
    component: () => import('@/views/404.vue'),
    meta: { title: '页面不存在', showTabBar: false }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  document.title = to.meta.title ? `${to.meta.title} - ${import.meta.env.VITE_APP_TITLE}` : import.meta.env.VITE_APP_TITLE

  if (to.meta.requiresAuth && !userStore.token) {
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
  } else if ((to.path === '/login' || to.path === '/register') && userStore.token) {
    next('/')
  } else {
    next()
  }
})

export default router
```

===FILE:front/store/src/stores/user.js===
```javascript
import { defineStore } from 'pinia'
import { login, register, logout, getUserInfo, updateUserInfo, changePassword } from '@/api/user'
import { setToken, removeToken, getToken } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    userInfo: null
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.userInfo?.username || '',
    nickname: (state) => state.userInfo?.nickname || '',
    avatar: (state) => state.userInfo?.avatar || '',
    phone: (state) => state.userInfo?.phone || ''
  },
  actions: {
    async login(loginData) {
      try {
        const res = await login(loginData)
        this.token = res.data.token
        setToken(res.data.token)
        await this.getUserInfo()
        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },
    async register(registerData) {
      try {
        const res = await register(registerData)
        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },
    async getUserInfo() {
      try {
        const res = await getUserInfo()
        this.userInfo = res.data
        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },
    async updateUserInfo(data) {
      try {
        const res = await updateUserInfo(data)
        this.userInfo = { ...this.userInfo, ...data }
        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },
    async changePassword(data) {
      try {
        const res = await changePassword(data)
        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },
    async logout() {
      try {
        await logout()
      } finally {
        this.token = ''
        this.userInfo = null
        removeToken()
      }
    }
  },
  persist: {
    key: 'store-user',
    storage: localStorage,
    paths: ['token']
  }
})
```

===FILE:front/store/src/stores/cart.js===
```javascript
import { defineStore } from 'pinia'
import { getCartList, addCartItem, updateCartItem, deleteCartItem, selectCartItems, clearCart } from '@/api/product'

export const useCartStore = defineStore('cart', {
  state: () => ({
    cartList: [],
    loading: false
  }),
  getters: {
    cartCount: (state) => state.cartList.reduce((sum, item) => sum + item.quantity, 0),
    selectedItems: (state) => state.cartList.filter(item => item.selected),
    selectedCount: (state) => state.selectedItems.reduce((sum, item) => sum + item.quantity, 0),
    totalPrice: (state) => state.selectedItems.reduce((sum, item) => sum + item.price * item.quantity, 0)
  },
  actions: {
    async getCartList() {
      this.loading = true
      try {
        const res = await getCartList()
        this.cartList = res.data.map(item => ({ ...item, selected: true }))
        return res
      } catch (error) {
        return Promise.reject(error)
      } finally {
        this.loading = false
      }
    },
    async addCartItem(data) {
      try {
        const res = await addCartItem(data)
        await this.getCartList()
        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },
    async updateCartItem(id, quantity) {
      try {
        const res = await updateCartItem(id, quantity)
        const index = this.cartList.findIndex(item => item.id === id)
        if (index !== -1) {
          this.cartList[index].quantity = quantity
        }
        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },
    async deleteCartItem(ids) {
      try {
        const res = await deleteCartItem(ids)
        this.cartList = this.cartList.filter(item => !ids.includes(item.id))
        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },
    async selectCartItems(ids, selected) {
      try {
        const res = await selectCartItems(ids, selected)
        this.cartList.forEach(item => {
          if (ids.includes(item.id)) {
            item.selected = selected
          }
        })
        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },
    async selectAll(selected) {
      const ids = this.cartList.map(item => item.id)
      await this.selectCartItems(ids, selected)
    },
    async clearSelected() {
      const ids = this.selectedItems.map(item => item.id)
      await this.deleteCartItem(ids)
    }
  },
  persist: {
    key: 'store-cart',
    storage: localStorage
  }
})
```

===FILE:front/store/src/stores/app.js===
```javascript
import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    showTabBar: true,
    backTopVisible: false
  }),
  getters: {},
  actions: {
    setShowTabBar(show) {
      this.showTabBar = show
    },
    setBackTopVisible(visible) {
      this.backTopVisible = visible
    }
  },
  persist: {
    key: 'store-app',
    storage: localStorage
  }
})
```

===FILE:front/store/src/stores/index.js===
```javascript
export * from './user'
export * from './cart'
export * from './address'
export * from './product'
export * from './home'
export * from './order'
export * from './app'
```

===FILE:front/store/src/utils/auth.js===
```javascript
const TOKEN_KEY = 'store-token'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}
```

===FILE:front/store/src/utils/request.js===
```javascript
import axios from 'axios'
import { showToast, showConfirmDialog } from 'vant'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000
})

request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      showToast(res.message || '请求失败')
      if (res.code === 401) {
        showConfirmDialog({
          title: '提示',
          message: '登录已过期，请重新登录'
        }).then(() => {
          const userStore = useUserStore()
          userStore.logout()
          router.push('/login')
        }).catch(() => {})
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      switch (status) {
        case 401:
          showConfirmDialog({
            title: '提示',
            message: '登录已过期，请重新登录'
          }).then(() => {
            const userStore = useUserStore()
            userStore.logout()
            router.push('/login')
          }).catch(() => {})
          break
        case 404:
          showToast('请求的资源不存在')
          break
        case 500:
          showToast('服务器错误')
          break
        default:
          showToast(data.message || '请求失败')
      }
    } else if (error.request) {
      showToast('网络错误，请检查网络连接')
    } else {
      showToast(error.message || '请求失败')
    }
    return Promise.reject(error)
  }
)

export default request
```

===FILE:front/store/src/utils/constants.js===
```javascript
export const ORDER_STATUS = {
  PENDING_PAYMENT: { value: 0, label: '待付款', type: 'warning' },
  PENDING_SHIPMENT: { value: 1, label: '待发货', type: 'primary' },
  SHIPPED: { value: 2, label: '已发货', type: 'info' },
  COMPLETED: { value: 3, label: '已完成', type: 'success' },
  CANCELLED: { value: 4, label: '已取消', type: 'danger' },
  REFUNDED: { value: 5, label: '已退款', type: 'danger' }
}

export const ORDER_STATUS_LIST = Object.values(ORDER_STATUS)

export const TAB_BAR_LIST = [
  { path: '/home', name: '首页', icon: 'home-o' },
  { path: '/products', name: '分类', icon: 'apps-o' },
  { path: '/cart', name: '购物车', icon: 'shopping-cart-o', badge: true },
  { path: '/user', name: '我的', icon: 'user-o' }
]
```

===FILE:front/store/src/utils/format.js===
```javascript
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import relativeTime from 'dayjs/plugin/relativeTime'

dayjs.locale('zh-cn')
dayjs.extend(relativeTime)

export function formatDate(date, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!date) return ''
  return dayjs(date).format(format)
}

export function formatRelativeTime(date) {
  if (!date) return ''
  return dayjs(date).fromNow()
}

export function formatPrice(price) {
  if (typeof price !== 'number' || isNaN(price)) return '¥0.00'
  return `¥${price.toFixed(2)}`
}

export function formatNumber(num, decimals = 0) {
  if (typeof num !== 'number' || isNaN(num)) return '0'
  return num.toFixed(decimals).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

export function formatPhone(phone) {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}
```

===FILE:front/store/src/utils/index.js===
```javascript
export * from './auth'
export * from './request'
export * from './constants'
export * from './format'
export * from './validate'
```

===FILE:front/store/src/api/index.js===
```javascript
export * from './user'
export * from './product'
export * from './order'
export * from './address'
export * from './home'
export * from './passwordReset'
```

===FILE:front/store/src/api/user.js===
```javascript
import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/user/auth/login',
    method: 'post',
    data
  })
}

export function register(data) {
  return request({
    url: '/user/auth/register',
    method: 'post',
    data
  })
}

export function logout() {
  return request({
    url: '/user/auth/logout',
    method: 'post'
  })
}

export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

export function updateUserInfo(data) {
  return request({
    url: '/user/info',
    method: 'put',
    data
  })
}

export function changePassword(data) {
  return request({
    url: '/user/password',
    method: 'put',
    data
  })
}
```

===FILE:front/store/src/api/passwordReset.js===
```javascript
import request from '@/utils/request'

export function sendResetCode(phone) {
  return request({
    url: '/user/password-reset/send-code',
    method: 'post',
    data: { phone }
  })
}

export function resetPassword(data) {
  return request({
    url: '/user/password-reset',
    method: 'post',
    data
  })
}
```

===FILE:front/store/src/components/index.js===
```javascript
export { default as ProductCard } from './ProductCard.vue'
export { default as ProductSkuSelector } from './ProductSkuSelector.vue'
export { default as PriceDisplay } from './PriceDisplay.vue'
export { default as Empty } from './Empty.vue'
export { default as AddressForm } from './AddressForm.vue'
export { default as OrderStatusTag } from './OrderStatusTag.vue'
export { default as OrderSteps } from './OrderSteps.vue'
export { default as BackTop } from './BackTop.vue'
export { default as HomeSwiper } from './HomeSwiper.vue'
export { default as HomeSearch } from './HomeSearch.vue'
export { default as ProductListSearch } from './ProductListSearch.vue'
export { default as CartItemQuantitySelector } from './CartItemQuantitySelector.vue'
```

===FILE:front/store/src/assets/base.css===
```css
*,
*::before,
*::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

html,
body,
#app {
  width: 100%;
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'Noto Sans', sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji';
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background-color: #f7f8fa;
}

a {
  text-decoration: none;
  color: inherit;
  -webkit-tap-highlight-color: transparent;
}

ul,
ol {
  list-style: none;
}

img {
  max-width: 100%;
  height: auto;
  display: block;
  vertical-align: middle;
}

button,
input,
textarea {
  font-family: inherit;
  font-size: inherit;
  outline: none;
  border: none;
  background: none;
}

button {
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
}
```

===FILE:front/store/src/assets/main.css===
```css
/* Vant 覆盖样式 */
:root {
  --van-primary-color: #1989fa;
  --van-success-color: #07c160;
  --van-danger-color: #ee0a24;
  --van-warning-color: #ff976a;
  --van-background-color-light: #f7f8fa;
  --van-text-color: #323233;
  --van-text-color-secondary: #969799;
}

.van-nav-bar {
  background-color: #fff;
}

.van-tabbar {
  background-color: #fff;
  box-shadow: 0 -1px 4px rgba(0, 0, 0, 0.05);
}

.van-tabbar-item--active {
  color: var(--van-primary-color);
}

.van-card {
  background-color: #fff;
  border-radius: 8px;
  margin-bottom: 12px;
}

.van-button--primary {
  background: linear-gradient(90deg, #1989fa, #07c160);
  border: none;
}

.van-field {
  background-color: #fff;
}

.van-cell {
  background-color: #fff;
}

.van-swipe {
  border-radius: 8px;
  overflow: hidden;
}

.van-tag {
  border-radius: 4px;
}
```