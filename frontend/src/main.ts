import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

import App from './App.vue'
import router from './router'
import i18n from './locales'
import { applyTheme } from './config/theme.config'

// Global styles (order matters)
import '@/assets/styles/variables.scss'
import '@/assets/styles/reset.scss'
import '@/assets/styles/global.scss'
import '@/assets/styles/animations.scss'
import '@/assets/styles/element-plus-overrides.scss'
import '@/assets/styles/markdown.scss'

const app = createApp(App)

// Initialize theme
const savedTheme = (localStorage.getItem('smartagent_theme') as 'light' | 'dark') || 'light'
applyTheme(savedTheme)

const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(i18n)
app.use(ElementPlus)

app.mount('#app')
