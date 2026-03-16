import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/material-generator',
    name: 'MaterialGenerator',
    component: () => import('@/views/MaterialGenerator.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/material-library',
    name: 'MaterialLibrary',
    component: () => import('@/views/MaterialLibrary.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/platform-accounts',
    name: 'PlatformAccounts',
    component: () => import('@/views/PlatformAccounts.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/publish-tasks',
    name: 'PublishTasks',
    component: () => import('@/views/PublishTasks.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/publish-history',
    name: 'PublishHistory',
    component: () => import('@/views/PublishHistory.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/statistics',
    name: 'Statistics',
    component: () => import('@/views/Statistics.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/referral',
    name: 'Referral',
    component: () => import('@/views/Referral.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/referral-tree',
    name: 'ReferralTree',
    component: () => import('@/views/ReferralTree.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/points',
    name: 'Points',
    component: () => import('@/views/Points.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true },
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue')
      },
      {
        path: 'requirements',
        name: 'AdminRequirements',
        component: () => import('@/views/admin/RequirementManagement.vue')
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('@/views/admin/OrderManagement.vue')
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/UserManagement.vue')
      },
      {
        path: 'ab-test',
        name: 'AdminABTest',
        component: () => import('@/views/admin/ABTest.vue')
      },
      {
        path: 'optimization',
        name: 'AdminOptimization',
        component: () => import('@/views/admin/Optimization.vue')
      },
      {
        path: 'config',
        name: 'AdminConfig',
        component: () => import('@/views/admin/SystemConfig.vue')
      },
      {
        path: 'materials',
        name: 'AdminMaterials',
        component: () => import('@/views/admin/MaterialManagement.vue')
      },
      {
        path: 'ai-strategy',
        name: 'AdminAiStrategy',
        component: () => import('@/views/admin/AiStrategy.vue')
      },
      {
        path: 'feedback-shadow',
        name: 'AdminFeedbackShadow',
        component: () => import('@/views/admin/FeedbackShadowMonitor.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  // 从 localStorage 获取 token（确保页面刷新后也能正确判断）
  const token = localStorage.getItem('token')
  
  if (to.meta.requiresAuth && !token) {
    // 未登录且访问需要登录的页面，跳转到登录页
    next('/login')
    return
  }
  
  if (to.path === '/login' && token) {
    // 已登录但访问登录页，跳转到首页
    next('/')
    return
  }
  
  // 已登录但 store 中没有用户信息，尝试获取
  if (token && !userStore.userInfo && to.meta.requiresAuth) {
    try {
      await userStore.fetchUserInfo()
    } catch (error) {
      // 获取用户信息失败，清除 token 并跳转到登录页
      localStorage.removeItem('token')
      next('/login')
      return
    }
  }
  
  next()
})

export default router
