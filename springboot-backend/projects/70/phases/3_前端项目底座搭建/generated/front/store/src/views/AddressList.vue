<template>
  <div class="address-list">
    <van-nav-bar title="收货地址" left-arrow>
      <template #right>
        <van-icon name="plus" size="20" @click="goAdd" />
      </template>
    </van-nav-bar>
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="onLoad">
        <div v-if="addressStore.addressList.length === 0" class="empty-address">
          <van-empty description="暂无收货地址" />
          <van-button type="primary" size="small" @click="goAdd">添加地址</van-button>
        </div>
        <div v-else class="address-card-list">
          <van-cell-group inset v-for="address in addressStore.addressList" :key="address.id">
            <van-cell @click="handleSelect(address)">
              <template #title>
                <div class="address-header">
                  <span class="name">{{ address.name }}</span>
                  <span class="phone">{{ address.phone }}</span>
                  <van-tag v-if="address.isDefault" type="primary" size="small">默认</van-tag>
                </div>
              </template>
              <template #label>
                <div class="address-detail">{{ address.province }}{{ address.city }}{{ address.district }}{{ address.detail }}</div>
              </template>
            </van-cell>
            <van-cell>
              <template #right-icon>
                <div class="address-actions">
                  <van-button size="small" plain type="primary" @click="goEdit(address)">编辑</van-button>
                  <van-button size="small" plain type="danger" @click="handleDelete(address.id)">删除</van-button>
                  <van-button v-if="!address.isDefault" size="small" plain type="warning" @click="handleSetDefault(address.id)">设为默认</van-button>
                </div>
              </template>
            </van-cell>
          </van-cell-group>
        </div>
      </van-list>
    </van-pull-refresh>
    <AddressForm v-model:visible="formVisible" :address="currentAddress" @success="onFormSuccess" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAddressStore } from '@/stores/address'
import { showConfirmDialog, showToast } from 'vant'
import AddressForm from '@/components/AddressForm.vue'

const route = useRoute()
const router = useRouter()
const addressStore = useAddressStore()
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(true)
const formVisible = ref(false)
const currentAddress = ref(null)

function onLoad() {
  loading.value = false
}

async function onRefresh() {
  try {
    await addressStore.fetchAddressList()
  } catch (error) {
    console.error(error)
  } finally {
    refreshing.value = false
  }
}

function goAdd() {
  currentAddress.value = null
  formVisible.value = true
}

function goEdit(address) {
  currentAddress.value = { ...address }
  formVisible.value = true
}

async function handleDelete(id) {
  try {
    await showConfirmDialog({
      title: '提示',
      message: '确定要删除这个地址吗？'
    })
    await addressStore.handleDeleteAddress(id)
    showToast('删除成功')
  } catch {
  }
}

async function handleSetDefault(id) {
  try {
    await addressStore.handleSetDefaultAddress(id)
    showToast('设置成功')
  } catch (error) {
    console.error(error)
  }
}

function handleSelect(address) {
  if (route.query.select === '1') {
    addressStore.handleSetDefaultAddress(address.id).catch(() => {})
    router.back()
  }
}

function onFormSuccess() {
  formVisible.value = false
  onRefresh()
}

onMounted(() => {
  onRefresh()
})
</script>

<style scoped>
.address-list {
  width: 100%;
  min-height: 100%;
  padding-bottom: 20px;
  background-color: #f5f5f5;
}

.empty-address {
  padding: 60px 20px;
  text-align: center;
}

.empty-address .van-button {
  margin-top: 20px;
}

.address-card-list {
  padding: 0 16px;
}

.address-card-list .van-cell-group {
  margin-bottom: 12px;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.phone {
  font-size: 14px;
  color: #666;
}

.address-detail {
  font-size: 14px;
  color: #666;
  margin-top: 8px;
}

.address-actions {
  display: flex;
  gap: 12px;
}
</style>