<template>
  <div class="back-top" v-show="visible" @click="backToTop">
    <van-icon name="upgrade" size="24" color="#fff" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  visibilityHeight: {
    type: Number,
    default: 300
  },
  right: {
    type: [String, Number],
    default: 20
  },
  bottom: {
    type: [String, Number],
    default: 40
  }
})

const visible = ref(false)

const handleScroll = () => {
  visible.value = window.pageYOffset > props.visibilityHeight
}

const backToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.back-top {
  position: fixed;
  right: v-bind('typeof right === "number" ? right + "px" : right');
  bottom: v-bind('typeof bottom === "number" ? bottom + "px" : bottom');
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background-color: #07c160;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.15);
  cursor: pointer;
  z-index: 999;
  transition: opacity 0.3s;
}
</style>