import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getAddressList, addAddress, updateAddress, deleteAddress, setDefaultAddress } from '@/api/address'

export const useAddressStore = defineStore('address', () => {
  const addressList = ref([])

  async function fetchAddressList() {
    const res = await getAddressList()
    addressList.value = res.data
  }

  async function handleAddAddress(data) {
    const res = await addAddress(data)
    if (data.isDefault) {
      addressList.value.forEach(item => item.isDefault = false)
    }
    addressList.value.unshift(res.data)
  }

  async function handleUpdateAddress(id, data) {
    const res = await updateAddress(id, data)
    const index = addressList.value.findIndex(item => item.id === id)
    if (index !== -1) {
      if (data.isDefault) {
        addressList.value.forEach(item => item.isDefault = false)
      }
      addressList.value[index] = res.data
    }
  }

  async function handleDeleteAddress(id) {
    await deleteAddress(id)
    addressList.value = addressList.value.filter(item => item.id !== id)
  }

  async function handleSetDefaultAddress(id) {
    await setDefaultAddress(id)
    addressList.value.forEach(item => {
      item.isDefault = item.id === id
    })
  }

  const defaultAddress = computed(() => {
    return addressList.value.find(item => item.isDefault) || addressList.value[0]
  })

  return {
    addressList,
    defaultAddress,
    fetchAddressList,
    handleAddAddress,
    handleUpdateAddress,
    handleDeleteAddress,
    handleSetDefaultAddress
  }
})