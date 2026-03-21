import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/admin/auth/login',
    method: 'post',
    data
  })
}

export function logout() {
  return request({
    url: '/admin/auth/logout',
    method: 'post'
  })
}

export function getUserInfo() {
  return request({
    url: '/admin/user/info',
    method: 'get'
  })
}

export function getUserList(params) {
  return request({
    url: '/admin/user/list',
    method: 'get',
    params
  })
}

export function updateUserStatus(id, status) {
  return request({
    url: `/admin/user/${id}/status`,
    method: 'put',
    data: { status }
  })
}

export function deleteUser(id) {
  return request({
    url: `/admin/user/${id}`,
    method: 'delete'
  })
}