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