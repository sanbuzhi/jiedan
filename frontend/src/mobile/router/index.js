import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'MobileGuide',
    redirect: '/guide'
  },
  {
    path: '/guide',
    name: 'MobileGuide',
    component: () => import('@/mobile/views/GuideView.vue')
  },
  {
    path: '/login',
    name: 'MobileLogin',
    component: () => import('@/mobile/views/LoginView.vue')
  },
  {
    path: '/home',
    name: 'MobileHome',
    component: () => import('@/mobile/views/HomeView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'MobileProfile',
    component: () => import('@/mobile/views/ProfileView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/points',
    name: 'MobilePoints',
    component: () => import('@/mobile/views/PointsView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/exchange',
    name: 'MobileExchange',
    component: () => import('@/mobile/views/ExchangeView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/requirement/step',
    name: 'MobileRequirementStep',
    component: () => import('@/mobile/views/RequirementStepView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/requirement/step_all',
    name: 'MobileRequirementStepAll',
    component: () => import('@/mobile/views/RequirementStepAllView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/requirement/ui_gallery',
    name: 'MobileUiGallery',
    component: () => import('@/mobile/views/UiGalleryView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/requirement/approve',
    name: 'MobileApprove',
    component: () => import('@/mobile/views/ApproveView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/requirement/suggestion',
    name: 'MobileSuggestion',
    component: () => import('@/mobile/views/SuggestionView.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHashHistory('/m'),
  routes
})

router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token')

  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }

  if (to.path === '/login' && token) {
    next('/home')
    return
  }

  if (to.path === '/' && token) {
    next('/home')
    return
  }

  if (to.path === '/' && !token) {
    next('/login')
    return
  }

  next()
})

export default router