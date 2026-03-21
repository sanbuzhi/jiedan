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