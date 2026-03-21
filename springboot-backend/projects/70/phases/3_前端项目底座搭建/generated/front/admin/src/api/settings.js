import request from '@/utils/request'

export function getSettings() {
  return request({
    url: '/admin/settings',
    method: 'get'
  })
}

export function updateSettings(data) {
  return request({
    url: '/admin/settings',
    method: 'put',
    data
  })
}

export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/admin/upload/image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}