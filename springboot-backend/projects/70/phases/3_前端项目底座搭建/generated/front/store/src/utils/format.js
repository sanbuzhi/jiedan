import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import relativeTime from 'dayjs/plugin/relativeTime'

dayjs.locale('zh-cn')
dayjs.extend(relativeTime)

export function formatDate(date, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!date) return ''
  return dayjs(date).format(format)
}

export function formatRelativeTime(date) {
  if (!date) return ''
  return dayjs(date).fromNow()
}

export function formatPrice(price) {
  if (typeof price !== 'number' || isNaN(price)) return '¥0.00'
  return `¥${price.toFixed(2)}`
}

export function formatNumber(num, decimals = 0) {
  if (typeof num !== 'number' || isNaN(num)) return '0'
  return num.toFixed(decimals).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

export function formatPhone(phone) {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}