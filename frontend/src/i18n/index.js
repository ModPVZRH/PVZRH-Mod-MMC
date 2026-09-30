import { createI18n } from 'vue-i18n'
import zhCn from './locales/zh-cn'
import en from './locales/en'

export const SUPPORT_LOCALES = [
  { value: 'zh-cn', labelKey: 'layout.chinese' },
  { value: 'en', labelKey: 'layout.english' },
]

export function normalizeLanguage(lang) {
  return lang === 'en' ? 'en' : 'zh-cn'
}

export function getSavedLanguage() {
  try {
    const raw = localStorage.getItem('ModConfig')
    if (!raw) return 'zh-cn'
    const parsed = JSON.parse(raw)
    return normalizeLanguage(parsed.language)
  } catch {
    return 'zh-cn'
  }
}

export const i18n = createI18n({
  legacy: false,
  globalInjection: true,
  locale: getSavedLanguage(),
  fallbackLocale: 'zh-cn',
  messages: {
    'zh-cn': zhCn,
    en,
  },
})

export function applyLanguage(lang) {
  const locale = normalizeLanguage(lang)
  i18n.global.locale.value = locale
  document.documentElement.lang = locale === 'zh-cn' ? 'zh-CN' : 'en'
  document.title = i18n.global.t('app.name')
}

export function tGlobal(key, params) {
  return i18n.global.t(key, params)
}

applyLanguage(getSavedLanguage())

export default i18n
