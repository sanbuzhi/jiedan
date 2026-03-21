/**
 * 移动端弹窗工具类
 * 提供类似微信小程序的弹窗体验
 */

import { createApp, h, ref } from 'vue'
import MobileDialog from '../mobile/components/MobileDialog.vue'
import MobileToast from '../mobile/components/MobileToast.vue'

/**
 * 显示提示框（类似 wx.showToast）
 * @param {Object} options - 配置项
 * @param {string} options.title - 提示内容
 * @param {string} options.icon - 图标类型：success | error | loading | none
 * @param {number} options.duration - 持续时间（毫秒）
 */
export const showToast = (options = {}) => {
  const {
    title = '',
    icon = 'info',
    duration = 2000
  } = options

  // 创建容器
  const container = document.createElement('div')
  document.body.appendChild(container)

  const visible = ref(true)

  const instance = createApp({
    setup() {
      const toastVisible = ref(true)
      
      // 自动关闭
      setTimeout(() => {
        toastVisible.value = false
        setTimeout(() => {
          instance.unmount()
          document.body.removeChild(container)
        }, 300)
      }, duration)

      return () => h(MobileToast, {
        visible: toastVisible.value,
        message: title,
        type: icon === 'success' ? 'success' : icon === 'error' ? 'error' : icon === 'loading' ? 'loading' : 'info'
      })
    }
  })

  instance.mount(container)
}

/**
 * 显示消息提示框（类似 wx.showModal）
 * @param {Object} options - 配置项
 * @param {string} options.title - 标题
 * @param {string} options.content - 内容
 * @param {boolean} options.showCancel - 是否显示取消按钮
 * @param {string} options.cancelText - 取消按钮文字
 * @param {string} options.confirmText - 确定按钮文字
 * @returns {Promise<boolean>} - 用户是否点击了确定
 */
export const showModal = (options = {}) => {
  const {
    title = '',
    content = '',
    showCancel = true,
    cancelText = '取消',
    confirmText = '确定'
  } = options

  return new Promise((resolve) => {
    const container = document.createElement('div')
    document.body.appendChild(container)

    const visible = ref(true)

    const instance = createApp({
      setup() {
        const dialogVisible = ref(true)

        const onConfirm = () => {
          dialogVisible.value = false
          setTimeout(() => {
            instance.unmount()
            document.body.removeChild(container)
          }, 300)
          resolve(true)
        }

        const onCancel = () => {
          dialogVisible.value = false
          setTimeout(() => {
            instance.unmount()
            document.body.removeChild(container)
          }, 300)
          resolve(false)
        }

        const onClose = () => {
          dialogVisible.value = false
          setTimeout(() => {
            instance.unmount()
            document.body.removeChild(container)
          }, 300)
          resolve(false)
        }

        return () => h(MobileDialog, {
          visible: dialogVisible.value,
          title,
          message: content,
          showClose: false,
          showFooter: true,
          showCancel: showCancel,
          showConfirm: true,
          showCloseBtn: !showCancel,
          cancelText,
          confirmText,
          closeOnClickOverlay: false,
          onConfirm,
          onCancel,
          onClose
        })
      }
    })

    instance.mount(container)
  })
}

/**
 * 显示加载提示（类似 wx.showLoading）
 * @param {Object} options - 配置项
 * @param {string} options.title - 提示内容
 */
export const showLoading = (options = {}) => {
  const {
    title = '加载中...'
  } = options

  const container = document.createElement('div')
  document.body.appendChild(container)

  const instance = createApp({
    setup() {
      const toastVisible = ref(true)

      return () => h(MobileToast, {
        visible: toastVisible.value,
        message: title,
        type: 'loading'
      })
    }
  })

  instance.mount(container)

  // 返回隐藏方法
  return {
    hide: () => {
      setTimeout(() => {
        instance.unmount()
        document.body.removeChild(container)
      }, 300)
    }
  }
}

/**
 * 隐藏加载提示（类似 wx.hideLoading）
 */
export const hideLoading = () => {
  // 由 showLoading 返回的对象调用 hide 方法
}

/**
 * 显示确认框（简化版 showModal）
 * @param {string} title - 标题
 * @param {string} content - 内容
 * @returns {Promise<boolean>}
 */
export const confirm = (title, content) => {
  return showModal({
    title,
    content,
    showCancel: true
  })
}

/**
 * 显示警告框（只有确定按钮）
 * @param {string} title - 标题
 * @param {string} content - 内容
 * @returns {Promise<void>}
 */
export const alert = (title, content) => {
  return showModal({
    title,
    content,
    showCancel: false
  }).then(() => {})
}

export default {
  showToast,
  showModal,
  showLoading,
  hideLoading,
  confirm,
  alert
}
