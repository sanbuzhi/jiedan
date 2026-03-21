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