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