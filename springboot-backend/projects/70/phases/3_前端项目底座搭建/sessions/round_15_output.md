===FILE:front/admin/src/utils/format.js===
```javascript
// 金额格式化
export const formatMoney = (value, decimals = 2, symbol = '¥') => {
  if (isNaN(value)) return `${symbol}0.00`
  const fixed = Number(value).toFixed(decimals)
  return `${symbol}${fixed}`
}

// 日期时间格式化
export const formatDateTime = (timestamp, format = 'YYYY-MM-DD HH:mm:ss') => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

// 文件大小格式化
export const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${(bytes / Math.pow(k, i)).toFixed(2)} ${sizes[i]}`
}
```

===FILE:front/store/src/utils/format.js===
```javascript
// 金额格式化
export const formatMoney = (value, decimals = 2, symbol = '¥') => {
  if (isNaN(value)) return `${symbol}0.00`
  const fixed = Number(value).toFixed(decimals)
  return `${symbol}${fixed}`
}

// 日期时间格式化
export const formatDateTime = (timestamp, format = 'YYYY-MM-DD HH:mm') => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}
```