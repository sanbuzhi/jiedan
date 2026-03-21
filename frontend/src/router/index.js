import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/client',
    redirect: '/client/guide'
  },
  {
    path: '/client/guide',
    name: 'ClientGuide',
    component: () => import('@/mobile/views/GuideView.vue')
  },
  {
    path: '/client/login',
    name: 'ClientLogin',
    component: () => import('@/mobile/views/LoginView.vue')
  },
  {
    path: '/client/home',
    name: 'ClientHome',
    component: () => import('@/mobile/views/HomeView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/client/profile',
    name: 'ClientProfile',
    component: () => import('@/mobile/views/ProfileView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/client/points',
    name: 'ClientPoints',
    component: () => import('@/mobile/views/PointsView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/client/exchange',
    name: 'ClientExchange',
    component: () => import('@/mobile/views/ExchangeView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/client/requirement/step',
    name: 'ClientRequirementStep',
    component: () => import('@/mobile/views/RequirementStepView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/client/requirement/step_all',
    name: 'ClientRequirementStepAll',
    component: () => import('@/mobile/views/RequirementStepAllView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/client/requirement/ui_gallery',
    name: 'ClientUiGallery',
    component: () => import('@/mobile/views/UiGalleryView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/client/requirement/approve',
    name: 'ClientApprove',
    component: () => import('@/mobile/views/ApproveView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/client/requirement/suggestion',
    name: 'ClientSuggestion',
    component: () => import('@/mobile/views/SuggestionView.vue'),
    meta: { requiresAuth: true }
  },
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

  const token = localStorage.getItem('token')

  if (to.meta.requiresAuth && !token) {
    if (to.path.startsWith('/client/')) {
      next('/client/login')
    } else {
      next('/login')
    }
    return
  }

  if ((to.path === '/login' || to.path === '/client/login') && token) {
    if (to.path.startsWith('/client/')) {
      next('/client/home')
    } else {
      next('/')
    }
    return
  }

  if (to.path === '/client/' || to.path === '/client') {
    next('/client/guide')
    return
  }

  if (token && !userStore.userInfo && to.meta.requiresAuth) {
    try {
      await userStore.fetchUserInfo()
    } catch (error) {
      localStorage.removeItem('token')
      if (to.path.startsWith('/client/')) {
        next('/client/login')
      } else {
        next('/login')
      }
      return
    }
  }

  next()
})

export default router