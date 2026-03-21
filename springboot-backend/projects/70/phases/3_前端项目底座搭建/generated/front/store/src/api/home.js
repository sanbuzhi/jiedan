import request from '@/utils/request'

// 获取首页轮播图
export function getHomeBanners() {
  return request({
    url: '/store/home/banners',
    method: 'get'
  })
}

// 获取首页热门商品
export function getHotProducts(params) {
  return request({
    url: '/store/home/hot-products',
    method: 'get',
    params
  })
}

// 获取首页新品推荐
export function getNewProducts(params) {
  return request({
    url: '/store/home/new-products',
    method: 'get',
    params
  })
}