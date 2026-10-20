import { createI18n } from 'vue-i18n'
import zhCN from './zh-CN'
import enUS from './en-US'

export const SUPPORTED_LOCALES = ['zh-CN', 'en-US']
export const LOCALE_KEY = 'locale'

/** 取当前语言：localStorage 优先，其次浏览器语言，最后默认中文 */
export function getInitialLocale() {
  const saved = localStorage.getItem(LOCALE_KEY)
  if (saved && SUPPORTED_LOCALES.includes(saved)) return saved
  const nav = (navigator.language || '').toLowerCase()
  if (nav.startsWith('en')) return 'en-US'
  return 'zh-CN'
}

const i18n = createI18n({
  legacy: false,
  globalInjection: true,
  locale: getInitialLocale(),
  fallbackLocale: 'zh-CN',
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS
  }
})

/** 切换语言：写入 localStorage 并更新 i18n */
export function setLocale(locale) {
  if (!SUPPORTED_LOCALES.includes(locale)) return
  i18n.global.locale.value = locale
  localStorage.setItem(LOCALE_KEY, locale)
  document.querySelector('html').setAttribute('lang', locale)
}

export default i18n
