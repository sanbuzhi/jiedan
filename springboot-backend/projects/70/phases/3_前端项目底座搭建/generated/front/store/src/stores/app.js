import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    showTabBar: true,
    backTopVisible: false
  }),
  getters: {},
  actions: {
    setShowTabBar(show) {
      this.showTabBar = show
    },
    setBackTopVisible(visible) {
      this.backTopVisible = visible
    }
  },
  persist: {
    key: 'store-app',
    storage: localStorage
  }
})