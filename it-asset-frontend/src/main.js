import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
// 引入路由
import router from './router'

// 静默 ResizeObserver 无害警告
const e = window.onerror
window.onerror = function(msg) { if (String(msg).includes('ResizeObserver')) return; if (e) return e.apply(this, arguments) }

createApp(App)
  .use(ElementPlus, { locale: zhCn })
  .use(router)
  .mount('#app')