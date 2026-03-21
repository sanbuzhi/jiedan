我将继续生成项目中可能缺失的关键文件。让我先检查并生成一些常见的配置和辅助文件。

===FILE:front/admin/.gitignore===
```
# Logs
logs
*.log
npm-debug.log*
yarn-debug.log*
yarn-error.log*
pnpm-debug.log*
lerna-debug.log*

node_modules
dist
dist-ssr
*.local

# Editor directories and files
.vscode/*
!.vscode/extensions.json
.idea
.DS_Store
*.suo
*.ntvs*
*.njsproj
*.sln
*.sw?
```

===FILE:front/store/.gitignore===
```
# Logs
logs
*.log
npm-debug.log*
yarn-debug.log*
yarn-error.log*
pnpm-debug.log*
lerna-debug.log*

node_modules
dist
dist-ssr
*.local

# Editor directories and files
.vscode/*
!.vscode/extensions.json
.idea
.DS_Store
*.suo
*.ntvs*
*.njsproj
*.sln
*.sw?
```

===FILE:front/admin/src/assets/base.css===
```css
/* 全局基础样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  width: 100%;
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

a {
  text-decoration: none;
  color: inherit;
}

ul, ol {
  list-style: none;
}

img {
  max-width: 100%;
  height: auto;
}

/* 布局类 */
.flex {
  display: flex;
}

.flex-col {
  flex-direction: column;
}

.items-center {
  align-items: center;
}

.justify-center {
  justify-content: center;
}

.justify-between {
  justify-content: space-between;
}

.flex-1 {
  flex: 1;
}

/* 间距类 */
.mt-4 {
  margin-top: 16px;
}

.mb-4 {
  margin-bottom: 16px;
}

.ml-4 {
  margin-left: 16px;
}

.mr-4 {
  margin-right: 16px;
}

.p-4 {
  padding: 16px;
}

/* 文本类 */
.text-center {
  text-align: center;
}

.text-right {
  text-align: right;
}

.font-bold {
  font-weight: bold;
}

/* 颜色类 */
.text-primary {
  color: #409eff;
}

.text-success {
  color: #67c23a;
}

.text-warning {
  color: #e6a23c;
}

.text-danger {
  color: #f56c6c;
}
```

===FILE:front/store/src/assets/base.css===
```css
/* 全局基础样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  width: 100%;
  min-height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #f7f8fa;
}

a {
  text-decoration: none;
  color: inherit;
}

ul, ol {
  list-style: none;
}

img {
  max-width: 100%;
  height: auto;
  display: block;
}

/* 布局类 */
.flex {
  display: flex;
}

.flex-col {
  flex-direction: column;
}

.items-center {
  align-items: center;
}

.justify-center {
  justify-content: center;
}

.justify-between {
  justify-content: space-between;
}

.flex-1 {
  flex: 1;
}

.flex-wrap {
  flex-wrap: wrap;
}

/* 间距类 */
.mt-2 {
  margin-top: 8px;
}

.mt-4 {
  margin-top: 16px;
}

.mb-2 {
  margin-bottom: 8px;
}

.mb-4 {
  margin-bottom: 16px;
}

.ml-2 {
  margin-left: 8px;
}

.ml-4 {
  margin-left: 16px;
}

.mr-2 {
  margin-right: 8px;
}

.mr-4 {
  margin-right: 16px;
}

.p-2 {
  padding: 8px;
}

.p-4 {
  padding: 16px;
}

.px-4 {
  padding-left: 16px;
  padding-right: 16px;
}

.py-4 {
  padding-top: 16px;
  padding-bottom: 16px;
}

/* 文本类 */
.text-center {
  text-align: center;
}

.text-right {
  text-align: right;
}

.text-left {
  text-align: left;
}

.font-bold {
  font-weight: bold;
}

.text-sm {
  font-size: 12px;
}

.text-base {
  font-size: 14px;
}

.text-lg {
  font-size: 16px;
}

.text-xl {
  font-size: 18px;
}

/* 颜色类 */
.text-primary {
  color: #1989fa;
}

.text-success {
  color: #07c160;
}

.text-warning {
  color: #ff976a;
}

.text-danger {
  color: #ee0a24;
}

.text-gray {
  color: #969799;
}

/* 背景类 */
.bg-white {
  background-color: #ffffff;
}

/* 阴影类 */
.shadow-sm {
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

/* 圆角类 */
.rounded {
  border-radius: 8px;
}

.rounded-lg {
  border-radius: 12px;
}

/* 容器类 */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

/* 卡片类 */
.card {
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  margin-bottom: 16px;
}

/* 分割线 */
.divider {
  height: 1px;
  background-color: #ebedf0;
  margin: 16px 0;
}
```

===FILE:front/admin/src/utils/format.js===
```javascript
/**
 * 格式化工具函数
 */

/**
 * 格式化日期时间
 * @param {Date|string|number} date - 日期对象、时间戳或日期字符串
 * @param {string} format - 格式化字符串，默认 'YYYY-MM-DD HH:mm:ss'
 * @returns {string} 格式化后的日期时间字符串
 */
export function formatDateTime(date, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!date) return ''
  
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 格式化日期
 * @param {Date|string|number} date - 日期对象、时间戳或日期字符串
 * @param {string} format - 格式化字符串，默认 'YYYY-MM-DD'
 * @returns {string} 格式化后的日期字符串
 */
export function formatDate(date, format = 'YYYY-MM-DD') {
  return formatDateTime(date, format)
}

/**
 * 格式化时间
 * @param {Date|string|number} date - 日期对象、时间戳或日期字符串
 * @param {string} format - 格式化字符串，默认 'HH:mm:ss'
 * @returns {string} 格式化后的时间字符串
 */
export function formatTime(date, format = 'HH:mm:ss') {
  return formatDateTime(date, format)
}

/**
 * 格式化金额
 * @param {number} amount - 金额数值
 * @param {string} currency - 货币符号，默认 '¥'
 * @param {number} decimals - 小数位数，默认 2
 * @returns {string} 格式化后的金额字符串
 */
export function formatCurrency(amount, currency = '¥', decimals = 2) {
  if (amount === null || amount === undefined) return ''
  const num = Number(amount)
  if (isNaN(num)) return ''
  return `${currency}${num.toFixed(decimals)}`
}

/**
 * 格式化数字，添加千分位分隔符
 * @param {number} num - 数字
 * @param {number} decimals - 小数位数，默认 0
 * @returns {string} 格式化后的数字字符串
 */
export function formatNumber(num, decimals = 0) {
  if (num === null || num === undefined) return ''
  const n = Number(num)
  if (isNaN(n)) return ''
  return n.toLocaleString('zh-CN', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals
  })
}

/**
 * 格式化文件大小
 * @param {number} bytes - 字节数
 * @returns {string} 格式化后的文件大小字符串
 */
export function formatFileSize(bytes) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`
}

/**
 * 截取文本并添加省略号
 * @param {string} text - 原始文本
 * @param {number} maxLength - 最大长度
 * @param {string} suffix - 后缀，默认 '...'
 * @returns {string} 截取后的文本
 */
export function truncateText(text, maxLength, suffix = '...') {
  if (!text) return ''
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + suffix
}

/**
 * 首字母大写
 * @param {string} str - 字符串
 * @returns {string} 首字母大写的字符串
 */
export function capitalize(str) {
  if (!str) return ''
  return str.charAt(0).toUpperCase() + str.slice(1)
}

/**
 * 驼峰命名转换为短横线命名
 * @param {string} str - 驼峰命名字符串
 * @returns {string} 短横线命名字符串
 */
export function camelToKebab(str) {
  if (!str) return ''
  return str.replace(/([A-Z])/g, '-$1').toLowerCase()
}

/**
 * 短横线命名转换为驼峰命名
 * @param {string} str - 短横线命名字符串
 * @returns {string} 驼峰命名字符串
 */
export function kebabToCamel(str) {
  if (!str) return ''
  return str.replace(/-([a-z])/g, (match, letter) => letter.toUpperCase())
}
```

===FILE:front/store/src/utils/format.js===
```javascript
/**
 * 格式化工具函数
 */

/**
 * 格式化日期时间
 * @param {Date|string|number} date - 日期对象、时间戳或日期字符串
 * @param {string} format - 格式化字符串，默认 'YYYY-MM-DD HH:mm:ss'
 * @returns {string} 格式化后的日期时间字符串
 */
export function formatDateTime(date, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!date) return ''
  
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 格式化日期
 * @param {Date|string|number} date - 日期对象、时间戳或日期字符串
 * @param {string} format - 格式化字符串，默认 'YYYY-MM-DD'
 * @returns {string} 格式化后的日期字符串
 */
export function formatDate(date, format = 'YYYY-MM-DD') {
  return formatDateTime(date, format)
}

/**
 * 格式化时间
 * @param {Date|string|number} date - 日期对象、时间戳或日期字符串
 * @param {string} format - 格式化字符串，默认 'HH:mm:ss'
 * @returns {string} 格式化后的时间字符串
 */
export function formatTime(date, format = 'HH:mm:ss') {
  return formatDateTime(date, format)
}

/**
 * 相对时间格式化
 * @param {Date|string|number} date - 日期对象、时间戳或日期字符串
 * @returns {string} 相对时间字符串
 */
export function formatRelativeTime(date) {
  if (!date) return ''
  
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''
  
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  const week = 7 * day
  const month = 30 * day
  const year = 365 * day
  
  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < week) {
    return `${Math.floor(diff / day)}天前`
  } else if (diff < month) {
    return `${Math.floor(diff / week)}周前`
  } else if (diff < year) {
    return `${Math.floor(diff / month)}个月前`
  } else {
    return `${Math.floor(diff / year)}年前`
  }
}

/**
 * 格式化金额
 * @param {number} amount - 金额数值
 * @param {string} currency - 货币符号，默认 '¥'
 * @param {number} decimals - 小数位数，默认 2
 * @returns {string} 格式化后的金额字符串
 */
export function formatCurrency(amount, currency = '¥', decimals = 2) {
  if (amount === null || amount === undefined) return ''
  const num = Number(amount)
  if (isNaN(num)) return ''
  return `${currency}${num.toFixed(decimals)}`
}

/**
 * 格式化数字，添加千分位分隔符
 * @param {number} num - 数字
 * @param {number} decimals - 小数位数，默认 0
 * @returns {string} 格式化后的数字字符串
 */
export function formatNumber(num, decimals = 0) {
  if (num === null || num === undefined) return ''
  const n = Number(num)
  if (isNaN(n)) return ''
  return n.toLocaleString('zh-CN', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals
  })
}

/**
 * 截取文本并添加省略号
 * @param {string} text - 原始文本
 * @param {number} maxLength - 最大长度
 * @param {string} suffix - 后缀，默认 '...'
 * @returns {string} 截取后的文本
 */
export function truncateText(text, maxLength, suffix = '...') {
  if (!text) return ''
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + suffix
}

/**
 * 首字母大写
 * @param {string} str - 字符串
 * @returns {string} 首字母大写的字符串
 */
export function capitalize(str) {
  if (!str) return ''
  return str.charAt(0).toUpperCase() + str.slice(1)
}

/**
 * 隐藏手机号中间四位
 * @param {string} phone - 手机号
 * @returns {string} 隐藏后的手机号
 */
export function hidePhone(phone) {
  if (!phone || phone.length !== 11) return phone
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

/**
 * 隐藏邮箱中间部分
 * @param {string} email - 邮箱
 * @returns {string} 隐藏后的邮箱
 */
export function hideEmail(email) {
  if (!email || !email.includes('@')) return email
  const [name, domain] = email.split('@')
  if (name.length <= 2) return email
  return `${name.charAt(0)}***${name.charAt(name.length - 1)}@${domain}`
}
```

===FILE:front/admin/src/utils/validate.js===
```javascript
/**
 * 验证工具函数
 */

/**
 * 验证邮箱
 * @param {string} email - 邮箱地址
 * @returns {boolean} 是否为有效邮箱
 */
export function validateEmail(email) {
  const reg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  return reg.test(email)
}

/**
 * 验证手机号
 * @param {string} phone - 手机号
 * @returns {boolean} 是否为有效手机号
 */
export function validatePhone(phone) {
  const reg = /^1[3-9]\d{9}$/
  return reg.test(phone)
}

/**
 * 验证电话号码（固话或手机）
 * @param {string} tel - 电话号码
 * @returns {boolean} 是否为有效电话号码
 */
export function validateTel(tel) {
  const reg = /^((0\d{2,3}-)?\d{7,8})|(1[3-9]\d{9})$/
  return reg.test(tel)
}

/**
 * 验证密码强度
 * @param {string} password - 密码
 * @returns {number} 密码强度 0-3，0为无效，1为弱，2为中，3为强
 */
export function validatePasswordStrength(password) {
  if (!password || password.length < 6) return 0
  
  let strength = 0
  // 包含小写字母
  if (/[a-z]/.test(password)) strength++
  // 包含大写字母
  if (/[A-Z]/.test(password)) strength++
  // 包含数字
  if (/\d/.test(password)) strength++
  // 包含特殊字符
  if (/[!@#$%^&*(),.?":{}|<>]/.test(password)) strength++
  
  // 根据长度和字符种类判断强度
  if (password.length >= 12 && strength >= 3) return 3
  if (password.length >= 8 && strength >= 2) return 2
  return 1
}

/**
 * 验证用户名
 * @param {string} username - 用户名
 * @returns {boolean} 是否为有效用户名
 */
export function validateUsername(username) {
  // 用户名由字母、数字、下划线组成，长度4-16位
  const reg = /^[a-zA-Z0-9_]{4,16}$/
  return reg.test(username)
}

/**
 * 验证URL
 * @param {string} url - URL地址
 * @returns {boolean} 是否为有效URL
 */
export function validateUrl(url) {
  const reg = /^(https?:\/\/)?([\da-z.-]+)\.([a-z.]{2,6})([/\w .-]*)*\/?$/
  return reg.test(url)
}

/**
 * 验证身份证号
 * @param {string} idCard - 身份证号
 * @returns {boolean} 是否为有效身份证号
 */
export function validateIdCard(idCard) {
  const reg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
  if (!reg.test(idCard)) return false
  
  // 18位身份证校验码验证
  if (idCard.length === 18) {
    const weights = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
    const checkCodes = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2']
    let sum = 0
    
    for (let i = 0; i < 17; i++) {
      sum += parseInt(idCard.charAt(i)) * weights[i]
    }
    
    const mod = sum % 11
    const checkCode = checkCodes[mod]
    
    return idCard.charAt(17).toUpperCase() === checkCode
  }
  
  return true
}

/**
 * 验证邮政编码
 * @param {string} zipCode - 邮政编码
 * @returns {boolean} 是否为有效邮政编码
 */
export function validateZipCode(zipCode) {
  const reg = /^\d{6}$/
  return reg.test(zipCode)
}

/**
 * 验证QQ号
 * @param {string} qq - QQ号
 * @returns {boolean} 是否为有效QQ号
 */
export function validateQQ(qq) {
  const reg = /^[1-9][0-9]{4,10}$/
  return reg.test(qq)
}

/**
 * 验证微信号
 * @param {string} wechat - 微信号
 * @returns {boolean} 是否为有效微信号
 */
export function validateWechat(wechat) {
  const reg = /^[a-zA-Z][a-zA-Z0-9_-]{5,19}$/
  return reg.test(wechat)
}

/**
 * 验证是否为数字
 * @param {*} value - 要验证的值
 * @returns {boolean} 是否为数字
 */
export function validateNumber(value) {
  return !isNaN(parseFloat(value)) && isFinite(value)
}

/**
 * 验证是否为整数
 * @param {*} value - 要验证的值
 * @returns {boolean} 是否为整数
 */
export function validateInteger(value) {
  return Number.isInteger(Number(value))
}

/**
 * 验证是否为正整数
 * @param {*} value - 要验证的值
 * @returns {boolean} 是否为正整数
 */
export function validatePositiveInteger(value) {
  const num = Number(value)
  return Number.isInteger(num) && num > 0
}

/**
 * 验证是否为非负整数
 * @param {*} value - 要验证的值
 * @returns {boolean} 是否为非负整数
 */
export function validateNonNegativeInteger(value) {
  const num = Number(value)
  return Number.isInteger(num) && num >= 0
}

/**
 * 验证金额
 * @param {*} value - 要验证的值
 * @returns {boolean} 是否为有效金额
 */
export function validateAmount(value) {
  const reg = /^\d+(\.\d{1,2})?$/
  return reg.test(value) && Number(value) >= 0
}

/**
 * 验证是否为空
 * @param {*} value - 要验证的值
 * @returns {boolean} 是否为空
 */
export function validateEmpty(value) {
  if (value === null || value === undefined) return true
  if (typeof value === 'string' && value.trim() === '') return true
  if (Array.isArray(value) && value.length === 0) return true
  if (typeof value === 'object' && !Array.isArray(value) && Object.keys(value).length === 0) return true
  return false
}

/**
 * 验证长度范围
 * @param {*} value - 要验证的值
 * @param {number} min - 最小长度
 * @param {number} max - 最大长度
 * @returns {boolean} 是否在长度范围内
 */
export function validateLength(value, min, max) {
  if (value === null || value === undefined) return false
  const length = typeof value === 'string' || Array.isArray(value) ? value.length : String(value).length
  return length >= min && length <= max
}

/**
 * 验证数值范围
 * @param {*} value - 要验证的值
 * @param {number} min - 最小值
 * @param {number} max - 最大值
 * @returns {boolean} 是否在数值范围内
 */
export function validateRange(value, min, max) {
  const num = Number(value)
  if (isNaN(num)) return false
  return num >= min && num <= max
}

/**
 * 验证是否包含中文字符
 * @param {string} value - 要验证的值
 * @returns {boolean} 是否包含中文字符
 */
export function validateContainChinese(value) {
  const reg = /[\u4e00-\u9fa5]/
  return reg.test(value)
}

/**
 * 验证是否全为中文字符
 * @param {string} value - 要验证的值
 * @returns {boolean} 是否全为中文字符
 */
export function validateChinese(value) {
  const reg = /^[\u4e00-\u9fa5]+$/
  return reg.test(value)
}
```

===FILE:front/store/src/utils/validate.js===
```javascript
/**
 * 验证工具函数
 */

/**
 * 验证邮箱
 * @param {string} email - 邮箱地址
 * @returns {boolean} 是否为有效邮箱
 */
export function validateEmail(email) {
  const reg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  return reg.test(email)
}

/**
 * 验证手机号
 * @param {string} phone - 手机号
 * @returns {boolean} 是否为有效手机号
 */
export function validatePhone(phone) {
  const reg = /^1[3-9]\d{9}$/
  return reg.test(phone)
}

/**
 * 验证密码强度
 * @param {string} password - 密码
 * @returns {number} 密码强度 0-3，0为无效，1为弱，2为中，3为强
 */
export function validatePasswordStrength(password) {
  if (!password || password.length < 6) return 0
  
  let strength = 0
  // 包含小写字母
  if (/[a-z]/.test(password)) strength++
  // 包含大写字母
  if (/[A-Z]/.test(password)) strength++
  // 包含数字
  if (/\d/.test(password)) strength++
  // 包含特殊字符
  if (/[!@#$%^&*(),.?":{}|<>]/.test(password)) strength++
  
  // 根据长度和字符种类判断强度
  if (password.length >= 12 && strength >= 3) return 3
  if (password.length >= 8 && strength >= 2) return 2
  return 1
}

/**
 * 验证用户名
 * @param {string} username - 用户名
 * @returns {boolean} 是否为有效用户名
 */
export function validateUsername(username) {
  // 用户名由字母、数字、下划线组成，长度4-16位
  const reg = /^[a-zA-Z0-9_]{4,16}$/
  return reg.test(username)
}

/**
 * 验证身份证号
 * @param {string} idCard - 身份证号
 * @returns {boolean} 是否为有效身份证号
 */
export function validateIdCard(idCard) {
  const reg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
  if (!reg.test(idCard)) return false
  
  // 18位身份证校验码验证
  if (idCard.length === 18) {
    const weights = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
    const checkCodes = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2']
    let sum = 0
    
    for (let i = 0; i < 17; i++) {
      sum += parseInt(idCard.charAt(i)) * weights[i]
    }
    
    const mod = sum % 11
    const checkCode = checkCodes[mod]
    
    return idCard.charAt(17).toUpperCase() === checkCode
  }
  
  return true
}

/**
 * 验证邮政编码
 * @param {string} zipCode - 邮政编码
 * @returns {boolean} 是否为有效邮政编码
 */
export function validateZipCode(zipCode) {
  const reg = /^\d{6}$/
  return reg.test(zipCode)
}

/**
 * 验证是否为数字
 * @param {*} value - 要验证的值
 * @returns {boolean} 是否为数字
 */
export function validateNumber(value) {
  return !isNaN(parseFloat(value)) && isFinite(value)
}

/**
 * 验证是否为整数
 * @param {*} value - 要验证的值
 * @returns {boolean} 是否为整数
 */
export function validateInteger(value) {
  return Number.isInteger(Number(value))
}

/**
 * 验证是否为正整数
 * @param {*} value - 要验证的值
 * @returns {boolean} 是否为正整数
 */
export function validatePositiveInteger(value) {
  const num = Number(value)
  return Number.isInteger(num) && num > 0
}

/**
 * 验证金额
 * @param {*} value - 要验证的值
 * @returns {boolean} 是否为有效金额
 */
export function validateAmount(value) {
  const reg = /^\d+(\.\d{1,2})?$/
  return reg.test(value) && Number(value) >= 0
}

/**
 * 验证是否为空
 * @param {*} value - 要验证的值
 * @returns {boolean} 是否为空
 */
export function validateEmpty(value) {
  if (value === null || value === undefined) return true
  if (typeof value === 'string' && value.trim() === '') return true
  if (Array.isArray(value) && value.length === 0) return true
  return false
}

/**
 * 验证长度范围
 * @param {*} value - 要验证的值
 * @param {number} min - 最小长度
 * @param {number} max - 最大长度
 * @returns {boolean} 是否在长度范围内
 */
export function validateLength(value, min, max) {
  if (value === null || value === undefined) return false
  const length = typeof value === 'string' || Array.isArray(value) ? value.length : String(value).length
  return length >= min && length <= max
}

/**
 * 验证数值范围
 * @param {*} value - 要验证的值
 * @param {number} min - 最小值
 * @param {number} max - 最大值
 * @returns {boolean} 是否在数值范围内
 */
export function validateRange(value, min, max) {
  const num = Number(value)
  if (isNaN(num)) return false
  return num >= min && num <= max
}

/**
 * 验证是否包含中文字符
 * @param {string} value - 要验证的值
 * @returns {boolean} 是否包含中文字符
 */
export function validateContainChinese(value) {
  const reg = /[\u4e00-\u9fa5]/
  return reg.test(value)
}

/**
 * 验证收货人姓名
 * @param {string} name - 收货人姓名
 * @returns {boolean} 是否为有效收货人姓名
 */
export function validateReceiverName(name) {
  if (!name || name.trim() === '') return false
  // 收货人姓名可以是中文、英文、数字，但不能包含特殊字符
  const reg = /^[\u4e00-\u9fa5a-zA-Z0-9]{1,20}$/
  return reg.test(name)
}

/**
 * 验证详细地址
 * @param {string} address - 详细地址
 * @returns {boolean} 是否为有效详细地址
 */
export function validateDetailAddress(address) {
  if (!address || address.trim() === '') return false
  // 详细地址至少5个字符
  return address.trim().length >= 5
}
```

===FILE:front/admin/src/utils/constants.js===
```javascript
/**
 * 常量定义
 */

// 订单状态
export const ORDER_STATUS = {
  PENDING_PAYMENT: 0,
  PAID: 1,
  SHIPPED: 2,
  COMPLETED: 3,
  CANCELLED: 4,
  REFUNDED: 5
}

// 订单状态文本
export const ORDER_STATUS_TEXT = {
  [ORDER_STATUS.PENDING_PAYMENT]: '待付款',
  [ORDER_STATUS.PAID]: '已付款',
  [ORDER_STATUS.SHIPPED]: '已发货',
  [ORDER_STATUS.COMPLETED]: '已完成',
  [ORDER_STATUS.CANCELLED]: '已取消',
  [ORDER_STATUS.REFUNDED]: '已退款'
}

// 订单状态类型
export const ORDER_STATUS_TYPE = {
  [ORDER_STATUS.PENDING_PAYMENT]: 'warning',
  [ORDER_STATUS.PAID]: 'primary',
  [ORDER_STATUS.SHIPPED]: 'info',
  [ORDER_STATUS.COMPLETED]: 'success',
  [ORDER_STATUS.CANCELLED]: 'info',
  [ORDER_STATUS.REFUNDED]: 'danger'
}

// 商品状态
export const PRODUCT_STATUS = {
  ON_SALE: 1,
  OFF_SALE: 0
}

// 商品状态文本
export const PRODUCT_STATUS_TEXT = {
  [PRODUCT_STATUS.ON_SALE]: '上架',
  [PRODUCT_STATUS.OFF_SALE]: '下架'
}

// 用户状态
export const USER_STATUS = {
  ACTIVE: 1,
  DISABLED: 0
}

// 用户状态文本
export const USER_STATUS_TEXT = {
  [USER_STATUS.ACTIVE]: '正常',
  [USER_STATUS.DISABLED]: '禁用'
}

// 用户性别
export const USER_GENDER = {
  UNKNOWN: 0,
  MALE: 1,
  FEMALE: 2
}

// 用户性别文本
export const USER_GENDER_TEXT = {
  [USER_GENDER.UNKNOWN]: '未知',
  [USER_GENDER.MALE]: '男',
  [USER_GENDER.FEMALE]: '女'
}

// 支付方式
export const PAYMENT_METHOD = {
  ALIPAY: 1,
  WECHAT: 2,
  BALANCE: 3
}

// 支付方式文本
export const PAYMENT_METHOD_TEXT = {
  [PAYMENT_METHOD.ALIPAY]: '支付宝',
  [PAYMENT_METHOD.WECHAT]: '微信支付',
  [PAYMENT_METHOD.BALANCE]: '余额支付'
}

// 配送方式
export const SHIPPING_METHOD = {
  EXPRESS: 1,
  SELF_PICKUP: 2
}

// 配送方式文本
export const SHIPPING_METHOD_TEXT = {
  [SHIPPING_METHOD.EXPRESS]: '快递配送',
  [SHIPPING_METHOD.SELF_PICKUP]: '自提'
}

// 系统角色
export const SYSTEM_ROLE = {
  ADMIN: 'admin',
  EDITOR: 'editor',
  VIEWER: 'viewer'
}

// 系统角色文本
export const SYSTEM_ROLE_TEXT = {
  [SYSTEM_ROLE.ADMIN]: '管理员',
  [SYSTEM_ROLE.EDITOR]: '编辑',
  [SYSTEM_ROLE.VIEWER]: '访客'
}

// 常用正则表达式
export const REGEX = {
  EMAIL: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
  PHONE: /^1[3-9]\d{9}$/,
  ID_CARD: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/,
  URL: /^(https?:\/\/)?([\da-z.-]+)\.([a-z.]{2,6})([/\w .-]*)*\/?$/,
  ZIP_CODE: /^\d{6}$/,
  CHINESE: /^[\u4e00-\u9fa5]+$/,
  USERNAME: /^[a-zA-Z0-9_]{4,16}$/,
  PASSWORD: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{6,}$/
}

// 日期时间格式
export const DATE_FORMAT = {
  DATE: 'YYYY-MM-DD',
  TIME: 'HH:mm:ss',
  DATETIME: 'YYYY-MM-DD HH:mm:ss',
  MONTH: 'YYYY-MM',
  YEAR: 'YYYY'
}

// 存储键名
export const STORAGE_KEYS = {
  TOKEN: 'admin_token',
  USER_INFO: 'admin_user_info',
  SETTINGS: 'admin_settings',
  LANGUAGE: 'admin_language'
}

// 分页默认配置
export const PAGINATION = {
  PAGE_SIZE: 10,
  PAGE_SIZES: [10, 20, 50, 100],
  CURRENT_PAGE: 1
}

// 上传文件配置
export const UPLOAD_CONFIG = {
  MAX_SIZE: 5 * 1024 * 1024, // 5MB
  ACCEPT_IMAGE: 'image/jpeg,image/jpg,image/png,image/gif,image/webp',
  ACCEPT_FILE: '.pdf,.doc,.docx,.xls,.xlsx,.txt',
  UPLOAD_URL: '/api/upload'
}

// 默认请求超时时间
export const REQUEST_TIMEOUT = 30000 // 30秒
```

===FILE:front/store/src/utils/constants.js===
```javascript
/**
 * 常量定义
 */

// 订单状态
export const ORDER_STATUS = {
  PENDING_PAYMENT: 0,
  PAID: 1,
  SHIPPED: 2,
  COMPLETED: 3,
  CANCELLED: 4,
  REFUNDED: 5
}

// 订单状态文本
export const ORDER_STATUS_TEXT = {
  [ORDER_STATUS.PENDING_PAYMENT]: '待付款',
  [ORDER_STATUS.PAID]: '已付款',
  [ORDER_STATUS.SHIPPED]: '已发货',
  [ORDER_STATUS.COMPLETED]: '已完成',
  [ORDER_STATUS.CANCELLED]: '已取消',
  [ORDER_STATUS.REFUNDED]: '已退款'
}

// 订单状态步骤
export const ORDER_STATUS_STEPS = {
  [ORDER_STATUS.PENDING_PAYMENT]: 1,
  [ORDER_STATUS.PAID]: 2,
  [ORDER_STATUS.SHIPPED]: 3,
  [ORDER_STATUS.COMPLETED]: 4,
  [ORDER_STATUS.CANCELLED]: 0,
  [ORDER_STATUS.REFUNDED]: 0
}

// 支付方式
export const PAYMENT_METHOD = {
  ALIPAY: 1,
  WECHAT: 2,
  BALANCE: 3
}

// 支付方式文本
export const PAYMENT_METHOD_TEXT = {
  [PAYMENT_METHOD.ALIPAY]: '支付宝',
  [PAYMENT_METHOD.WECHAT]: '微信支付',
  [PAYMENT_METHOD.BALANCE]: '余额支付'
}

// 配送方式
export const SHIPPING_METHOD = {
  EXPRESS: 1,
  SELF_PICKUP: 2
}

// 配送方式文本
export const SHIPPING_METHOD_TEXT = {
  [SHIPPING_METHOD.EXPRESS]: '快递配送',
  [SHIPPING_METHOD.SELF_PICKUP]: '自提'
}

// 用户性别
export const USER_GENDER = {
  UNKNOWN: 0,
  MALE: 1,
  FEMALE: 2
}

// 用户性别文本
export const USER_GENDER_TEXT = {
  [USER_GENDER.UNKNOWN]: '保密',
  [USER_GENDER.MALE]: '男',
  [USER_GENDER.FEMALE]: '女'
}

// 常用正则表达式
export const REGEX = {
  EMAIL: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
  PHONE: /^1[3-9]\d{9}$/,
  ID_CARD: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/,
  ZIP_CODE: /^\d{6}$/,
  USERNAME: /^[a-zA-Z0-9_]{4,16}$/,
  PASSWORD: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{6,}$/
}

// 日期时间格式
export const DATE_FORMAT = {
  DATE: 'YYYY-MM-DD',
  TIME: 'HH:mm:ss',
  DATETIME: 'YYYY-MM-DD HH:mm:ss',
  MONTH: 'YYYY-MM',
  YEAR: 'YYYY'
}

// 存储键名
export const STORAGE_KEYS = {
  TOKEN: 'store_token',
  USER_INFO: 'store_user_info',
  CART: 'store_cart',
  ADDRESS: 'store_address',
  SEARCH_HISTORY: 'store_search_history'
}

// 分页默认配置
export const PAGINATION = {
  PAGE_SIZE: 10,
  CURRENT_PAGE: 1
}

// 商品列表排序
export const PRODUCT_SORT = {
  DEFAULT: 'default',
  PRICE_ASC: 'price_asc',
  PRICE_DESC: 'price_desc',
  SALES_DESC: 'sales_desc',
  NEWEST: 'newest'
}

// 商品列表排序文本
export const PRODUCT_SORT_TEXT = {
  [PRODUCT_SORT.DEFAULT]: '综合',
  [PRODUCT_SORT.PRICE_ASC]: '价格升序',
  [PRODUCT_SORT.PRICE_DESC]: '价格降序',
  [PRODUCT_SORT.SALES_DESC]: '销量优先',
  [PRODUCT_SORT.NEWEST]: '最新发布'
}

// 购物车最大商品数量
export const CART_MAX_COUNT = 99

// 默认请求超时时间
export const REQUEST_TIMEOUT = 30000 // 30秒

// 页面路由
export const ROUTES = {
  HOME: '/',
  PRODUCT_LIST: '/products',
  PRODUCT_DETAIL: '/product/:id',
  CART: '/cart',
  CHECKOUT: '/checkout',
  ORDER_LIST: '/orders',
  ORDER_DETAIL: '/order/:id',
  ADDRESS_LIST: '/address',
  ADDRESS_EDIT: '/address/edit/:id?',
  USER: '/user',
  LOGIN: '/login',
  REGISTER: '/register'
}
```

这些文件都是项目中常用的工具类和常量定义文件，它们对于项目的正常运行和开发效率非常重要。接下来我将继续检查是否还有其他缺失的关键文件。