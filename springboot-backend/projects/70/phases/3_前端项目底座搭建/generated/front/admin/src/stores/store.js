import { defineStore } from 'pinia'

export const useStoreStore = defineStore('store', {
  state: () => ({
    storeInfo: null
  }),
  actions: {
    setStoreInfo(info) {
      this.storeInfo = info
    }
  },
  persist: {
    key: 'tongquyouyi-admin-store',
    storage: localStorage
  }
})