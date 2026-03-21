<template>
  <div class="user-profile-edit-page">
    <van-nav-bar title="编辑个人信息" left-arrow @click-left="goBack" />
    <div class="edit-content">
      <van-form ref="formRef" @submit="handleSubmit">
        <van-cell-group inset>
          <van-field
            v-model="form.nickname"
            name="nickname"
            label="昵称"
            placeholder="请输入昵称"
            :rules="[{ required: true, message: '请输入昵称' }]"
          />
          <van-field
            v-model="form.avatar"
            name="avatar"
            label="头像"
            readonly
            is-link
            placeholder="请选择头像"
            @click="showAvatarPicker = true"
          >
            <template #input>
              <img v-if="form.avatar" :src="form.avatar" class="avatar-preview" />
            </template>
          </van-field>
          <van-field
            v-model="form.gender"
            name="gender"
            label="性别"
            is-link
            readonly
            :placeholder="genderOptions.find(item => item.value === form.gender)?.label || '请选择性别'"
            @click="showGenderPicker = true"
          />
          <van-field
            v-model="form.birthday"
            name="birthday"
            label="生日"
            is-link
            readonly
            placeholder="请选择生日"
            @click="showBirthdayPicker = true"
          />
        </van-cell-group>
        <div class="submit-btn">
          <van-button round block type="primary" native-type="submit" :loading="loading">保存修改</van-button>
        </div>
      </van-form>

      <!-- 性别选择器 -->
      <van-popup v-model:show="showGenderPicker" position="bottom">
        <van-picker
          :columns="genderOptions"
          @confirm="onGenderConfirm"
          @cancel="showGenderPicker = false"
        />
      </van-popup>

      <!-- 生日选择器 -->
      <van-popup v-model:show="showBirthdayPicker" position="bottom">
        <van-date-picker
          v-model="birthdayValue"
          :max-date="new Date()"
          @confirm="onBirthdayConfirm"
          @cancel="showBirthdayPicker = false"
        />
      </van-popup>

      <!-- 头像选择器（占位，后续可替换为上传组件） -->
      <van-popup v-model:show="showAvatarPicker" position="bottom">
        <div class="avatar-picker">
          <van-cell title="拍照" is-link @click="handleAvatarSelect('https://via.placeholder.com/100')" />
          <van-cell title="从相册选择" is-link @click="handleAvatarSelect('https://via.placeholder.com/100')" />
          <van-cell title="取消" is-link @click="showAvatarPicker = false" />
        </div>
      </van-popup>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast } from 'vant'
import { getCurrentUser, updateCurrentUser } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { formatDate } from '@/utils/format'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const showGenderPicker = ref(false)
const showBirthdayPicker = ref(false)
const showAvatarPicker = ref(false)
const birthdayValue = ref([new Date().getFullYear(), new Date().getMonth() + 1, new Date().getDate()])

const genderOptions = [
  { text: '男', value: 1 },
  { text: '女', value: 2 },
  { text: '保密', value: 0 }
]

const form = reactive({
  nickname: '',
  avatar: '',
  gender: 0,
  birthday: ''
})

const goBack = () => {
  router.back()
}

const getProfile = async () => {
  try {
    const res = await getCurrentUser()
    Object.assign(form, res.data)
    if (form.birthday) {
      const [year, month, day] = form.birthday.split('-').map(Number)
      birthdayValue.value = [year, month, day]
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
  }
}

const onGenderConfirm = ({ selectedOptions }) => {
  form.gender = selectedOptions[0].value
  showGenderPicker.value = false
}

const onBirthdayConfirm = ({ selectedValues }) => {
  form.birthday = formatDate(new Date(selectedValues[0], selectedValues[1] - 1, selectedValues[2]))
  showBirthdayPicker.value = false
}

const handleAvatarSelect = (url) => {
  form.avatar = url
  showAvatarPicker.value = false
}

const handleSubmit = async () => {
  loading.value = true
  try {
    const res = await updateCurrentUser(form)
    showSuccessToast('修改成功')
    // 更新本地用户信息
    await userStore.getUserInfo()
    router.back()
  } catch (error) {
    console.error('修改用户信息失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getProfile()
})
</script>

<style scoped lang="css">
.user-profile-edit-page {
  min-height: 100vh;
  background-color: #f7f8fa;
}
.edit-content {
  padding: 20px;
}
.avatar-preview {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}
.submit-btn {
  margin-top: 40px;
}
.avatar-picker {
  padding-bottom: env(safe-area-inset-bottom);
}
</style>