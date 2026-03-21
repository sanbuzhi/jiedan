export const ORDER_STATUS = {
  PENDING_PAYMENT: { value: 0, label: '待付款', type: 'warning' },
  PENDING_SHIPMENT: { value: 1, label: '待发货', type: 'primary' },
  SHIPPED: { value: 2, label: '已发货', type: 'info' },
  COMPLETED: { value: 3, label: '已完成', type: 'success' },
  CANCELLED: { value: 4, label: '已取消', type: 'danger' },
  REFUNDED: { value: 5, label: '已退款', type: 'danger' }
}

export const ORDER_STATUS_LIST = Object.values(ORDER_STATUS)

export const TAB_BAR_LIST = [
  { path: '/home', name: '首页', icon: 'home-o' },
  { path: '/products', name: '分类', icon: 'apps-o' },
  { path: '/cart', name: '购物车', icon: 'shopping-cart-o', badge: true },
  { path: '/user', name: '我的', icon: 'user-o' }
]