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