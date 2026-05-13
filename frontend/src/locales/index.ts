import { createI18n } from 'vue-i18n'
import zhCN from './zh-CN.json'
import en from './en.json'
import { DEFAULT_LOCALE, SUPPORTED_LOCALES, LOCALE_STORAGE_KEY, type SupportedLocale } from '@/config/app.config'

const messages = {
  'zh-CN': zhCN,
  en,
}

export function detectLocale(): SupportedLocale {
  const stored = localStorage.getItem(LOCALE_STORAGE_KEY) as SupportedLocale | null
  if (stored && SUPPORTED_LOCALES.includes(stored)) return stored

  const browserLang = navigator.language
  if (browserLang.startsWith('zh')) return 'zh-CN'
  if (browserLang.startsWith('en')) return 'en'

  return DEFAULT_LOCALE
}

export const i18n = createI18n({
  legacy: false,
  locale: detectLocale(),
  fallbackLocale: DEFAULT_LOCALE,
  messages,
  globalInjection: true,
})

export default i18n
