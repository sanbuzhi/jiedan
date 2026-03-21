import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import relativeTime from 'dayjs/plugin/relativeTime'

// 配置dayjs
dayjs.locale('zh-cn')
dayjs.extend(relativeTime)

/**
 * 格式化日期时间
 * @param date 日期对象/时间戳/字符串
 * @param format 格式化字符串，默认YYYY-MM-DD HH:mm:ss
 * @returns 格式化后的字符串
 */
export const formatDateTime = (
  date: dayjs.ConfigType,
  format: string = 'YYYY-MM-DD HH:mm:ss'
): string => {
  if (!date) return ''
  return dayjs(date).format(format)
}

/**
 * 格式化日期
 * @param date 日期对象/时间戳/字符串
 * @param format 格式化字符串，默认YYYY-MM-DD
 * @returns 格式化后的字符串
 */
export const formatDate = (
  date: dayjs.ConfigType,
  format: string = 'YYYY-MM-DD'
): string => {
  return formatDateTime(date, format)
}

/**
 * 格式化时间
 * @param date 日期对象/时间戳/字符串
 * @param format 格式化字符串，默认HH:mm:ss
 * @returns 格式化后的字符串
 */
export const formatTime = (
  date: dayjs.ConfigType,
  format: string = 'HH:mm:ss'
): string => {
  return formatDateTime(date, format)
}

/**
 * 获取相对时间
 * @param date 日期对象/时间戳/字符串
 * @returns 相对时间字符串，如"5分钟前"
 */
export const formatRelativeTime = (date: dayjs.ConfigType): string => {
  if (!date) return ''
  return dayjs(date).fromNow()
}

/**
 * 判断是否是今天
 * @param date 日期对象/时间戳/字符串
 * @returns 是否是今天
 */
export const isToday = (date: dayjs.ConfigType): boolean => {
  return dayjs(date).isSame(dayjs(), 'day')
}

/**
 * 判断是否是昨天
 * @param date 日期对象/时间戳/字符串
 * @returns 是否是昨天
 */
export const isYesterday = (date: dayjs.ConfigType): boolean => {
  return dayjs(date).isSame(dayjs().subtract(1, 'day'), 'day')
}

export default dayjs