import request from '@/utils/request'

/**
 * AI策略配置API服务
 */

/**
 * 获取所有AI策略配置
 * @returns {Promise<Array>} 配置列表
 */
export function getAllConfigs() {
  return request({
    url: '/ai/strategy/config',
    method: 'get'
  })
}

/**
 * 获取所有启用的AI策略配置
 * @returns {Promise<Array>} 配置列表
 */
export function getEnabledConfigs() {
  return request({
    url: '/ai/strategy/config/enabled',
    method: 'get'
  })
}

/**
 * 根据ID获取AI策略配置
 * @param {number} id 配置ID
 * @returns {Promise<Object>} 配置对象
 */
export function getConfigById(id) {
  return request({
    url: `/ai/strategy/config/${id}`,
    method: 'get'
  })
}

/**
 * 根据接口代码获取AI策略配置
 * @param {string} apiCode 接口代码
 * @returns {Promise<Object>} 配置对象
 */
export function getConfigByApiCode(apiCode) {
  return request({
    url: `/ai/strategy/config/code/${apiCode}`,
    method: 'get'
  })
}

/**
 * 创建AI策略配置
 * @param {Object} data 配置数据
 * @returns {Promise<Object>} 创建后的配置
 */
export function createConfig(data) {
  return request({
    url: '/ai/strategy/config',
    method: 'post',
    data
  })
}

/**
 * 更新AI策略配置
 * @param {number} id 配置ID
 * @param {Object} data 配置数据
 * @returns {Promise<Object>} 更新后的配置
 */
export function updateConfig(id, data) {
  return request({
    url: `/ai/strategy/config/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除AI策略配置
 * @param {number} id 配置ID
 * @returns {Promise<void>}
 */
export function deleteConfig(id) {
  return request({
    url: `/ai/strategy/config/${id}`,
    method: 'delete'
  })
}

/**
 * 批量更新AI策略配置
 * @param {Array} data 配置列表
 * @returns {Promise<Array>} 更新后的配置列表
 */
export function batchUpdateConfigs(data) {
  return request({
    url: '/ai/strategy/config/batch',
    method: 'post',
    data
  })
}

/**
 * 初始化默认AI策略配置
 * @returns {Promise<void>}
 */
export function initDefaultConfigs() {
  return request({
    url: '/ai/strategy/config/init',
    method: 'post'
  })
}

/**
 * 获取可用AI模型列表
 * @returns {Promise<Array>} 模型列表
 */
export function getAvailableModels() {
  return request({
    url: '/ai/strategy/models',
    method: 'get'
  })
}

/**
 * 获取AI提供商列表
 * @returns {Promise<Array>} 提供商列表
 */
export function getProviders() {
  return request({
    url: '/ai/strategy/providers',
    method: 'get'
  })
}
