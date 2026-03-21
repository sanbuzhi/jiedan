export const ORDER_STATUS = {
  PENDING_PAYMENT: { value: 0, label: '待付款', type: 'warning' },
  PENDING_SHIPMENT: { value: 1, label: '待发货', type: 'primary' },
  SHIPPED: { value: 2, label: '已发货', type: 'info' },
  COMPLETED: { value: 3, label: '已完成', type: 'success' },
  CANCELLED: { value: 4, label: '已取消', type: 'danger' },
  REFUNDED: { value: 5, label: '已退款', type: 'danger' }
}

export const ORDER_STATUS_LIST = Object.values(ORDER_STATUS)

export const PRODUCT_STATUS = {
  OFF_SHELF: { value: 0, label: '下架', type: 'danger' },
  ON_SHELF: { value: 1, label: '上架', type: 'success' }
}

export const PRODUCT_STATUS_LIST = Object.values(PRODUCT_STATUS)

export const GENDER = {
  UNKNOWN: { value: 0, label: '未知' },
  MALE: { value: 1, label: '男' },
  FEMALE: { value: 2, label: '女' }
}

export const GENDER_LIST = Object.values(GENDER)