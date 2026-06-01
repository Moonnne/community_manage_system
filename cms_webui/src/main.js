import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import InputOtpCompat from './components/InputOtpCompat.vue'
import { installMobileTableActions } from './utils/mobileTableActions'
import './styles/index.css'

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.component('ElInputOtp', InputOtpCompat)

app.use(createPinia())
app.use(router)
app.use(ElementPlus, {
  message: {
    duration: 1000
  }
})
installMobileTableActions()
app.mount('#app')
