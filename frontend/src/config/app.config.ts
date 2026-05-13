export const APP_NAME = import.meta.env.VITE_APP_NAME || 'SmartAgent'
export const APP_VERSION = import.meta.env.VITE_APP_VERSION || '1.0.0'

export const SIDEBAR_DEFAULT_COLLAPSED = false
export const MESSAGE_LIST_PAGE_SIZE = 50
export const TOKEN_REFRESH_BUFFER_SECONDS = 300
export const DEVICE_ID_STORAGE_KEY = 'smartagent_device_id'
export const LOCALE_STORAGE_KEY = 'smartagent_locale'
export const THEME_STORAGE_KEY = 'smartagent_theme'
export const AUTH_STORAGE_KEY = 'smartagent_auth'

export const DEFAULT_LOCALE = 'zh-CN'
export const SUPPORTED_LOCALES = ['zh-CN', 'en'] as const
export type SupportedLocale = (typeof SUPPORTED_LOCALES)[number]
