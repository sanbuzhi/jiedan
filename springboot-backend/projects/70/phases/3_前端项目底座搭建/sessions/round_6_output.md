===FILE:front/admin/src/router/index.js===
```javascript
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false })

const routes = [
  {
    path: '/login',
    component: () => import('@/layouts/LoginLayout.vue'),
    children: [
      {
        path: '',
        name: 'Login',
        component: () => import('@/views/Login.vue'),
        meta: { title: '登录', requiresAuth: false }
      }
    ]
  },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '仪表盘', icon: 'DataAnalysis' }
      },
      {
        path: 'products',
        name: 'ProductList',
        component: () => import('@/views/ProductList.vue'),
        meta: { title: '商品管理', icon: 'Goods' }
      },
      {
        path: 'orders',
        name: 'OrderList',
        component: () => import('@/views/OrderList.vue'),
        meta: { title: '订单管理', icon: 'Tickets' }
      },
      {
        path: 'users',
        name: 'UserList',
        component: () => import('@/views/UserList.vue'),
        meta: { title: '用户管理', icon: 'User' }
      }
    ]
  },
  {
    path: '/404',
    name: '404',
    component: () => import('@/views/404.vue'),
    meta: { title: '404' }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach(async (to, from, next) => {
  NProgress.start()
  document.title = to.meta.title ? `${to.meta.title} - 电商后台` : '电商后台'

  const userStore = useUserStore()
  const token = userStore.token

  if (to.meta.requiresAuth) {
    if (!token) {
      next({ name: 'Login' })
    } else {
      if (!userStore.userInfo.id) {
        try {
          await userStore.getUserInfo()
        } catch (error) {
          await userStore.logout()
          next({ name: 'Login' })
          return
        }
      }
      next()
    }
  } else {
    if (token && to.name === 'Login') {
      next({ name: 'Dashboard' })
    } else {
      next()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})

export default router
```
===FILE:front/store/src/router/index.js===
```javascript
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false })

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/StoreLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页', requiresAuth: false }
      },
      {
        path: 'products',
        name: 'ProductList',
        component: () => import('@/views/ProductList.vue'),
        meta: { title: '商品列表', requiresAuth: false }
      },
      {
        path: 'product/:id',
        name: 'ProductDetail',
        component: () => import('@/views/ProductDetail.vue'),
        meta: { title: '商品详情', requiresAuth: false }
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/Cart.vue'),
        meta: { title: '购物车', requiresAuth: true }
      },
      {
        path: 'checkout',
        name: 'Checkout',
        component: () => import('@/views/Checkout.vue'),
        meta: { title: '结算', requiresAuth: true }
      },
      {
        path: 'orders',
        name: 'OrderList',
        component: () => import('@/views/OrderList.vue'),
        meta: { title: '我的订单', requiresAuth: true }
      },
      {
        path: 'addresses',
        name: 'AddressList',
        component: () => import('@/views/AddressList.vue'),
        meta: { title: '收货地址', requiresAuth: true }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/User.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', requiresAuth: false }
  },
  {
    path: '/404',
    name: '404',
    component: () => import('@/views/404.vue'),
    meta: { title: '404' }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach(async (to, from, next) => {
  NProgress.start()
  document.title = to.meta.title ? `${to.meta.title} - 电商商城` : '电商商城'

  const userStore = useUserStore()
  const token = userStore.token

  if (to.meta.requiresAuth) {
    if (!token) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
    } else {
      if (!userStore.userInfo.id) {
        try {
          await userStore.getUserInfo()
        } catch (error) {
          await userStore.logout()
          next({ name: 'Login', query: { redirect: to.fullPath } })
          return
        }
      }
      next()
    }
  } else {
    if (token && (to.name === 'Login' || to.name === 'Register')) {
      next({ name: 'Home' })
    } else {
      next()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})

export default router
```
===FILE:front/admin/src/stores/user.js===
```javascript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { login, getUserInfo, logout as logoutApi } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const userInfo = ref({})

  const isLogin = computed(() => !!token.value)

  async function handleLogin(data) {
    const res = await login(data)
    token.value = res.data.token
    setToken(res.data.token)
    await getUserInfoFromApi()
  }

  async function getUserInfoFromApi() {
    const res = await getUserInfo()
    userInfo.value = res.data
  }

  async function logout() {
    try {
      await logoutApi()
    } catch (error) {
      console.error('Logout API error:', error)
    }
    token.value = ''
    userInfo.value = {}
    removeToken()
  }

  return {
    token,
    userInfo,
    isLogin,
    handleLogin,
    getUserInfo: getUserInfoFromApi,
    logout
  }
})
```
===FILE:front/store/src/stores/user.js===
```javascript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { login, register, getUserInfo, logout as logoutApi } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const userInfo = ref({})

  const isLogin = computed(() => !!token.value)

  async function handleLogin(data) {
    const res = await login(data)
    token.value = res.data.token
    setToken(res.data.token)
    await getUserInfoFromApi()
  }

  async function handleRegister(data) {
    const res = await register(data)
    token.value = res.data.token
    setToken(res.data.token)
    await getUserInfoFromApi()
  }

  async function getUserInfoFromApi() {
    const res = await getUserInfo()
    userInfo.value = res.data
  }

  async function logout() {
    try {
      await logoutApi()
    } catch (error) {
      console.error('Logout API error:', error)
    }
    token.value = ''
    userInfo.value = {}
    removeToken()
  }

  return {
    token,
    userInfo,
    isLogin,
    handleLogin,
    handleRegister,
    getUserInfo: getUserInfoFromApi,
    logout
  }
})
```
===FILE:front/store/src/stores/cart.js===
```javascript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCartList, addCart, updateCart, deleteCart, clearCart } from '@/api/product'

export const useCartStore = defineStore('cart', () => {
  const cartList = ref([])

  const totalCount = computed(() => {
    return cartList.value.reduce((sum, item) => sum + item.quantity, 0)
  })

  const totalPrice = computed(() => {
    return cartList.value.reduce((sum, item) => sum + item.quantity * item.product.price, 0)
  })

  const checkedItems = computed(() => {
    return cartList.value.filter(item => item.checked)
  })

  const checkedCount = computed(() => {
    return checkedItems.value.reduce((sum, item) => sum + item.quantity, 0)
  })

  const checkedPrice = computed(() => {
    return checkedItems.value.reduce((sum, item) => sum + item.quantity * item.product.price, 0)
  })

  const allChecked = computed(() => {
    return cartList.value.length > 0 && cartList.value.every(item => item.checked)
  })

  async function fetchCartList() {
    const res = await getCartList()
    cartList.value = res.data.map(item => ({ ...item, checked: true }))
  }

  async function handleAddCart(productId, quantity = 1) {
    const existing = cartList.value.find(item => item.productId === productId)
    if (existing) {
      await handleUpdateCart(existing.id, existing.quantity + quantity)
    } else {
      const res = await addCart({ productId, quantity })
      cartList.value.push({ ...res.data, checked: true })
    }
  }

  async function handleUpdateCart(id, quantity) {
    await updateCart(id, { quantity })
    const item = cartList.value.find(item => item.id === id)
    if (item) item.quantity = quantity
  }

  async function handleDeleteCart(id) {
    await deleteCart(id)
    cartList.value = cartList.value.filter(item => item.id !== id)
  }

  async function handleClearCart() {
    await clearCart()
    cartList.value = []
  }

  function toggleItemChecked(id) {
    const item = cartList.value.find(item => item.id === id)
    if (item) item.checked = !item.checked
  }

  function toggleAllChecked(checked) {
    cartList.value.forEach(item => {
      item.checked = checked
    })
  }

  function resetCheckedItems() {
    cartList.value = cartList.value.filter(item => !item.checked)
  }

  return {
    cartList,
    totalCount,
    totalPrice,
    checkedItems,
    checkedCount,
    checkedPrice,
    allChecked,
    fetchCartList,
    handleAddCart,
    handleUpdateCart,
    handleDeleteCart,
    handleClearCart,
    toggleItemChecked,
    toggleAllChecked,
    resetCheckedItems
  }
})
```
===FILE:front/store/src/stores/address.js===
```javascript
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getAddressList, addAddress, updateAddress, deleteAddress, setDefaultAddress } from '@/api/address'

export const useAddressStore = defineStore('address', () => {
  const addressList = ref([])

  async function fetchAddressList() {
    const res = await getAddressList()
    addressList.value = res.data
  }

  async function handleAddAddress(data) {
    const res = await addAddress(data)
    if (data.isDefault) {
      addressList.value.forEach(item => item.isDefault = false)
    }
    addressList.value.unshift(res.data)
  }

  async function handleUpdateAddress(id, data) {
    const res = await updateAddress(id, data)
    const index = addressList.value.findIndex(item => item.id === id)
    if (index !== -1) {
      if (data.isDefault) {
        addressList.value.forEach(item => item.isDefault = false)
      }
      addressList.value[index] = res.data
    }
  }

  async function handleDeleteAddress(id) {
    await deleteAddress(id)
    addressList.value = addressList.value.filter(item => item.id !== id)
  }

  async function handleSetDefaultAddress(id) {
    await setDefaultAddress(id)
    addressList.value.forEach(item => {
      item.isDefault = item.id === id
    })
  }

  const defaultAddress = computed(() => {
    return addressList.value.find(item => item.isDefault) || addressList.value[0]
  })

  return {
    addressList,
    defaultAddress,
    fetchAddressList,
    handleAddAddress,
    handleUpdateAddress,
    handleDeleteAddress,
    handleSetDefaultAddress
  }
})
```
===FILE:front/admin/src/utils/request.js===
```javascript
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000
})

service.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
        router.push({ name: 'Login' })
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    console.error('Response error:', error)
    if (error.response) {
      switch (error.response.status) {
        case 401:
          ElMessage.error('登录已过期，请重新登录')
          const userStore = useUserStore()
          userStore.logout()
          router.push({ name: 'Login' })
          break
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(error.response.data.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default service
```
===FILE:front/store/src/utils/request.js===
```javascript
import axios from 'axios'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000
})

service.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      showToast(res.message || '请求失败')
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
        const redirect = router.currentRoute.value.fullPath
        router.push({ name: 'Login', query: { redirect } })
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    console.error('Response error:', error)
    if (error.response) {
      switch (error.response.status) {
        case 401:
          showToast('登录已过期，请重新登录')
          const userStore = useUserStore()
          userStore.logout()
          const redirect = router.currentRoute.value.fullPath
          router.push({ name: 'Login', query: { redirect } })
          break
        case 403:
          showToast('没有权限访问')
          break
        case 404:
          showToast('请求的资源不存在')
          break
        case 500:
          showToast('服务器错误')
          break
        default:
          showToast(error.response.data.message || '请求失败')
      }
    } else {
      showToast('网络错误，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default service
```
===FILE:front/admin/src/utils/auth.js===
```javascript
const TOKEN_KEY = 'admin_token'

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
===FILE:front/store/src/utils/auth.js===
```javascript
const TOKEN_KEY = 'store_token'

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
===FILE:front/admin/src/api/index.js===
```javascript
export * from './user'
export * from './product'
export * from './order'
```
===FILE:front/store/src/api/index.js===
```javascript
export * from './user'
export * from './product'
export * from './order'
export * from './address'
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

export function getUserInfo() {
  return request({
    url: '/admin/auth/info',
    method: 'get'
  })
}

export function logout() {
  return request({
    url: '/admin/auth/logout',
    method: 'post'
  })
}

export function getUserList(params) {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

export function updateUserStatus(id, data) {
  return request({
    url: `/admin/users/${id}/status`,
    method: 'put',
    data
  })
}
```
===FILE:front/admin/src/api/product.js===
```javascript
import request from '@/utils/request'

export function getProductList(params) {
  return request({
    url: '/admin/products',
    method: 'get',
    params
  })
}

export function getProductDetail(id) {
  return request({
    url: `/admin/products/${id}`,
    method: 'get'
  })
}

export function createProduct(data) {
  return request({
    url: '/admin/products',
    method: 'post',
    data
  })
}

export function updateProduct(id, data) {
  return request({
    url: `/admin/products/${id}`,
    method: 'put',
    data
  })
}

export function deleteProduct(id) {
  return request({
    url: `/admin/products/${id}`,
    method: 'delete'
  })
}

export function updateProductStatus(id, data) {
  return request({
    url: `/admin/products/${id}/status`,
    method: 'put',
    data
  })
}
```
===FILE:front/admin/src/api/order.js===
```javascript
import request from '@/utils/request'

export function getOrderList(params) {
  return request({
    url: '/admin/orders',
    method: 'get',
    params
  })
}

export function getOrderDetail(id) {
  return request({
    url: `/admin/orders/${id}`,
    method: 'get'
  })
}

export function updateOrderStatus(id, data) {
  return request({
    url: `/admin/orders/${id}/status`,
    method: 'put',
    data
  })
}
```
===FILE:front/store/src/api/user.js===
```javascript
import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/store/auth/login',
    method: 'post',
    data
  })
}

export function register(data) {
  return request({
    url: '/store/auth/register',
    method: 'post',
    data
  })
}

export function getUserInfo() {
  return request({
    url: '/store/auth/info',
    method: 'get'
  })
}

export function logout() {
  return request({
    url: '/store/auth/logout',
    method: 'post'
  })
}

export function updateUserInfo(data) {
  return request({
    url: '/store/users/info',
    method: 'put',
    data
  })
}

export function updatePassword(data) {
  return request({
    url: '/store/users/password',
    method: 'put',
    data
  })
}
```
===FILE:front/store/src/api/product.js===
```javascript
import request from '@/utils/request'

export function getProductList(params) {
  return request({
    url: '/store/products',
    method: 'get',
    params
  })
}

export function getProductDetail(id) {
  return request({
    url: `/store/products/${id}`,
    method: 'get'
  })
}

export function getCartList() {
  return request({
    url: '/store/cart',
    method: 'get'
  })
}

export function addCart(data) {
  return request({
    url: '/store/cart',
    method: 'post',
    data
  })
}

export function updateCart(id, data) {
  return request({
    url: `/store/cart/${id}`,
    method: 'put',
    data
  })
}

export function deleteCart(id) {
  return request({
    url: `/store/cart/${id}`,
    method: 'delete'
  })
}

export function clearCart() {
  return request({
    url: '/store/cart',
    method: 'delete'
  })
}
```
===FILE:front/store/src/api/order.js===
```javascript
import request from '@/utils/request'

export function createOrder(data) {
  return request({
    url: '/store/orders',
    method: 'post',
    data
  })
}

export function getOrderList(params) {
  return request({
    url: '/store/orders',
    method: 'get',
    params
  })
}

export function getOrderDetail(id) {
  return request({
    url: `/store/orders/${id}`,
    method: 'get'
  })
}

export function cancelOrder(id) {
  return request({
    url: `/store/orders/${id}/cancel`,
    method: 'post'
  })
}

export function payOrder(id, data) {
  return request({
    url: `/store/orders/${id}/pay`,
    method: 'post',
    data
  })
}

export function confirmOrder(id) {
  return request({
    url: `/store/orders/${id}/confirm`,
    method: 'post'
  })
}
```
===FILE:front/store/src/api/address.js===
```javascript
import request from '@/utils/request'

export function getAddressList() {
  return request({
    url: '/store/addresses',
    method: 'get'
  })
}

export function getAddressDetail(id) {
  return request({
    url: `/store/addresses/${id}`,
    method: 'get'
  })
}

export function addAddress(data) {
  return request({
    url: '/store/addresses',
    method: 'post',
    data
  })
}

export function updateAddress(id, data) {
  return request({
    url: `/store/addresses/${id}`,
    method: 'put',
    data
  })
}

export function deleteAddress(id) {
  return request({
    url: `/store/addresses/${id}`,
    method: 'delete'
  })
}

export function setDefaultAddress(id) {
  return request({
    url: `/store/addresses/${id}/default`,
    method: 'post'
  })
}
```
===FILE:front/admin/src/stores/index.js===
```javascript
import { createPinia } from 'pinia'
import { createPersistedState } from 'pinia-plugin-persistedstate'

const pinia = createPinia()
pinia.use(createPersistedState({
  storage: localStorage
}))

export default pinia
```
===FILE:front/store/src/stores/index.js===
```javascript
import { createPinia } from 'pinia'
import { createPersistedState } from 'pinia-plugin-persistedstate'

const pinia = createPinia()
pinia.use(createPersistedState({
  storage: localStorage
}))

export default pinia
```
===FILE:front/admin/src/main.js===
```javascript
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import pinia from './stores'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import './assets/index.css'

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus)

app.mount('#app')
```
===FILE:front/store/src/main.js===
```javascript
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import pinia from './stores'
import { Button, NavBar, Tabbar, TabbarItem, Card, Tag, Stepper, Checkbox, CheckboxGroup, Form, Field, CellGroup, Cell, Button as VanButton, Swipe, SwipeItem, Search, List, PullRefresh, Dialog, Toast, Loading, Popup, Picker, DatetimePicker, Radio, RadioGroup, Divider } from 'vant'
import 'vant/lib/index.css'
import './assets/index.css'

const app = createApp(App)

app.use(pinia)
app.use(router)
app.use(Button)
app.use(NavBar)
app.use(Tabbar)
app.use(TabbarItem)
app.use(Card)
app.use(Tag)
app.use(Stepper)
app.use(Checkbox)
app.use(CheckboxGroup)
app.use(Form)
app.use(Field)
app.use(CellGroup)
app.use(Cell)
app.use(VanButton)
app.use(Swipe)
app.use(SwipeItem)
app.use(Search)
app.use(List)
app.use(PullRefresh)
app.use(Dialog)
app.use(Toast)
app.use(Loading)
app.use(Popup)
app.use(Picker)
app.use(DatetimePicker)
app.use(Radio)
app.use(RadioGroup)
app.use(Divider)

app.mount('#app')
```
===FILE:front/admin/src/App.vue===
```vue
<template>
  <router-view />
</template>

<script setup>
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  width: 100%;
  height: 100%;
}
</style>
```
===FILE:front/store/src/App.vue===
```vue
<template>
  <router-view />
</template>

<script setup>
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  width: 100%;
  height: 100%;
  background-color: #f5f5f5;
}
</style>
```
===FILE:front/admin/vite.config.js===
```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src')
      }
    },
    server: {
      port: 3001,
      proxy: {
        [env.VITE_API_BASE_URL]: {
          target: 'http://localhost:8080',
          changeOrigin: true,
          rewrite: (path) => path.replace(new RegExp(`^${env.VITE_API_BASE_URL}`), '')
        }
      }
    }
  }
})

function loadEnv(mode, envDir) {
  const fs = require('fs')
  const path = require('path')
  const envPath = path.resolve(envDir, `.env.${mode}`)
  const env = {}
  if (fs.existsSync(envPath)) {
    const config = fs.readFileSync(envPath, 'utf-8')
    config.split('\n').forEach(line => {
      const [key, value] = line.split('=')
      if (key && value) {
        env[key.trim()] = value.trim().replace(/^['"](.*)['"]$/, '$1')
      }
    })
  }
  return env
}
```
===FILE:front/store/vite.config.js===
```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src')
      }
    },
    server: {
      port: 3000,
      proxy: {
        [env.VITE_API_BASE_URL]: {
          target: 'http://localhost:8080',
          changeOrigin: true,
          rewrite: (path) => path.replace(new RegExp(`^${env.VITE_API_BASE_URL}`), '')
        }
      }
    }
  }
})

function loadEnv(mode, envDir) {
  const fs = require('fs')
  const path = require('path')
  const envPath = path.resolve(envDir, `.env.${mode}`)
  const env = {}
  if (fs.existsSync(envPath)) {
    const config = fs.readFileSync(envPath, 'utf-8')
    config.split('\n').forEach(line => {
      const [key, value] = line.split('=')
      if (key && value) {
        env[key.trim()] = value.trim().replace(/^['"](.*)['"]$/, '$1')
      }
    })
  }
  return env
}
```
===FILE:front/admin/package.json===
```json
{
  "name": "admin",
  "private": true,
  "version": "0.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.3.11",
    "vue-router": "^4.2.5",
    "pinia": "^2.1.7",
    "pinia-plugin-persistedstate": "^3.2.0",
    "element-plus": "^2.3.14",
    "@element-plus/icons-vue": "^2.3.1",
    "axios": "^1.6.2",
    "nprogress": "^0.2.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^4.5.2",
    "vite": "^5.0.8"
  }
}
```
===FILE:front/store/package.json===
```json
{
  "name": "store",
  "private": true,
  "version": "0.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.3.11",
    "vue-router": "^4.2.5",
    "pinia": "^2.1.7",
    "pinia-plugin-persistedstate": "^3.2.0",
    "vant": "^4.8.0",
    "axios": "^1.6.2",
    "nprogress": "^0.2.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^4.5.2",
    "vite": "^5.0.8"
  }
}
```
===FILE:front/admin/.env.development===
```
VITE_API_BASE_URL=/api
```
===FILE:front/admin/.env.production===
```
VITE_API_BASE_URL=https://api.example.com
```
===FILE:front/store/.env.development===
```
VITE_API_BASE_URL=/api
```
===FILE:front/store/.env.production===
```
VITE_API_BASE_URL=https://api.example.com
```
===FILE:front/admin/src/layouts/LoginLayout.vue===
```vue
<template>
  <div class="login-layout">
    <div class="login-container">
      <router-view />
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.login-layout {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-container {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}
</style>
```
===FILE:front/admin/src/layouts/AdminLayout.vue===
```vue
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
```
===FILE:front/store/src/layouts/StoreLayout.vue===
```vue
<template>
  <div class="store-layout">
    <router-view />
    <van-tabbar v-model="activeTab" route v-if="showTabbar">
      <van-tabbar-item replace to="/home" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item replace to="/products" icon="search">商品</van-tabbar-item>
      <van-tabbar-item replace to="/cart" icon="shopping-cart-o" :badge="cartStore.totalCount > 99 ? '99+' : cartStore.totalCount || ''">购物车</van-tabbar-item>
      <van-tabbar-item replace to="/user" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useCartStore } from '@/stores/cart'

const route = useRoute()
const cartStore = useCartStore()
const activeTab = ref(0)

const showTabbar = computed(() => {
  const noTabbarPaths = ['/product', '/checkout', '/orders', '/addresses', '/login', '/register', '/404']
  return !noTabbarPaths.some(path => route.path.startsWith(path))
})

if (cartStore.isLogin) {
  cartStore.fetchCartList().catch(() => {})
}
</script>

<style scoped>
.store-layout {
  width: 100%;
  min-height: 100%;
  padding-bottom: 50px;
}
</style>
```
===FILE:front/admin/src/views/Login.vue===
```vue
<template>
  <div class="login">
    <h2 class="title">电商后台管理系统</h2>
    <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-position="top">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="User" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password @keyup.enter="handleLogin" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" style="width: 100%" @click="handleLogin">登录</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.handleLogin(loginForm)
        ElMessage.success('登录成功')
        router.push({ name: 'Dashboard' })
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login {
  width: 100%;
}

.title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}
</style>
```
===FILE:front/store/src/views/Login.vue===
```vue
<template>
  <div class="login">
    <van-nav-bar title="登录" left-arrow @click-left="goBack" />
    <div class="login-container">
      <h2 class="title">欢迎登录</h2>
      <van-form @submit="handleLogin">
        <van-cell-group inset>
          <van-field
            v-model="loginForm.username"
            name="username"
            label="用户名"
            placeholder="请输入用户名"
            :rules="[{ required: true, message: '请输入用户名' }]"
          />
          <van-field
            v-model="loginForm.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请输入密码' }]"
          />
        </van-cell-group>
        <div class="button-group">
          <van-button round block type="primary" native-type="submit" :loading="loading">登录</van-button>
          <van-button round block plain type="primary" @click="goRegister">注册账号</van-button>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { showToast } from 'vant'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

function goBack() {
  router.back()
}

function goRegister() {
  router.push({ name: 'Register' })
}

async function handleLogin() {
  loading.value = true
  try {
    await userStore.handleLogin(loginForm)
    showToast('登录成功')
    const redirect = route.query.redirect || '/home'
    router.push(redirect)
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login {
  width: 100%;
  min-height: 100%;
  background-color: white;
}

.login-container {
  padding: 40px 20px;
}

.title {
  text-align: center;
  margin-bottom: 40px;
  color: #333;
  font-size: 24px;
}

.button-group {
  margin-top: 40px;
  padding: 0 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
```
===FILE:front/store/src/views/Register.vue===
```vue
<template>
  <div class="register">
    <van-nav-bar title="注册" left-arrow @click-left="goBack" />
    <div class="register-container">
      <h2 class="title">创建账号</h2>
      <van-form @submit="handleRegister">
        <van-cell-group inset>
          <van-field
            v-model="registerForm.username"
            name="username"
            label="用户名"
            placeholder="请输入用户名"
            :rules="[{ required: true, message: '请输入用户名' }, { min: 3, message: '用户名至少3个字符' }]"
          />
          <van-field
            v-model="registerForm.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请输入密码' }, { min: 6, message: '密码至少6个字符' }]"
          />
          <van-field
            v-model="registerForm.confirmPassword"
            type="password"
            name="confirmPassword"
            label="确认密码"
            placeholder="请再次输入密码"
            :rules="[{ required: true, message: '请确认密码' }, { validator: validateConfirmPassword, message: '两次输入的密码不一致' }]"
          />
        </van-cell-group>
        <div class="button-group">
          <van-button round block type="primary" native-type="submit" :loading="loading">注册</van-button>
          <van-button round block plain type="primary" @click="goLogin">已有账号？登录</van-button>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { showToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

function validateConfirmPassword(val) {
  return val === registerForm.password
}

function goBack() {
  router.back()
}

function goLogin() {
  router.push({ name: 'Login' })
}

async function handleRegister() {
  loading.value = true
  try {
    await userStore.handleRegister(registerForm)
    showToast('注册成功')
    router.push('/home')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register {
  width: 100%;
  min-height: 100%;
  background-color: white;
}

.register-container {
  padding: 40px 20px;
}

.title {
  text-align: center;
  margin-bottom: 40px;
  color: #333;
  font-size: 24px;
}

.button-group {
  margin-top: 40px;
  padding: 0 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
```
===FILE:front/admin/src/views/Dashboard.vue===
```vue
<template>
  <div class="dashboard">
    <PageHeader title="仪表盘" />
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #409EFF">
              <el-icon :size="30"><DataLine /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">1234</div>
              <div class="stat-label">商品总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #67C23A">
              <el-icon :size="30"><ShoppingCart /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">567</div>
              <div class="stat-label">今日订单</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #E6A23C">
              <el-icon :size="30"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">8901</div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #F56C6C">
              <el-icon :size="30"><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥123,456</div>
              <div class="stat-label">今日销售额</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import PageHeader from '@/components/PageHeader.vue'
import { DataLine, ShoppingCart, User, Money } from '@element-plus/icons-vue'
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 4px;
}
</style>
```
===FILE:front/admin/src/views/ProductList.vue===
```vue
<template>
  <div class="product-list">
    <PageHeader title="商品管理">
      <template #extra>
        <el-button type="primary">
          <el-icon><Plus /></el-icon>
          新增商品
        </el-button>
      </template>
    </PageHeader>
    <el-card style="margin-top: 20px">
      <el-form :inline="true" :model="queryForm" style="margin-bottom: 20px">
        <el-form-item label="商品名称">
          <el-input v-model="queryForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="商品名称" />
        <el-table-column prop="price" label="价格" width="120">
          <template #default="{ row }">
            ¥{{ row.price.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small">编辑</el-button>
            <el-button type="warning" link size="small">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button type="danger" link size="small">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        @change="handleQuery"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import Pagination from '@/components/Pagination.vue'
import { Plus, Search, RefreshLeft } from '@element-plus/icons-vue'
import { getProductList } from '@/api/product'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryForm = reactive({
  name: '',
  status: '',
  page: 1,
  pageSize: 10
})

async function handleQuery() {
  loading.value = true
  try {
    const res = await getProductList(queryForm)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

function handleReset() {
  queryForm.name = ''
  queryForm.status = ''
  queryForm.page = 1
  handleQuery()
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped>
</style>
```
===FILE:front/admin/src/views/OrderList.vue===
```vue
<template>
  <div class="order-list">
    <PageHeader title="订单管理" />
    <el-card style="margin-top: 20px">
      <el-form :inline="true" :model="queryForm" style="margin-bottom: 20px">
        <el-form-item label="订单号">
          <el-input v-model="queryForm.orderNo" placeholder="请输入订单号" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
            <el-option label="待付款" :value="0" />
            <el-option label="待发货" :value="1" />
            <el-option label="已发货" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="username" label="下单用户" width="120" />
        <el-table-column prop="totalAmount" label="总金额" width="120">
          <template #default="{ row }">
            ¥{{ row.totalAmount.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTypeMap[row.status]">
              {{ statusTextMap[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small">查看</el-button>
            <el-button v-if="row.status === 1" type="success" link size="small">发货</el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        @change="handleQuery"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import Pagination from '@/components/Pagination.vue'
import { Search, RefreshLeft } from '@element-plus/icons-vue'
import { getOrderList } from '@/api/order'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const statusTypeMap = {
  0: 'info',
  1: 'warning',
  2: 'primary',
  3: 'success',
  4: 'danger'
}

const statusTextMap = {
  0: '待付款',
  1: '待发货',
  2: '已发货',
  3: '已完成',
  4: '已取消'
}

const queryForm = reactive({
  orderNo: '',
  status: '',
  page: 1,
  pageSize: 10
})

async function handleQuery() {
  loading.value = true
  try {
    const res = await getOrderList(queryForm)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

function handleReset() {
  queryForm.orderNo = ''
  queryForm.status = ''
  queryForm.page = 1
  handleQuery()
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped>
</style>
```
===FILE:front/admin/src/views/UserList.vue===
```vue
<template>
  <div class="user-list">
    <PageHeader title="用户管理" />
    <el-card style="margin-top: 20px">
      <el-form :inline="true" :model="queryForm" style="margin-bottom: 20px">
        <el-form-item label="用户名">
          <el-input v-model="queryForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button :type="row.status === 1 ? 'warning' : 'success'" link size="small">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        @change="handleQuery"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import Pagination from '@/components/Pagination.vue'
import { Search, RefreshLeft } from '@element-plus/icons-vue'
import { getUserList } from '@/api/user'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryForm = reactive({
  username: '',
  status: '',
  page: 1,
  pageSize: 10
})

async function handleQuery() {
  loading.value = true
  try {
    const res = await getUserList(queryForm)
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

function handleReset() {
  queryForm.username = ''
  queryForm.status = ''
  queryForm.page = 1
  handleQuery()
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped>
</style>
```
===FILE:front/admin/src/components/PageHeader.vue===
```vue
<template>
  <el-page-header @back="goBack">
    <template #content>
      <span class="page-title">{{ title }}</span>
    </template>
    <template #extra>
      <slot name="extra"></slot>
    </template>
  </el-page-header>
</template>

<script setup>
import { useRouter } from 'vue-router'

defineProps({
  title: {
    type: String,
    default: ''
  }
})

const router = useRouter()

function goBack() {
  router.back()
}
</script>

<style scoped>
.page-title {
  font-size: 18px;
  font-weight: bold;
}
</style>
```
===FILE:front/admin/src/components/Pagination.vue===
```vue
<template>
  <div class="pagination-container">
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :page-sizes="[10, 20, 50, 100]"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleChange"
      @current-change="handleChange"
    />
  </div>
</template>

<script setup>
import { watch } from 'vue'

const props = defineProps({
  currentPage: {
    type: Number,
    default: 1
  },
  pageSize: {
    type: Number,
    default: 10
  },
  total: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['update:currentPage', 'update:pageSize', 'change'])

function handleChange() {
  emit('update:currentPage', props.currentPage)
  emit('update:pageSize', props.pageSize)
  emit('change')
}

watch(() => props.total, () => {
  if (props.currentPage > Math.ceil(props.total / props.pageSize)) {
    emit('update:currentPage', 1)
  }
})
</script>

<style scoped>
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
```
===FILE:front/store/src/views/Home.vue===
```vue
<template>
  <div class="home">
    <van-nav-bar title="电商商城">
      <template #right>
        <van-icon name="search" size="20" @click="goSearch" />
      </template>
    </van-nav-bar>
    <van-swipe :autoplay="3000" indicator-color="white">
      <van-swipe-item v-for="(item, index) in bannerList" :key="index">
        <img :src="item" alt="banner" />
      </van-swipe-item>
    </van-swipe>
    <div class="category-section">
      <van-grid :column-num="5" :border="false">
        <van-grid-item v-for="(item, index) in categoryList" :key="index" :icon="item.icon" :text="item.text" @click="goCategory(item.text)" />
      </van-grid>
    </div>
    <div class="product-section">
      <div class="section-header">
        <span class="section-title">热门商品</span>
        <span class="section-more" @click="goProductList">更多 ></span>
      </div>
      <div class="product-grid">
        <ProductCard v-for="product in productList" :key="product.id" :product="product" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import ProductCard from '@/components/ProductCard.vue'
import { getProductList } from '@/api/product'

const router = useRouter()
const bannerList = ref([
  'https://fastly.jsdelivr.net/npm/@vant/assets/apple-1.jpeg',
  'https://fastly.jsdelivr.net/npm/@vant/assets/apple-2.jpeg'
])
const categoryList = ref([
  { icon: 'shopping-cart-o', text: '全部' },
  { icon: 'tv-o', text: '数码' },
  { icon: 'clothes-o', text: '服饰' },
  { icon: 'food-o', text: '美食' },
  { icon: 'flower-o', text: '美妆' }
])
const productList = ref([])

async function fetchProductList() {
  try {
    const res = await getProductList({ page: 1, pageSize: 10 })
    productList.value = res.data.list || []
  } catch (error) {
    console.error(error)
  }
}

function goSearch() {
  router.push({ name: 'ProductList' })
}

function goCategory(category) {
  router.push({ name: 'ProductList', query: { category } })
}

function goProductList() {
  router.push({ name: 'ProductList' })
}

onMounted(() => {
  fetchProductList()
})
</script>

<style scoped>
.home {
  width: 100%;
}

.van-swipe img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.category-section {
  background-color: white;
  padding: 16px 0;
}

.product-section {
  margin-top: 10px;
  background-color: white;
  padding: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.section-more {
  font-size: 14px;
  color: #999;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}
</style>
```
===FILE:front/store/src/views/ProductList.vue===
```vue
<template>
  <div class="product-list">
    <van-nav-bar title="商品列表" left-arrow />
    <van-search v-model="keyword" placeholder="搜索商品" shape="round" @search="handleSearch" />
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="onLoad">
        <div class="product-grid">
          <ProductCard v-for="product in productList" :key="product.id" :product="product" />
        </div>
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import ProductCard from '@/components/ProductCard.vue'
import { getProductList } from '@/api/product'

const route = useRoute()
const keyword = ref('')
const productList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)

const queryForm = reactive({
  keyword: '',
  category: '',
  page: 1,
  pageSize: 10
})

async function fetchProductList() {
  try {
    const res = await getProductList(queryForm)
    if (queryForm.page === 1) {
      productList.value = res.data.list || []
    } else {
      productList.value.push(...(res.data.list || []))
    }
    finished.value = productList.value.length >= (res.data.total || 0)
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function onLoad() {
  fetchProductList()
  queryForm.page++
}

function onRefresh() {
  queryForm.page = 1
  finished.value = false
  fetchProductList()
}

function handleSearch() {
  queryForm.keyword = keyword.value
  queryForm.page = 1
  finished.value = false
  productList.value = []
  onLoad()
}

onMounted(() => {
  queryForm.category = route.query.category || ''
  queryForm.keyword = route.query.keyword || ''
  keyword.value = queryForm.keyword
  onLoad()
})
</script>

<style scoped>
.product-list {
  width: 100%;
  min-height: 100%;
  background-color: #f5f5f5;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 12px;
}
</style>
```
===FILE:front/store/src/views/ProductDetail.vue===
```vue
<template>
  <div class="product-detail">
    <van-nav-bar title="商品详情" left-arrow />
    <van-swipe :autoplay="3000" indicator-color="white">
      <van-swipe-item v-for="(img, index) in product.images || ['https://fastly.jsdelivr.net/npm/@vant/assets/apple-1.jpeg']" :key="index">
        <img :src="img" alt="product" />
      </van-swipe-item>
    </van-swipe>
    <div class="product-info">
      <div class="price">
        <span class="symbol">¥</span>
        <span class="value">{{ product.price?.toFixed(2) || '0.00' }}</span>
      </div>
      <div class="name">{{ product.name || '加载中...' }}</div>
      <div class="desc">{{ product.description || '暂无描述' }}</div>
      <div class="extra">
        <span>库存：{{ product.stock || 0 }}</span>
        <span>销量：{{ product.sales || 0 }}</span>
      </div>
    </div>
    <div class="bottom-bar">
      <van-button icon="chat-o" size="small">客服</van-button>
      <van-button icon="shopping-cart-o" size="small" @click="goCart">购物车</van-button>
      <van-button type="warning" size="small" @click="handleAddCart">加入购物车</van-button>
      <van-button type="danger" size="small">立即购买</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { showToast, showConfirmDialog } from 'vant'
import { getProductDetail } from '@/api/product'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()
const product = ref({})

async function fetchProductDetail() {
  try {
    const res = await getProductDetail(route.params.id)
    product.value = res.data
  } catch (error) {
    console.error(error)
  }
}

function goCart() {
  router.push({ name: 'Cart' })
}

async function handleAddCart() {
  if (!userStore.isLogin) {
    try {
      await showConfirmDialog({
        title: '提示',
        message: '请先登录'
      })
      router.push({ name: 'Login', query: { redirect: route.fullPath } })
    } catch {
    }
    return
  }
  try {
    await cartStore.handleAddCart(product.value.id, 1)
    showToast('已加入购物车')
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  fetchProductDetail()
})
</script>

<style scoped>
.product-detail {
  width: 100%;
  min-height: 100%;
  padding-bottom: 60px;
  background-color: white;
}

.van-swipe img {
  width: 100%;
  height: 300px;
  object-fit: cover;
}

.product-info {
  padding: 16px;
}

.price {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 12px;
}

.symbol {
  font-size: 16px;
  color: #ff4444;
}

.value {
  font-size: 28px;
  font-weight: bold;
  color: #ff4444;
}

.name {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 16px;
}

.extra {
  display: flex;
  gap: 20px;
  font-size: 12px;
  color: #999;
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 50px;
  background-color: white;
  display: flex;
  align-items: center;
  justify-content: space-around;
  border-top: 1px solid #f0f0f0;
  padding: 0 16px;
  z-index: 100;
}
</style>
```
===FILE:front/store/src/views/Cart.vue===
```vue
<template>
  <div class="cart">
    <van-nav-bar title="购物车" />
    <van-checkbox v-model="cartStore.allChecked" @change="cartStore.toggleAllChecked">
      全选
    </van-checkbox>
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <div v-if="cartStore.cartList.length === 0" class="empty-cart">
        <van-empty description="购物车是空的" />
        <van-button type="primary" size="small" @click="goHome">去逛逛</van-button>
      </div>
      <div v-else class="cart-list">
        <div v-for="item in cartStore.cartList" :key="item.id" class="cart-item">
          <van-checkbox v-model="item.checked" @change="cartStore.toggleItemChecked(item.id)" />
          <img :src="item.product?.images?.[0] || 'https://fastly.jsdelivr.net/npm/@vant/assets/apple-1.jpeg'" alt="product" @click="goProductDetail(item.productId)" />
          <div class="item-info">
            <div class="item-name" @click="goProductDetail(item.productId)">{{ item.product?.name || '商品' }}</div>
            <div class="item-price">¥{{ item.product?.price?.toFixed(2) || '0.00' }}</div>
            <div class="item-actions">
              <van-stepper v-model="item.quantity" :min="1" :max="item.product?.stock || 999" @change="handleUpdateQuantity(item)" />
              <van-icon name="delete-o" size="20" @click="handleDelete(item.id)" />
            </div>
          </div>
        </div>
      </div>
    </van-pull-refresh>
    <div v-if="cartStore.cartList.length > 0" class="bottom-bar">
      <van-checkbox v-model="cartStore.allChecked" @change="cartStore.toggleAllChecked">
        全选
      </van-checkbox>
      <div class="total">
        <span>合计：</span>
        <span class="price">¥{{ cartStore.checkedPrice.toFixed(2) }}</span>
      </div>
      <van-button type="danger" :disabled="cartStore.checkedCount === 0" @click="goCheckout">
        结算({{ cartStore.checkedCount }})
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { showConfirmDialog, showToast } from 'vant'

const router = useRouter()
const cartStore = useCartStore()
const refreshing = ref(false)

async function onRefresh() {
  try {
    await cartStore.fetchCartList()
  } catch (error) {
    console.error(error)
  } finally {
    refreshing.value = false
  }
}

async function handleUpdateQuantity(item) {
  try {
    await cartStore.handleUpdateCart(item.id, item.quantity)
  } catch (error) {
    console.error(error)
  }
}

async function handleDelete(id) {
  try {
    await showConfirmDialog({
      title: '提示',
      message: '确定要删除这个商品吗？'
    })
    await cartStore.handleDeleteCart(id)
    showToast('删除成功')
  } catch {
  }
}

function goHome() {
  router.push({ name: 'Home' })
}

function goProductDetail(id) {
  router.push({ name: 'ProductDetail', params: { id } })
}

function goCheckout() {
  router.push({ name: 'Checkout' })
}

onMounted(() => {
  cartStore.fetchCartList().catch(() => {})
})
</script>

<style scoped>
.cart {
  width: 100%;
  min-height: 100%;
  padding-bottom: 60px;
  background-color: #f5f5f5;
}

.cart .van-checkbox {
  padding: 16px;
  background-color: white;
  margin-bottom: 10px;
}

.empty-cart {
  padding: 60px 20px;
  text-align: center;
}

.empty-cart .van-button {
  margin-top: 20px;
}

.cart-list {
  padding: 0 16px;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background-color: white;
  border-radius: 8px;
  margin-bottom: 12px;
}

.cart-item img {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  object-fit: cover;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.item-name {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-price {
  font-size: 16px;
  font-weight: bold;
  color: #ff4444;
}

.item-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 50px;
  background-color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-top: 1px solid #f0f0f0;
  z-index: 100;
}

.total {
  flex: 1;
  text-align: right;
  padding-right: 16px;
}

.price {
  font-size: 18px;
  font-weight: bold;
  color: #ff4444;
}
</style>
```
===FILE:front/store/src/views/Checkout.vue===
```vue
<template>
  <div class="checkout">
    <van-nav-bar title="结算" left-arrow />
    <div class="address-section" @click="goAddressList">
      <van-cell-group inset>
        <van-cell v-if="addressStore.defaultAddress" :title="`${addressStore.defaultAddress.name} ${addressStore.defaultAddress.phone}`" :value="addressStore.defaultAddress.detail" is-link />
        <van-cell v-else title="请选择收货地址" is-link />
      </van-cell-group>
    </div>
    <div class="product-section">
      <van-cell-group inset>
        <van-cell v-for="item in cartStore.checkedItems" :key="item.id">
          <template #icon>
            <img :src="item.product?.images?.[0] || 'https://fastly.jsdelivr.net/npm/@vant/assets/apple-1.jpeg'" alt="product" class="product-img" />
          </template>
          <template #title>
            <div class="product-name">{{ item.product?.name || '商品' }}</div>
          </template>
          <template #label>
            <div class="product-meta">
              <span>¥{{ item.product?.price?.toFixed(2) || '0.00' }}</span>
              <span>x{{ item.quantity }}</span>
            </div>
          </template>
        </van-cell>
      </van-cell-group>
    </div>
    <div class="price-section">
      <van-cell-group inset>
        <van-cell title="商品金额" :value="`¥${cartStore.checkedPrice.toFixed(2)}`" />
        <van-cell title="运费" value="¥0.00" />
        <van-cell title="实付金额" :value="`¥${cartStore.checkedPrice.toFixed(2)}`" />
      </van-cell-group>
    </div>
    <div class="bottom-bar">
      <div class="total">
        <span>实付：</span>
        <span class="price">¥{{ cartStore.checkedPrice.toFixed(2) }}</span>
      </div>
      <van-button type="danger" :disabled="!addressStore.defaultAddress || cartStore.checkedItems.length === 0" @click="handleSubmitOrder">提交订单</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useAddressStore } from '@/stores/address'
import { showToast, showLoading, hideLoading } from 'vant'
import { createOrder } from '@/api/order'

const router = useRouter()
const cartStore = useCartStore()
const addressStore = useAddressStore()

async function handleSubmitOrder() {
  showLoading({ message: '提交中...' })
  try {
    const orderItems = cartStore.checkedItems.map(item => ({
      productId: item.productId,
      quantity: item.quantity
    }))
    const res = await createOrder({
      addressId: addressStore.defaultAddress.id,
      items: orderItems
    })
    hideLoading()
    showToast('订单提交成功')
    await cartStore.resetCheckedItems()
    router.push({ name: 'OrderDetail', params: { id: res.data.id } })
  } catch (error) {
    hideLoading()
    console.error(error)
  }
}

function goAddressList() {
  router.push({ name: 'AddressList', query: { select: '1' } })
}

onMounted(() => {
  addressStore.fetchAddressList().catch(() => {})
})
</script>

<style scoped>
.checkout {
  width: 100%;
  min-height: 100%;
  padding-bottom: 60px;
  background-color: #f5f5f5;
}

.address-section, .product-section, .price-section {
  margin-top: 10px;
}

.product-img {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  object-fit: cover;
}

.product-name {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;
  font-size: 14px;
  color: #999;
}

.product-meta span:first-child {
  color: #ff4444;
  font-weight: bold;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 50px;
  background-color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-top: 1px solid #f0f0f0;
  z-index: 100;
}

.total {
  flex: 1;
  text-align: right;
  padding-right: 16px;
}

.price {
  font-size: 20px;
  font-weight: bold;
  color: #ff4444;
}
</style>
```
===FILE:front/store/src/views/AddressList.vue===
```vue
<template>
  <div class="address-list">
    <van-nav-bar title="收货地址" left-arrow>
      <template #right>
        <van-icon name="plus" size="20" @click="goAdd" />
      </template>
    </van-nav-bar>
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="onLoad">
        <div v-if="addressStore.addressList.length === 0" class="empty-address">
          <van-empty description="暂无收货地址" />
          <van-button type="primary" size="small" @click="goAdd">添加地址</van-button>
        </div>
        <div v-else class="address-card-list">
          <van-cell-group inset v-for="address in addressStore.addressList" :key="address.id">
            <van-cell @click="handleSelect(address)">
              <template #title>
                <div class="address-header">
                  <span class="name">{{ address.name }}</span>
                  <span class="phone">{{ address.phone }}</span>
                  <van-tag v-if="address.isDefault" type="primary" size="small">默认</van-tag>
                </div>
              </template>
              <template #label>
                <div class="address-detail">{{ address.province }}{{ address.city }}{{ address.district }}{{ address.detail }}</div>
              </template>
            </van-cell>
            <van-cell>
              <template #right-icon>
                <div class="address-actions">
                  <van-button size="small" plain type="primary" @click="goEdit(address)">编辑</van-button>
                  <van-button size="small" plain type="danger" @click="handleDelete(address.id)">删除</van-button>
                  <van-button v-if="!address.isDefault" size="small" plain type="warning" @click="handleSetDefault(address.id)">设为默认</van-button>
                </div>
              </template>
            </van-cell>
          </van-cell-group>
        </div>
      </van-list>
    </van-pull-refresh>
    <AddressForm v-model:visible="formVisible" :address="currentAddress" @success="onFormSuccess" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAddressStore } from '@/stores/address'
import { showConfirmDialog, showToast } from 'vant'
import AddressForm from '@/components/AddressForm.vue'

const route = useRoute()
const router = useRouter()
const addressStore = useAddressStore()
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(true)
const formVisible = ref(false)
const currentAddress = ref(null)

function onLoad() {
  loading.value = false
}

async function onRefresh() {
  try {
    await addressStore.fetchAddressList()
  } catch (error) {
    console.error(error)
  } finally {
    refreshing.value = false
  }
}

function goAdd() {
  currentAddress.value = null
  formVisible.value = true
}

function goEdit(address) {
  currentAddress.value = { ...address }
  formVisible.value = true
}

async function handleDelete(id) {
  try {
    await showConfirmDialog({
      title: '提示',
      message: '确定要删除这个地址吗？'
    })
    await addressStore.handleDeleteAddress(id)
    showToast('删除成功')
  } catch {
  }
}

async function handleSetDefault(id) {
  try {
    await addressStore.handleSetDefaultAddress(id)
    showToast('设置成功')
  } catch (error) {
    console.error(error)
  }
}

function handleSelect(address) {
  if (route.query.select === '1') {
    addressStore.handleSetDefaultAddress(address.id).catch(() => {})
    router.back()
  }
}

function onFormSuccess() {
  formVisible.value = false
  onRefresh()
}

onMounted(() => {
  onRefresh()
})
</script>

<style scoped>
.address-list {
  width: 100%;
  min-height: 100%;
  padding-bottom: 20px;
  background-color: #f5f5f5;
}

.empty-address {
  padding: 60px 20px;
  text-align: center;
}

.empty-address .van-button {
  margin-top: 20px;
}

.address-card-list {
  padding: 0 16px;
}

.address-card-list .van-cell-group {
  margin-bottom: 12px;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.phone {
  font-size: 14px;
  color: #666;
}

.address-detail {
  font-size: 14px;
  color: #666;
  margin-top: 8px;
}

.address-actions {
  display: flex;
  gap: 12px;
}
</style>
```
===FILE:front/store/src/views/OrderList.vue===
```vue
<template>
  <div class="order-list">
    <van-nav-bar title="我的订单" left-arrow />
    <van-tabs v-model:active="activeTab" sticky>
      <van-tab title="全部" name=""></van-tab>
      <van-tab title="待付款" name="0"></van-tab>
      <van-tab title="待发货" name="1"></van-tab>
      <van-tab title="已发货" name="2"></van-tab>
      <van-tab title="已完成" name="3"></van-tab>
    </van-tabs>
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="onLoad">
        <div v-if="orderList.length === 0" class="empty-order">
          <van-empty description="暂无订单" />
        </div>
        <div v-else class="order-card-list">
          <van-cell-group inset v-for="order in orderList" :key="order.id">
            <van-cell>
              <template #title>
                <div class="order-header">
                  <span class="order-no">订单号：{{ order.orderNo }}</span>
                  <van-tag :type="statusTypeMap[order.status]">{{ statusTextMap[order.status] }}</van-tag>
                </div>
              </template>
            </van-cell>
            <van-cell v-for="item in order.items" :key="item.id" @click="goOrderDetail(order.id)">
              <template #icon>
                <img :src="item.product?.images?.[0] || 'https://fastly.jsdelivr.net/npm/@vant/assets/apple-1.jpeg'" alt="product" class="product-img" />
              </template>
              <template #title>
                <div class="product-name">{{ item.product?.name || '商品' }}</div>
              </template>
              <template #label>
                <div class="product-meta">
                  <span>¥{{ item.price?.toFixed(2) || '0.00' }}</span>
                  <span>x{{ item.quantity }}</span>
                </div>
              </template>
            </van-cell>
            <van-cell>
              <template #title>
                <div class="order-footer">
                  <span>共{{ order.totalQuantity }}件商品，实付：</span>
                  <span class="price">¥{{ order.totalAmount?.toFixed(2) || '0.00' }}</span>
                </div>
              </template>
              <template #right-icon>
                <div class="order-actions">
                  <van-button v-if="order.status === 0" size="small" type="primary" @click="handlePay(order.id)">付款</van-button>
                  <van-button v-if="order.status === 0" size="small" plain type="default" @click="handleCancel(order.id)">取消</van-button>
                  <van-button v-if="order.status === 2" size="small" type="success" @click="handleConfirm(order.id)">确认收货</van-button>
                  <van-button size="small" plain type="primary" @click="goOrderDetail(order.id)">查看详情</van-button>
                </div>
              </template>
            </van-cell>
          </van-cell-group>
        </div>
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { getOrderList, cancelOrder, payOrder, confirmOrder } from '@/api/order'

const router = useRouter()
const activeTab = ref('')
const orderList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)

const statusTypeMap = {
  0: 'info',
  1: 'warning',
  2: 'primary',
  3: 'success',
  4: 'danger'
}

const statusTextMap = {
  0: '待付款',
  1: '待发货',
  2: '已发货',
  3: '已完成',
  4: '已取消'
}

const queryForm = reactive({
  status: '',
  page: 1,
  pageSize: 10
})

async function fetchOrderList() {
  try {
    const res = await getOrderList(queryForm)
    if (queryForm.page === 1) {
      orderList.value = res.data.list || []
    } else {
      orderList.value.push(...(res.data.list || []))
    }
    finished.value = orderList.value.length >= (res.data.total || 0)
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function onLoad() {
  fetchOrderList()
  queryForm.page++
}

function onRefresh() {
  queryForm.page = 1
  finished.value = false
  fetchOrderList()
}

async function handleCancel(id) {
  try {
    await showConfirmDialog({
      title: '提示',
      message: '确定要取消这个订单吗？'
    })
    await cancelOrder(id)
    showToast('订单已取消')
    onRefresh()
  } catch {
  }
}

async function handlePay(id) {
  try {
    await payOrder(id, { payMethod: 1 })
    showToast('支付成功')
    onRefresh()
  } catch (error) {
    console.error(error)
  }
}

async function handleConfirm(id) {
  try {
    await showConfirmDialog({
      title: '提示',
      message: '确定已收到商品？'
    })
    await confirmOrder(id)
    showToast('确认收货成功')
    onRefresh()
  } catch {
  }
}

function goOrderDetail(id) {
  // router.push({ name: 'OrderDetail', params: { id } })
  showToast('详情页开发中')
}

watch(activeTab, (val) => {
  queryForm.status = val
  queryForm.page = 1
  finished.value = false
  orderList.value = []
  onLoad()
})

onMounted(() => {
  onLoad()
})
</script>

<style scoped>
.order-list {
  width: 100%;
  min-height: 100%;
  background-color: #f5f5f5;
}

.empty-order {
  padding: 60px 20px;
  text-align: center;
}

.order-card-list {
  padding: 0 16px;
}

.order-card-list .van-cell-group {
  margin-bottom: 12px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-no {
  font-size: 12px;
  color: #999;
}

.product-img {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  object-fit: cover;
}

.product-name {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;
  font-size: 14px;
  color: #999;
}

.product-meta span:first-child {
  color: #ff4444;
  font-weight: bold;
}

.order-footer {
  font-size: 14px;
  color: #333;
}

.order-footer .price {
  font-size: 18px;
  font-weight: bold;
  color: #ff4444;
}

.order-actions {
  display: flex;
  gap: 12px;
}
</style>
```
===FILE:front/store/src/views/User.vue===
```vue
<template>
  <div class="user">
    <div class="user-header">
      <div class="user-info" @click="userStore.isLogin ? null : goLogin">
        <van-avatar :size="60" :src="userStore.userInfo.avatar || ''">
          <van-icon name="user-o" size="30" />
        </van-avatar>
        <div class="info-text">
          <div class="username">{{ userStore.userInfo.nickname || userStore.userInfo.username || '点击登录' }}</div>
          <div class="phone" v-if="userStore.isLogin">{{ userStore.userInfo.phone || '未绑定手机号' }}</div>
        </div>
      </div>
    </div>
    <div class="order-section">
      <van-cell-group inset>
        <van-cell title="我的订单" is-link @click="goOrderList">
          <template #right-icon>
            <span class="more-text">全部订单 ></span>
          </template>
        </van-cell>
        <van-cell-center>
          <div class="order-grid">
            <div class="order-item" @click="goOrderList('0')">
              <van-icon name="pending-payment" size="24" />
              <span>待付款</span>
            </div>
            <div class="order-item" @click="goOrderList('1')">
              <van-icon name="logistics" size="24" />
              <span>待发货</span>
            </div>
            <div class="order-item" @click="goOrderList('2')">
              <van-icon name="goods-collect-o" size="24" />
              <span>已发货</span>
            </div>
            <div class="order-item" @click="goOrderList('3')">
              <van-icon name="description" size="24" />
              <span>已完成</span>
            </div>
          </div>
        </van-cell-center>
      </van-cell-group>
    </div>
    <div class="service-section">
      <van-cell-group inset>
        <van-cell title="收货地址" icon="location-o" is-link @click="goAddressList" />
        <van-cell title="联系客服" icon="service-o" is-link />
        <van-cell title="关于我们" icon="info-o" is-link />
      </van-cell-group>
    </div>
    <div class="logout-section" v-if="userStore.isLogin">
      <van-button block plain type="danger" @click="handleLogout">退出登录</van-button>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { showConfirmDialog, showToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

function goLogin() {
  router.push({ name: 'Login' })
}

function goOrderList(status = '') {
  router.push({ name: 'OrderList', query: { status } })
}

function goAddressList() {
  router.push({ name: 'AddressList' })
}

async function handleLogout() {
  try {
    await showConfirmDialog({
      title: '提示',
      message: '确定要退出登录吗？'
    })
    await userStore.logout()
    cartStore.cartList.value = []
    showToast('退出成功')
  } catch {
  }
}

onMounted(() => {
  if (userStore.isLogin && !userStore.userInfo.id) {
    userStore.getUserInfo().catch(() => {})
  }
})
</script>

<style scoped>
.user {
  width: 100%;
  min-height: 100%;
  background-color: #f5f5f5;
  padding-bottom: 20px;
}

.user-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px 30px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.info-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.username {
  font-size: 18px;
  font-weight: bold;
  color: white;
}

.phone {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.order-section, .service-section {
  margin-top: 10px;
}

.more-text {
  font-size: 14px;
  color: #999;
}

.order-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  padding: 16px 0;
}

.order-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #333;
}

.logout-section {
  margin-top: 20px;
  padding: 0 16px;
}
</style>
```
===FILE:front/store/src/components/ProductCard.vue===
```vue
<template>
  <div class="product-card" @click="goDetail">
    <img :src="product.images?.[0] || 'https://fastly.jsdelivr.net/npm/@vant/assets/apple-1.jpeg'" alt="product" />
    <div class="card-info">
      <div class="card-name">{{ product.name || '商品' }}</div>
      <div class="card-price">
        <span class="symbol">¥</span>
        <span class="value">{{ product.price?.toFixed(2) || '0.00' }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  product: {
    type: Object,
    default: () => ({})
  }
})

const router = useRouter()

function goDetail() {
  router.push({ name: 'ProductDetail', params: { id: props.product.id } })
}
</script>

<style scoped>
.product-card {
  background-color: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.product-card img {
  width: 100%;
  height: 160px;
  object-fit: cover;
}

.card-info {
  padding: 12px;
}

.card-name {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 8px;
}

.card-price {
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.card-price .symbol {
  font-size: 12px;
  color: #ff4444;
}

.card-price .value {
  font-size: 18px;
  font-weight: bold;
  color: #ff4444;
}
</style>
```
===FILE:front/store/src/components/AddressForm.vue===
```vue
<template>
  <van-popup v-model:visible="visible" position="bottom" round :style="{ height: '80%' }">
    <van-nav-bar title="编辑地址" left-arrow @click-left="visible = false">
      <template #right>
        <van-button type="primary" size="small" plain @click="handleSubmit">保存</van-button>
      </template>
    </van-nav-bar>
    <van-form ref="formRef" @submit="handleSubmit">
      <van-cell-group inset>
        <van-field
          v-model="form.name"
          name="name"
          label="收货人"
          placeholder="请输入收货人姓名"
          :rules="[{ required: true, message: '请输入收货人姓名' }]"
        />
        <van-field
          v-model="form.phone"
          name="phone"
          label="手机号"
          placeholder="请输入手机号"
          :rules="[{ required: true, message: '请输入手机号' }, { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' }]"
        />
        <van-field
          v-model="areaValue"
          name="area"
          label="所在地区"
          placeholder="请选择所在地区"
          readonly
          is-link
          @click="showAreaPicker = true"
          :rules="[{ required: true, message: '请选择所在地区' }]"
        />
        <van-field
          v-model="form.detail"
          name="detail"
          label="详细地址"
          type="textarea"
          placeholder="请输入详细地址"
          :rules="[{ required: true, message: '请输入详细地址' }]"
        />
        <van-cell title="设为默认">
          <template #right-icon>
            <van-switch v-model="form.isDefault" />
          </template>
        </van-cell>
      </van-cell-group>
    </van-form>
    <van-area
      v-model:areaValue"
      :area-list="areaList"
      :visible="showAreaPicker"
      @confirm="onAreaConfirm"
      @cancel="showAreaPicker = false"
    />
  </van-popup>
</template>

<script setup>
import { ref, watch, reactive } from 'vue'
import { showToast } from 'vant'
import { addAddress, updateAddress } from '@/api/address'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  address: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:visible', 'success'])

const formRef = ref(null)
const showAreaPicker = ref(false)
const areaValue = ref('')
const areaList = ref({
  province_list: {
    110000: '北京市',
    310000: '上海市',
    440000: '广东省'
  },
  city_list: {
    110100: '北京市',
    310100: '上海市',
    440100: '广州市',
    440300: '深圳市'
  },
  county_list: {
    110101: '东城区',
    110102: '西城区',
    310101: '黄浦区',
    310104: '徐汇区',
    440103: '荔湾区',
    440104: '越秀区',
    440303: '罗湖区',
    440304: '福田区'
  }
})

const form = reactive({
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false
})

function onAreaConfirm({ selectedOptions }) {
  form.province = selectedOptions[0]?.text || ''
  form.city = selectedOptions[1]?.text || ''
  form.district = selectedOptions[2]?.text || ''
  areaValue.value = selectedOptions.map(option => option.text).join('')
  showAreaPicker.value = false
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (props.address) {
          await updateAddress(props.address.id, form)
        } else {
          await addAddress(form)
        }
        showToast('保存成功')
        emit('success')
      } catch (error) {
        console.error(error)
      }
    }
  })
}

watch(() => props.visible, (val) => {
  if (val) {
    if (props.address) {
      Object.assign(form, props.address)
      areaValue.value = `${form.province}${form.city}${form.district}`
    } else {
      Object.assign(form, {
        name: '',
        phone: '',
        province: '',
        city: '',
        district: '',
        detail: '',
        isDefault: false
      })
      areaValue.value = ''
    }
  }
})

watch(() => props.address, (val) => {
  if (val && props.visible) {
    Object.assign(form, val)
    areaValue.value = `${form.province}${form.city}${form.district}`
  }
})
</script>

<style scoped>
</style>
```
===FILE:front/admin/src/assets/index.css===
```css
```
===FILE:front/store/src/assets/index.css===
```css
```
===FILE:front/admin/index.html===
```html
<!DOCTYPE html>
<html lang="zh-CN">
  <head>
    <meta charset="UTF-8" />
    <link rel="icon" type="image/svg+xml" href="/vite.svg" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>电商后台管理系统</title>
  </head>
  <body>
    <div id="app"></div>
    <script type="module" src="/src/main.js"></script>
  </body>
</html>
```
===FILE:front/store/index.html===
```html
<!DOCTYPE html>
<html lang="zh-CN">
  <head>
    <meta charset="UTF-8" />
    <link rel="icon" type="image/svg+xml" href="/vite.svg" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
    <title>电商商城</title>
  </head>
  <body>
    <div id="app"></div>
    <script type="module" src="/src/main.js"></script>
  </body>
</html>
```