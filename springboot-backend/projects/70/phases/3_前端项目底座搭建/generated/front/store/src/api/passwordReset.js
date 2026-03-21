import request from '@/utils/request'

export function sendResetCode(phone) {
  return request({
    url: '/user/password-reset/send-code',
    method: 'post',
    data: { phone }
  })
}

export function resetPassword(data) {
  return request({
    url: '/user/password-reset',
    method: 'post',
    data
  })
}