import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    sidebarCollapsed: false,
    theme: 'light',
    fixedHeader: true
  }),
  getters: {},
  actions: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },
    setTheme(theme) {
      this.theme = theme
    },
    setFixedHeader(fixed) {
      this.fixedHeader = fixed
    }
  },
  persist: {
    key: 'admin-app',
    storage: localStorage
  }
})