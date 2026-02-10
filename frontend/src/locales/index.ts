import { createI18n } from 'vue-i18n'
import zh from './zh'
import en from './en'

/**
 * Detect browser language.
 * If the browser language starts with 'zh', use Chinese.
 * Otherwise, default to English.
 */
function getDefaultLocale(): string {
  const browserLang = navigator.language || (navigator as any).userLanguage || 'en'
  return browserLang.toLowerCase().startsWith('zh') ? 'zh' : 'en'
}

const i18n = createI18n({
  legacy: false, // Use Composition API mode
  locale: getDefaultLocale(),
  fallbackLocale: 'en',
  messages: {
    zh,
    en,
  },
})

export default i18n
