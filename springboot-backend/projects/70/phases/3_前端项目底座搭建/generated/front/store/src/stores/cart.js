import { defineStore } from 'pinia'
import { getCartList, addCartItem, updateCartItem, deleteCartItem, selectCartItems, clearCart } from '@/api/product'

export const useCartStore = defineStore('cart', {
  state: () => ({
    cartList: [],
    loading: false
  }),
  getters: {
    cartCount: (state) => state.cartList.reduce((sum, item) => sum + item.quantity, 0),
    selectedItems: (state) => state.cartList.filter(item => item.selected),
    selectedCount: (state) => state.selectedItems.reduce((sum, item) => sum + item.quantity, 0),
    totalPrice: (state) => state.selectedItems.reduce((sum, item) => sum + item.price * item.quantity, 0)
  },
  actions: {
    async getCartList() {
      this.loading = true
      try {
        const res = await getCartList()
        this.cartList = res.data.map(item => ({ ...item, selected: true }))
        return res
      } catch (error) {
        return Promise.reject(error)
      } finally {
        this.loading = false
      }
    },
    async addCartItem(data) {
      try {
        const res = await addCartItem(data)
        await this.getCartList()
        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },
    async updateCartItem(id, quantity) {
      try {
        const res = await updateCartItem(id, quantity)
        const index = this.cartList.findIndex(item => item.id === id)
        if (index !== -1) {
          this.cartList[index].quantity = quantity
        }
        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },
    async deleteCartItem(ids) {
      try {
        const res = await deleteCartItem(ids)
        this.cartList = this.cartList.filter(item => !ids.includes(item.id))
        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },
    async selectCartItems(ids, selected) {
      try {
        const res = await selectCartItems(ids, selected)
        this.cartList.forEach(item => {
          if (ids.includes(item.id)) {
            item.selected = selected
          }
        })
        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },
    async selectAll(selected) {
      const ids = this.cartList.map(item => item.id)
      await this.selectCartItems(ids, selected)
    },
    async clearSelected() {
      const ids = this.selectedItems.map(item => item.id)
      await this.deleteCartItem(ids)
    }
  },
  persist: {
    key: 'store-cart',
    storage: localStorage
  }
})