import request from '@/utils/request'

export function getCategoryTree() {
  return request({
    url: '/admin/category/tree',
    method: 'get'
  })
}

export function getCategoryList(params) {
  return request({
    url: '/admin/category/list',
    method: 'get',
    params
  })
}

export function getCategoryDetail(id) {
  return request({
    url: `/admin/category/${id}`,
    method: 'get'
  })
}

export function createCategory(data) {
  return request({
    url: '/admin/category',
    method: 'post',
    data
  })
}

export function updateCategory(id, data) {
  return request({
    url: `/admin/category/${id}`,
    method: 'put',
    data
  })
}

export function deleteCategory(id) {
  return request({
    url: `/admin/category/${id}`,
    method: 'delete'
  })
}