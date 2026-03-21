import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/store/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/pos',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'pos',
        name: 'Pos',
        component: () => import('@/views/pos-layout/index.vue'),
        meta: { requiresAuth: true, title: 'POS收银' }
      },
      {
        path: 'sku-init',
        name: 'SkuInit',
        component: () => import('@/views/sku-init/index.vue'),
        meta: { requiresAuth: true, title: 'SKU初始化', roles: ['admin'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const token = getToken()
  const userStore = useUserStore()

  if (to.meta.requiresAuth) {
    if (!token) {
      next('/login')
      return
    }
    if (!userStore.userInfo) {
      try {
        await userStore.getUserInfo()
      } catch (error) {
        next('/login')
        return
      }
    }
    if (to.meta.roles && !to.meta.roles.includes(userStore.userInfo?.role)) {
      next('/pos')
      return
    }
    next()
  } else {
    if (token && to.path === '/login') {
      next('/')
    } else {
      next()
    }
  }
})

export default router