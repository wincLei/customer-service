import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

import App from './App.vue'
import router from './router'
import i18n from './locales'

import { logger } from './utils/logger'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(i18n)
app.use(ElementPlus)


if (import.meta.env.DEV) {
	logger.info('App starting in DEV mode, log level:', import.meta.env.VITE_LOG_LEVEL)
}

app.mount('#app')
