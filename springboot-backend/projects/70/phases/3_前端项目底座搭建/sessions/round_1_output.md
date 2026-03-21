===FILE:front/admin/package.json===
```json
{
  "name": "tongquyouyi-admin",
  "version": "1.0.0",
  "private": true,
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "3.3.11",
    "vue-router": "4.2.5",
    "pinia": "2.1.7",
    "pinia-plugin-persistedstate": "3.2.1",
    "axios": "1.6.2",
    "element-plus": "2.3.14",
    "@element-plus/icons-vue": "2.3.1",
    "vue-cropper": "1.1.1"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "5.0.2",
    "vite": "5.0.10",
    "unplugin-auto-import": "0.17.3",
    "unplugin-vue-components": "0.26.0",
    "sass": "1.69.5"
  }
}
```

===FILE:front/admin/vite.config.js===
```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: ['vue', 'vue-router', 'pinia'],
      dts: 'src/auto-imports.d.ts'
    }),
    Components({
      resolvers: [ElementPlusResolver()],
      dts: 'src/components.d.ts'
    })
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path
      }
    }
  }
})
```

===FILE:front/admin/.env.development===
```env
VITE_APP_TITLE=童趣优衣管理后台
VITE_API_BASE_URL=/api
```

===FILE:front/admin/.env.production===
```env
VITE_APP_TITLE=童趣优衣管理后台
VITE_API_BASE_URL=https://api.tongquyouyi.com
```

===FILE:front/admin/src/main.js===
```javascript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus)
app.mount('#app')
```

===FILE:front/admin/src/App.vue===
```vue
<template>
  <router-view />
</template>

<script setup>
</script>

<style lang="scss">
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  width: 100%;
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}
</style>
```

===FILE:front/admin/src/router/index.js===
```javascript
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false, title: '登录' }
  },
  {
    path: '/',
    name: 'AdminLayout',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '系统首页' }
      }
    ]
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/404.vue'),
    meta: { requiresAuth: false, title: '404' }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title || '页面'} - ${import.meta.env.VITE_APP_TITLE}`
  const userStore = useUserStore()
  const token = userStore.token

  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
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

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    refreshToken: '',
    userInfo: null
  }),
  getters: {
    isLoggedIn: (state) => !!state.token
  },
  actions: {
    setToken(data) {
      this.token = data.token
      this.refreshToken = data.refreshToken
    },
    setUserInfo(info) {
      this.userInfo = info
    },
    logout() {
      this.token = ''
      this.refreshToken = ''
      this.userInfo = null
    }
  },
  persist: {
    key: 'tongquyouyi-admin-user',
    storage: localStorage
  }
})
```

===FILE:front/admin/src/stores/store.js===
```javascript
import { defineStore } from 'pinia'

export const useStoreStore = defineStore('store', {
  state: () => ({
    storeInfo: null
  }),
  actions: {
    setStoreInfo(info) {
      this.storeInfo = info
    }
  },
  persist: {
    key: 'tongquyouyi-admin-store',
    storage: localStorage
  }
})
```

===FILE:front/admin/src/utils/auth.js===
```javascript
import { useUserStore } from '@/stores/user'

export const getToken = () => {
  const userStore = useUserStore()
  return userStore.token
}

export const setToken = (data) => {
  const userStore = useUserStore()
  userStore.setToken(data)
}

export const removeToken = () => {
  const userStore = useUserStore()
  userStore.logout()
}
```

===FILE:front/admin/src/utils/request.js===
```javascript
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from './auth'
import router from '@/router'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000
})

request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
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
      ElMessage.error(res.msg || '请求失败')
      if (res.code === 401) {
        removeToken()
        router.push('/login')
      }
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  (error) => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
```

===FILE:front/admin/src/api/index.js===
```javascript
import request from '@/utils/request'

export const login = (data) => {
  return request({
    url: '/admin/auth/login',
    method: 'post',
    data
  })
}

export const getCaptcha = () => {
  return request({
    url: '/admin/auth/captcha',
    method: 'get'
  })
}

export const logout = () => {
  return request({
    url: '/admin/auth/logout',
    method: 'post'
  })
}

export const getProfile = () => {
  return request({
    url: '/admin/system/profile/get',
    method: 'get'
  })
}

export const getStoreInfo = () => {
  return request({
    url: '/admin/system/store/get',
    method: 'get'
  })
}
```

===FILE:front/admin/src/layouts/LoginLayout.vue===
```vue
<template>
  <div class="login-layout">
    <div class="login-container">
      <slot />
    </div>
  </div>
</template>

<script setup>
</script>

<style lang="scss" scoped>
.login-layout {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;

  .login-container {
    width: 400px;
    padding: 40px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  }
}
</style>
```

===FILE:front/admin/src/layouts/AdminLayout.vue===
```vue
<template>
  <el-container class="admin-layout">
    <el-aside width="240px">
      <div class="logo">
        <h2>{{ import.meta.env.VITE_APP_TITLE }}</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><House /></el-icon>
          <span>系统首页</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-left">
          <span>欢迎，{{ userStore.userInfo?.nickname || '管理员' }}</span>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar" />
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
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
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import { logout as logoutApi } from '@/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await logoutApi()
      userStore.logout()
      router.push('/login')
    } catch (error) {
      console.log('取消退出')
    }
  } else if (command === 'profile') {
    // 后续跳转到个人信息页
    ElMessage.info('功能开发中')
  }
}
</script>

<style lang="scss" scoped>
.admin-layout {
  width: 100%;
  height: 100%;

  .el-aside {
    background-color: #304156;
    overflow-x: hidden;

    .logo {
      height: 60px;
      line-height: 60px;
      text-align: center;
      color: #fff;
      font-size: 18px;
      font-weight: bold;
      border-bottom: 1px solid #1f2d3d;
    }
  }

  .el-header {
    background-color: #fff;
    border-bottom: 1px solid #e6e6e6;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-left {
      font-size: 14px;
      color: #333;
    }

    .header-right {
      .el-dropdown-link {
        display: flex;
        align-items: center;
        cursor: pointer;
        gap: 8px;
      }
    }
  }

  .el-main {
    background-color: #f0f2f5;
    padding: 20px;
  }
}
</style>
```

===FILE:front/admin/src/views/Login.vue===
```vue
<template>
  <LoginLayout>
    <div class="login-header">
      <h1>童趣优衣</h1>
      <p>童装零售单店管理后台</p>
    </div>
    <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="0">
      <el-form-item prop="username">
        <el-input
          v-model="loginForm.username"
          placeholder="请输入账号"
          prefix-icon="User"
          size="large"
        />
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="loginForm.password"
          type="password"
          placeholder="请输入密码"
          prefix-icon="Lock"
          size="large"
          show-password
          @keyup.enter="handleLogin"
        />
      </el-form-item>
      <el-form-item prop="captcha">
        <div class="captcha-container">
          <el-input
            v-model="loginForm.captcha"
            placeholder="请输入验证码"
            prefix-icon="Key"
            size="large"
          />
          <div class="captcha-img" @click="refreshCaptcha">
            <img v-if="captchaImg" :src="captchaImg" alt="验证码" />
            <span v-else>点击刷新</span>
          </div>
        </div>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="handleLogin">
          登录
        </el-button>
      </el-form-item>
    </el-form>
  </LoginLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import LoginLayout from '@/layouts/LoginLayout.vue'
import { useUserStore, useStoreStore } from '@/stores'
import { login, getCaptcha, getProfile, getStoreInfo } from '@/api'

const router = useRouter()
const userStore = useUserStore()
const storeStore = useStoreStore()

const loginFormRef = ref(null)
const loading = ref(false)
const captchaImg = ref('')

const loginForm = reactive({
  username: '',
  password: '',
  captcha: '',
  captchaKey: ''
})

const rules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 6, max: 20, message: '账号长度为6-20位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

const refreshCaptcha = async () => {
  try {
    const res = await getCaptcha()
    loginForm.captchaKey = res.data.captchaKey
    captchaImg.value = res.data.captchaImg
  } catch (error) {
    console.error('获取验证码失败', error)
  }
}

const handleLogin = async () => {
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login(loginForm)
        userStore.setToken(res.data)
        const [profileRes, storeRes] = await Promise.all([getProfile(), getStoreInfo()])
        userStore.setUserInfo(profileRes.data)
        storeStore.setStoreInfo(storeRes.data)
        ElMessage.success('登录成功')
        router.push('/')
      } catch (error) {
        refreshCaptcha()
        console.error('登录失败', error)
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  refreshCaptcha()
})
</script>

<style lang="scss" scoped>
.login-header {
  text-align: center;
  margin-bottom: 30px;

  h1 {
    font-size: 28px;
    color: #333;
    margin-bottom: 8px;
  }

  p {
    font-size: 14px;
    color: #999;
  }
}

.captcha-container {
  display: flex;
  gap: 12px;

  .captcha-img {
    width: 120px;
    height: 40px;
    background-color: #f5f7fa;
    border-radius: 4px;
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    span {
      font-size: 12px;
      color: #999;
    }
  }
}
</style>
```

===FILE:front/admin/src/views/Dashboard.vue===
```vue
<template>
  <div class="dashboard">
    <el-card>
      <h2>欢迎使用童趣优衣童装零售单店管理后台</h2>
      <p>当前阶段：前端项目底座搭建</p>
    </el-card>
  </div>
</template>

<script setup>
</script>

<style lang="scss" scoped>
.dashboard {
  h2 {
    margin-bottom: 12px;
    color: #333;
  }

  p {
    color: #666;
  }
}
</style>
```

===FILE:front/admin/src/views/404.vue===
```vue
<template>
  <div class="not-found">
    <h1>404</h1>
    <p>页面不存在</p>
    <el-button type="primary" @click="router.push('/')">返回首页</el-button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style lang="scss" scoped>
.not-found {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;

  h1 {
    font-size: 120px;
    color: #409EFF;
    margin-bottom: 20px;
  }

  p {
    font-size: 24px;
    color: #666;
    margin-bottom: 40px;
  }
}
</style>
```

===FILE:front/admin/src/stores/index.js===
```javascript
export * from './user'
export * from './store'
```

===FILE:front/store/package.json===
```json
{
  "name": "tongquyouyi-store",
  "version": "1.0.0",
  "private": true,
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "3.3.11",
    "vue-router": "4.2.5",
    "pinia": "2.1.7",
    "pinia-plugin-persistedstate": "3.2.1",
    "axios": "1.6.2",
    "vant": "4.8.0",
    "@vant/touch-emulator": "1.4.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "5.0.2",
    "vite": "5.0.10",
    "unplugin-auto-import": "0.17.3",
    "unplugin-vue-components": "0.26.0",
    {
      "vant": {
        "resolver": "unplugin-vue-components/resolvers"
      }
    },
    "sass": "1.69.5"
  }
}
```

===FILE:front/store/vite.config.js===
```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { VantResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [VantResolver()],
      imports: ['vue', 'vue-router', 'pinia'],
      dts: 'src/auto-imports.d.ts'
    }),
    Components({
      resolvers: [VantResolver()],
      dts: 'src/components.d.ts'
    })
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3001,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path
      }
    }
  }
})
```

===FILE:front/store/.env.development===
```env
VITE_APP_TITLE=童趣优衣
VITE_API_BASE_URL=/api
```

===FILE:front/store/.env.production===
```env
VITE_APP_TITLE=童趣优衣
VITE_API_BASE_URL=https://api.tongquyouyi.com
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

const app = createApp(App)
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

app.use(pinia)
app.use(router)
app.use(Vant)
app.mount('#app')
```

===FILE:front/store/src/App.vue===
```vue
<template>
  <router-view />
</template>

<script setup>
</script>

<style lang="scss">
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  width: 100%;
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #f5f5f5;
}
</style>
```

===FILE:front/store/src/router/index.js===
```javascript
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false, title: '登录' }
  },
  {
    path: '/',
    name: 'StoreLayout',
    component: () => import('@/layouts/StoreLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { requiresAuth: false, title: '首页' }
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/Cart.vue'),
        meta: { requiresAuth: true, title: '购物车' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/User.vue'),
        meta: { requiresAuth: true, title: '我的' }
      }
    ]
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/404.vue'),
    meta: { requiresAuth: false, title: '404' }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title || '页面'} - ${import.meta.env.VITE_APP_TITLE}`
  const userStore = useUserStore()
  const token = userStore.token

  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
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

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    refreshToken: '',
    userInfo: null
  }),
  getters: {
    isLoggedIn: (state) => !!state.token
  },
  actions: {
    setToken(data) {
      this.token = data.token
      this.refreshToken = data.refreshToken
    },
    setUserInfo(info) {
      this.userInfo = info
    },
    logout() {
      this.token = ''
      this.refreshToken = ''
      this.userInfo = null
    }
  },
  persist: {
    key: 'tongquyouyi-store-user',
    storage: localStorage
  }
})
```

===FILE:front/store/src/stores/cart.js===
```javascript
import { defineStore } from 'pinia'

export const useCartStore = defineStore('cart', {
  state: () => ({
    cartList: []
  }),
  getters: {
    selectedCartList: (state) => state.cartList.filter(item => item.isSelected),
    totalPrice: (state) => state.selectedCartList.reduce((sum, item) => sum + item.salePrice * item.quantity, 0),
    totalCount: (state) => state.selectedCartList.reduce((sum, item) => sum + item.quantity, 0)
  },
  actions: {
    addToCart(product) {
      const index = this.cartList.findIndex(item => item.skuId === product.skuId)
      if (index > -1) {
        this.cartList[index].quantity += product.quantity
      } else {
        this.cartList.push({ ...product, isSelected: true })
      }
    },
    updateCartItem(item) {
      const index = this.cartList.findIndex(cartItem => cartItem.id === item.id)
      if (index > -1) {
        this.cartList[index] = item
      }
    },
    removeFromCart(ids) {
      this.cartList = this.cartList.filter(item => !ids.includes(item.id))
    },
    toggleSelectAll(selected) {
      this.cartList.forEach(item => {
        item.isSelected = selected
      })
    },
    clearCart() {
      this.cartList = []
    }
  },
  persist: {
    key: 'tongquyouyi-store-cart',
    storage: localStorage
  }
})
```

===FILE:front/store/src/stores/address.js===
```javascript
import { defineStore } from 'pinia'

export const useAddressStore = defineStore('address', {
  state: () => ({
    addressList: [],
    defaultAddress: null
  }),
  actions: {
    setAddressList(list) {
      this.addressList = list
      this.defaultAddress = list.find(item => item.isDefault) || list[0] || null
    },
    addAddress(address) {
      if (address.isDefault) {
        this.addressList.forEach(item => {
          item.isDefault = false
        })
      }
      this.addressList.push(address)
      if (address.isDefault || !this.defaultAddress) {
        this.defaultAddress = address
      }
    },
    updateAddress(address) {
      const index = this.addressList.findIndex(item => item.id === address.id)
      if (index > -1) {
        if (address.isDefault) {
          this.addressList.forEach(item => {
            item.isDefault = false
          })
        }
        this.addressList[index] = address
        this.defaultAddress = this.addressList.find(item => item.isDefault) || this.addressList[0] || null
      }
    },
    removeAddress(id) {
      this.addressList = this.addressList.filter(item => item.id !== id)
      if (this.defaultAddress?.id === id) {
        this.defaultAddress = this.addressList.find(item => item.isDefault) || this.addressList[0] || null
      }
    },
    setDefaultAddress(id) {
      this.addressList.forEach(item => {
        item.isDefault = item.id === id
      })
      this.defaultAddress = this.addressList.find(item => item.isDefault)
    }
  },
  persist: {
    key: 'tongquyouyi-store-address',
    storage: localStorage
  }
})
```

===FILE:front/store/src/utils/auth.js===
```javascript
import { useUserStore } from '@/stores/user'

export const getToken = () => {
  const userStore = useUserStore()
  return userStore.token
}

export const setToken = (data) => {
  const userStore = useUserStore()
  userStore.setToken(data)
}

export const removeToken = () => {
  const userStore = useUserStore()
  userStore.logout()
}
```

===FILE:front/store/src/utils/request.js===
```javascript
import axios from 'axios'
import { showToast } from 'vant'
import { getToken, removeToken } from './auth'
import router from '@/router'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000
})

request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
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
      showToast(res.msg || '请求失败')
      if (res.code === 401) {
        removeToken()
        router.push('/login')
      }
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  (error) => {
    showToast(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
```

===FILE:front/store/src/api/index.js===
```javascript
import request from '@/utils/request'

export const sendSms = (data) => {
  return request({
    url: '/store/auth/send-sms',
    method: 'post',
    data
  })
}

export const login = (data) => {
  return request({
    url: '/store/auth/login',
    method: 'post',
    data
  })
}

export const logout = () => {
  return request({
    url: '/store/auth/logout',
    method: 'post'
  })
}

export const getUserInfo = () => {
  return request({
    url: '/store/user/get',
    method: 'get'
  })
}
```

===FILE:front/store/src/layouts/StoreLayout.vue===
```vue
<template>
  <div class="store-layout">
    <div class="main-content">
      <router-view />
    </div>
    <van-tabbar v-model="active" route>
      <van-tabbar-item replace to="/home" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item replace to="/cart" icon="shopping-cart-o" :badge="cartStore.totalCount > 0 ? cartStore.totalCount : ''">购物车</van-tabbar-item>
      <van-tabbar-item replace to="/user" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useCartStore } from '@/stores'

const route = useRoute()
const cartStore = useCartStore()
const active = ref(route.path)

watch(() => route.path, (newPath) => {
  active.value = newPath
})
</script>

<style lang="scss" scoped>
.store-layout {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;

  .main-content {
    flex: 1;
    overflow-y: auto;
    padding-bottom: 50px;
  }
}
</style>
```

===FILE:front/store/src/views/Login.vue===
```vue
<template>
  <div class="login">
    <van-nav-bar title="登录" />
    <div class="login-content">
      <div class="logo">
        <h1>童趣优衣</h1>
      </div>
      <van-form @submit="handleLogin">
        <van-cell-group inset>
          <van-field
            v-model="loginForm.phone"
            name="phone"
            label="手机号"
            placeholder="请输入手机号"
            :rules="[{ required: true, message: '请输入手机号' }, { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' }]"
          />
          <van-field
            v-model="loginForm.code"
            name="code"
            label="验证码"
            placeholder="请输入验证码"
            :rules="[{ required: true, message: '请输入验证码' }]"
          >
            <template #button>
              <van-button
                size="small"
                type="primary"
                :disabled="countdown > 0"
                @click="handleSendSms"
              >
                {{ countdown > 0 ? `${countdown}秒后重试` : '发送验证码' }}
              </van-button>
            </template>
          </van-field>
        </van-cell-group>
        <div style="margin: 16px;">
          <van-button round block type="primary" native-type="submit" :loading="loading">
            登录
          </van-button>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '@/stores'
import { sendSms, login, getUserInfo } from '@/api'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const countdown = ref(0)

const loginForm = reactive({
  phone: '',
  code: ''
})

let timer = null

const handleSendSms = async () => {
  if (!loginForm.phone || !/^1[3-9]\d{9}$/.test(loginForm.phone)) {
    showToast('请输入正确的手机号')
    return
  }
  try {
    await sendSms({ phone: loginForm.phone })
    showToast('验证码发送成功')
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    console.error('发送验证码失败', error)
  }
}

const handleLogin = async () => {
  loading.value = true
  try {
    const res = await login(loginForm)
    userStore.setToken(res.data)
    if (!res.data.needInit) {
      const profileRes = await getUserInfo()
      userStore.setUserInfo(profileRes.data)
    }
    showToast('登录成功')
    if (res.data.needInit) {
      // 后续跳转到完善信息页
      router.push('/user')
    } else {
      router.push('/')
    }
  } catch (error) {
    console.error('登录失败', error)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login {
  width: 100%;
  height: 100%;
  background-color: #fff;

  .login-content {
    padding-top: 80px;

    .logo {
      text-align: center;
      margin-bottom: 60px;

      h1 {
        font-size: 32px;
        color: #333;
      }
    }
  }
}
</style>
```

===FILE:front/store/src/views/Home.vue===
```vue
<template>
  <div class="home">
    <van-nav-bar title="童趣优衣" />
    <div class="home-content">
      <van-empty description="首页开发中" />
    </div>
  </div>
</template>

<script setup>
</script>

<style lang="scss" scoped>
.home {
  background-color: #fff;
}
</style>
```

===FILE:front/store/src/views/Cart.vue===
```vue
<template>
  <div class="cart">
    <van-nav-bar title="购物车" />
    <div class="cart-content">
      <van-empty description="购物车开发中" />
    </div>
  </div>
</template>

<script setup>
</script>

<style lang="scss" scoped>
.cart {
  background-color: #fff;
}
</style>
```

===FILE:front/store/src/views/User.vue===
```vue
<template>
  <div class="user">
    <van-nav-bar title="我的" />
    <div class="user-content">
      <van-cell-group inset>
        <van-cell center>
          <template #icon>
            <van-avatar :size="64" :src="userStore.userInfo?.avatar" />
          </template>
          <template #default>
            <div class="user-info">
              <div class="nickname">{{ userStore.userInfo?.nickname || '未登录' }}</div>
              <div class="phone">{{ userStore.userInfo?.phone || '' }}</div>
            </div>
          </template>
        </van-cell>
      </van-cell-group>
      <van-cell-group inset style="margin-top: 16px;">
        <van-cell title="我的订单" is-link />
        <van-cell title="我的收藏" is-link />
        <van-cell title="我的积分" is-link />
        <van-cell title="我的储值" is-link />
        <van-cell title="我的优惠券" is-link />
        <van-cell title="收货地址" is-link />
      </van-cell-group>
      <van-cell-group inset style="margin-top: 16px;">
        <van-cell title="个人信息" is-link />
        <van-cell title="退出登录" @click="handleLogout" />
      </van-cell-group>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { useUserStore } from '@/stores'
import { logout as logoutApi } from '@/api'

const router = useRouter()
const userStore = useUserStore()

const handleLogout = async () => {
  try {
    await showConfirmDialog({
      title: '提示',
      message: '确定要退出登录吗？'
    })
    await logoutApi()
    userStore.logout()
    showToast('退出成功')
    router.push('/login')
  } catch (error) {
    console.log('取消退出')
  }
}
</script>

<style lang="scss" scoped>
.user {
  background-color: #f5f5f5;

  .user-content {
    .user-info {
      text-align: left;
      margin-left: 16px;

      .nickname {
        font-size: 18px;
        font-weight: bold;
        color: #333;
        margin-bottom: 4px;
      }

      .phone {
        font-size: 14px;
        color: #999;
      }
    }
  }
}
</style>
```

===FILE:front/store/src/views/404.vue===
```vue
<template>
  <div class="not-found">
    <van-empty description="页面不存在">
      <van-button type="primary" @click="router.push('/')">返回首页</van-button>
    </van-empty>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style lang="scss" scoped>
.not-found {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #fff;
}
</style>
```

===FILE:front/store/src/stores/index.js===
```javascript
export * from './user'
export * from './cart'
export * from './address'
```