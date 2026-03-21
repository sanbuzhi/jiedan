<template>
  <van-popup v-model:visible="visible" position="bottom" round :style="{ height: '80%' }">
    <van-nav-bar title="编辑地址" left-arrow @click-left="visible = false">
      <template #right>
        <van-button type="primary" size="small" plain @click="handleSubmit">保存</van-button>
      </template>
    </van-nav-bar>
    <van-form ref="formRef" @submit="handleSubmit">
      <van-cell-group inset>
        <van-field
          v-model="form.name"
          name="name"
          label="收货人"
          placeholder="请输入收货人姓名"
          :rules="[{ required: true, message: '请输入收货人姓名' }]"
        />
        <van-field
          v-model="form.phone"
          name="phone"
          label="手机号"
          placeholder="请输入手机号"
          :rules="[{ required: true, message: '请输入手机号' }, { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' }]"
        />
        <van-field
          v-model="areaValue"
          name="area"
          label="所在地区"
          placeholder="请选择所在地区"
          readonly
          is-link
          @click="showAreaPicker = true"
          :rules="[{ required: true, message: '请选择所在地区' }]"
        />
        <van-field
          v-model="form.detail"
          name="detail"
          label="详细地址"
          type="textarea"
          placeholder="请输入详细地址"
          :rules="[{ required: true, message: '请输入详细地址' }]"
        />
        <van-cell title="设为默认">
          <template #right-icon>
            <van-switch v-model="form.isDefault" />
          </template>
        </van-cell>
      </van-cell-group>
    </van-form>
    <van-area
      v-model:areaValue"
      :area-list="areaList"
      :visible="showAreaPicker"
      @confirm="onAreaConfirm"
      @cancel="showAreaPicker = false"
    />
  </van-popup>
</template>

<script setup>
import { ref, watch, reactive } from 'vue'
import { showToast } from 'vant'
import { addAddress, updateAddress } from '@/api/address'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  address: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:visible', 'success'])

const formRef = ref(null)
const showAreaPicker = ref(false)
const areaValue = ref('')
const areaList = ref({
  province_list: {
    110000: '北京市',
    310000: '上海市',
    440000: '广东省'
  },
  city_list: {
    110100: '北京市',
    310100: '上海市',
    440100: '广州市',
    440300: '深圳市'
  },
  county_list: {
    110101: '东城区',
    110102: '西城区',
    310101: '黄浦区',
    310104: '徐汇区',
    440103: '荔湾区',
    440104: '越秀区',
    440303: '罗湖区',
    440304: '福田区'
  }
})

const form = reactive({
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false
})

function onAreaConfirm({ selectedOptions }) {
  form.province = selectedOptions[0]?.text || ''
  form.city = selectedOptions[1]?.text || ''
  form.district = selectedOptions[2]?.text || ''
  areaValue.value = selectedOptions.map(option => option.text).join('')
  showAreaPicker.value = false
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (props.address) {
          await updateAddress(props.address.id, form)
        } else {
          await addAddress(form)
        }
        showToast('保存成功')
        emit('success')
      } catch (error) {
        console.error(error)
      }
    }
  })
}

watch(() => props.visible, (val) => {
  if (val) {
    if (props.address) {
      Object.assign(form, props.address)
      areaValue.value = `${form.province}${form.city}${form.district}`
    } else {
      Object.assign(form, {
        name: '',
        phone: '',
        province: '',
        city: '',
        district: '',
        detail: '',
        isDefault: false
      })
      areaValue.value = ''
    }
  }
})

watch(() => props.address, (val) => {
  if (val && props.visible) {
    Object.assign(form, val)
    areaValue.value = `${form.province}${form.city}${form.district}`
  }
})
</script>

<style scoped>
</style>