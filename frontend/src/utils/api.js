import request from './request'

export async function getHealthStatus() {
  return request.get('/health')
}

export async function sendVerificationCode(phone) {
  return request.post('/auth/send-code', { phone })
}

export async function login(data) {
  return request.post('/auth/login', data)
}

export async function webLogin(data) {
  return request.post('/auth/web-login', data)
}

export async function register(data) {
  return request.post('/auth/register', data)
}

export async function getUserInfo() {
  return request.get('/users/me')
}

export async function updateUserInfo(data) {
  return request.put('/users/me', data)
}

export async function getMyReferrals(level = null) {
  const params = level ? { level } : {}
  return request.get('/users/referrals', { params })
}

export async function getReferralTree() {
  return request.get('/users/referrals/tree')
}

export async function getMyPoints(skip = 0, limit = 20) {
  return request.get('/users/points', { params: { skip, limit } })
}

export async function getRules() {
  return request.get('/users/rules')
}

export async function initializeRules() {
  return request.post('/users/rules/init')
}

export async function generateMaterial(data) {
  return request.post('/materials/generate', data)
}

export async function createMaterial(data) {
  return request.post('/materials', data)
}

export async function getMaterials(params = {}) {
  return request.get('/materials', { params })
}

export async function getMaterial(id) {
  return request.get(`/materials/${id}`)
}

export async function updateMaterial(id, data) {
  return request.put(`/materials/${id}`, data)
}

export async function deleteMaterial(id) {
  return request.delete(`/materials/${id}`)
}

export async function getCategories() {
  return request.get('/materials/categories')
}

export async function createCategory(data) {
  return request.post('/materials/categories', data)
}

export async function getTemplates(params = {}) {
  return request.get('/materials/templates', { params })
}

export async function createTemplate(data) {
  return request.post('/materials/templates', data)
}

export async function getSupportedPlatforms() {
  return request.get('/publish/platforms')
}

export async function createPlatformAccount(data) {
  return request.post('/publish/accounts', data)
}

export async function getPlatformAccounts(params = {}) {
  return request.get('/publish/accounts', { params })
}

export async function getPlatformAccount(id) {
  return request.get(`/publish/accounts/${id}`)
}

export async function updatePlatformAccount(id, data) {
  return request.put(`/publish/accounts/${id}`, data)
}

export async function deletePlatformAccount(id) {
  return request.delete(`/publish/accounts/${id}`)
}

export async function createPublishTask(data) {
  return request.post('/publish/tasks', data)
}

export async function getPublishTasks(params = {}) {
  return request.get('/publish/tasks', { params })
}

export async function getPublishTask(id) {
  return request.get(`/publish/tasks/${id}`)
}

export async function updatePublishTask(id, data) {
  return request.put(`/publish/tasks/${id}`, data)
}

export async function cancelPublishTask(id) {
  return request.post(`/publish/tasks/${id}/cancel`)
}

export async function retryPublishTask(id) {
  return request.post(`/publish/tasks/${id}/retry`)
}

export async function getPublishRecords(params = {}) {
  return request.get('/publish/records', { params })
}

export async function getPublishStatistics() {
  return request.get('/publish/statistics')
}

export async function getDashboardStats(days = 30) {
  return request.get('/analytics/dashboard', { params: { days } })
}

export async function getConversionStats(days = 30) {
  return request.get('/analytics/conversion-stats', { params: { days } })
}

export async function getEvents(params = {}) {
  return request.get('/analytics/events', { params })
}

export async function createEvent(data) {
  return request.post('/analytics/events', data)
}

export async function getExperiments(params = {}) {
  return request.get('/analytics/experiments', { params })
}

export async function getExperiment(id) {
  return request.get(`/analytics/experiments/${id}`)
}

export async function createExperiment(data) {
  return request.post('/analytics/experiments', data)
}

export async function updateExperiment(id, data) {
  return request.put(`/analytics/experiments/${id}`, data)
}

export async function getOptimizationRules(params = {}) {
  return request.get('/analytics/rules', { params })
}

export async function createOptimizationRule(data) {
  return request.post('/analytics/rules', data)
}

export async function updateOptimizationRule(id, data) {
  return request.put(`/analytics/rules/${id}`, data)
}

export async function deleteOptimizationRule(id) {
  return request.delete(`/analytics/rules/${id}`)
}

export async function runOptimization(strategy = 'bayesian', days = 30) {
  return request.post('/analytics/optimize', null, { params: { strategy, days } })
}

export async function getOptimizationSuggestions() {
  return request.get('/analytics/optimize/suggestions')
}

export async function getExperimentResults(experimentId) {
  return request.get(`/analytics/experiments/${experimentId}/results`)
}

export async function getAdminUsers(params = {}) {
  return request.get('/admin/users', { params })
}

export async function toggleUserActive(id) {
  return request.put(`/admin/users/${id}/toggle-active`)
}

export async function updateAdminUser(id, data) {
  return request.put(`/admin/users/${id}`, data)
}

export async function getSystemConfig() {
  return request.get('/admin/system-config')
}

export async function updateSystemConfig(data) {
  return request.put('/admin/system-config', data)
}

export async function getRequirements(params = {}) {
  return request.get('/requirements', { params })
}

export async function getRequirementDetail(id) {
  return request.get(`/requirements/${id}`)
}

export async function updateRequirementStatus(id, data) {
  return request.put(`/requirements/${id}`, data)
}

export async function getOrders(params = {}) {
  return request.get('/orders', { params })
}

export async function getOrderDetail(id) {
  return request.get(`/orders/${id}`)
}

export async function confirmPayment(id) {
  return request.put(`/orders/${id}/pay`)
}

export async function refundOrder(id) {
  return request.put(`/orders/${id}/refund`)
}

export default {
  getHealthStatus,
  sendVerificationCode,
  login,
  webLogin,
  register,
  getUserInfo,
  updateUserInfo,
  getMyReferrals,
  getReferralTree,
  getMyPoints,
  getRules,
  initializeRules,
  generateMaterial,
  createMaterial,
  getMaterials,
  getMaterial,
  updateMaterial,
  deleteMaterial,
  getCategories,
  createCategory,
  getTemplates,
  createTemplate,
  getSupportedPlatforms,
  createPlatformAccount,
  getPlatformAccounts,
  getPlatformAccount,
  updatePlatformAccount,
  deletePlatformAccount,
  createPublishTask,
  getPublishTasks,
  getPublishTask,
  updatePublishTask,
  cancelPublishTask,
  retryPublishTask,
  getPublishRecords,
  getPublishStatistics,
  getDashboardStats,
  getConversionStats,
  getEvents,
  createEvent,
  getExperiments,
  getExperiment,
  createExperiment,
  updateExperiment,
  getOptimizationRules,
  createOptimizationRule,
  updateOptimizationRule,
  deleteOptimizationRule,
  runOptimization,
  getOptimizationSuggestions,
  getExperimentResults,
  getAdminUsers,
  toggleUserActive,
  updateAdminUser,
  getSystemConfig,
  updateSystemConfig,
  getRequirements,
  getRequirementDetail,
  updateRequirementStatus,
  getOrders,
  getOrderDetail,
  confirmPayment,
  refundOrder
}
