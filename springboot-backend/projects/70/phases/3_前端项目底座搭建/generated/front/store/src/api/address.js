import request from '@/utils/request'

export function getAddressList() {
  return request({
    url: '/store/addresses',
    method: 'get'
  })
}

export function getAddressDetail(id) {
  return request({
    url: `/store/addresses/${id}`,
    method: 'get'
  })
}

export function addAddress(data) {
  return request({
    url: '/store/addresses',
    method: 'post',
    data
  })
}

export function updateAddress(id, data) {
  return request({
    url: `/store/addresses/${id}`,
    method: 'put',
    data
  })
}

export function deleteAddress(id) {
  return request({
    url: `/store/addresses/${id}`,
    method: 'delete'
  })
}

export function setDefaultAddress(id) {
  return request({
    url: `/store/addresses/${id}/default`,
    method: 'post'
  })
}