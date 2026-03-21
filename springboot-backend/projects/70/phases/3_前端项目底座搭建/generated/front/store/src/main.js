import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import Vant from 'vant'
import 'vant/lib/index.css'
import '@vant/touch-emulator'

import App from './App.vue'
import router from './router'
import './assets/base.css'
import './assets/main.css'
import './assets/index.css'

const app = createApp(App)
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

app.use(pinia)
app.use(router)
app.use(Vant)

app.mount('#app')