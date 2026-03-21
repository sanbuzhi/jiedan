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