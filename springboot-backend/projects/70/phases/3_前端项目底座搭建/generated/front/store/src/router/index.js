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