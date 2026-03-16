import request from './request'

const SESSION_ID_KEY = 'analytics_session_id'
const USER_ID_KEY = 'user_id'

function generateSessionId() {
  return 'sess_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
}

function getSessionId() {
  let sessionId = sessionStorage.getItem(SESSION_ID_KEY)
  if (!sessionId) {
    sessionId = generateSessionId()
    sessionStorage.setItem(SESSION_ID_KEY, sessionId)
  }
  return sessionId
}

function getUserId() {
  const userInfo = localStorage.getItem('user')
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      return user.id || null
    } catch {
      return null
    }
  }
  return null
}

function getDeviceInfo() {
  return {
    screenWidth: window.screen.width,
    screenHeight: window.screen.height,
    viewportWidth: window.innerWidth,
    viewportHeight: window.innerHeight,
    language: navigator.language,
    platform: navigator.platform,
    userAgent: navigator.userAgent
  }
}

export const EventType = {
  CLICK: 'click',
  VIEW: 'view',
  REGISTER: 'register',
  CONVERT: 'convert',
  GENERATE: 'generate',
  SAVE: 'save',
  SHARE: 'share',
  LOGIN: 'login',
  LOGOUT: 'logout',
  PUBLISH: 'publish',
  EXPORT: 'export'
}

export async function trackEvent(eventType, data = {}) {
  try {
    // 检查是否有 token，没有则跳过（避免登录页触发 403）
    const token = localStorage.getItem('token')
    if (!token) {
      console.log('Analytics skipped: no token')
      return
    }

    // 检查是否是模拟 token（开发模式）
    if (token.startsWith('mock_token_')) {
      console.log('Analytics skipped: mock token')
      return
    }

    const payload = {
      event_type: eventType,
      platform: data.platform || null,
      material_id: data.materialId || null,
      publish_record_id: data.publishRecordId || null,
      session_id: getSessionId(),
      source_url: window.location.href,
      user_agent: navigator.userAgent,
      metadata: {
        ...data.metadata,
        device: getDeviceInfo(),
        timestamp: Date.now()
      }
    }

    const userId = getUserId()
    if (userId) {
      payload.user_id = userId
    }

    await request.post('/analytics/track', payload)
  } catch (error) {
    console.error('Event tracking failed:', error)
  }
}

export function trackClick(element, data = {}) {
  return trackEvent(EventType.CLICK, {
    ...data,
    metadata: {
      ...data.metadata,
      element: element,
      path: window.location.pathname
    }
  })
}

export function trackView(pageName, data = {}) {
  return trackEvent(EventType.VIEW, {
    ...data,
    metadata: {
      ...data.metadata,
      page: pageName,
      referrer: document.referrer
    }
  })
}

export function trackConversion(conversionType, data = {}) {
  return trackEvent(EventType.CONVERT, {
    ...data,
    metadata: {
      ...data.metadata,
      conversion_type: conversionType
    }
  })
}

export function trackGenerate(platform, materialType, data = {}) {
  return trackEvent(EventType.GENERATE, {
    platform,
    ...data,
    metadata: {
      ...data.metadata,
      material_type: materialType
    }
  })
}

export function trackSave(materialId, data = {}) {
  return trackEvent(EventType.SAVE, {
    materialId,
    ...data
  })
}

export function trackPublish(platform, materialId, data = {}) {
  return trackEvent(EventType.PUBLISH, {
    platform,
    materialId,
    ...data
  })
}

export function trackShare(platform, data = {}) {
  return trackEvent(EventType.SHARE, {
    platform,
    ...data
  })
}

export function trackRegister(referralCode = null, data = {}) {
  return trackEvent(EventType.REGISTER, {
    ...data,
    metadata: {
      ...data.metadata,
      referral_code: referralCode,
      source: document.referrer || 'direct'
    }
  })
}

export function trackLogin(data = {}) {
  return trackEvent(EventType.LOGIN, {
    ...data,
    metadata: {
      ...data.metadata,
      method: 'phone_code'
    }
  })
}

export function trackLogout(data = {}) {
  return trackEvent(EventType.LOGOUT, data)
}

export function trackExport(format, data = {}) {
  return trackEvent(EventType.EXPORT, {
    ...data,
    metadata: {
      ...data.metadata,
      format
    }
  })
}

export default {
  EventType,
  trackEvent,
  trackClick,
  trackView,
  trackConversion,
  trackGenerate,
  trackSave,
  trackPublish,
  trackShare,
  trackRegister,
  trackLogin,
  trackLogout,
  trackExport
}
