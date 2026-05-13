import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)

export function formatDateTime(date: string | null | undefined): string {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

export function formatDate(date: string | null | undefined): string {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD')
}

export function formatRelativeTime(date: string | null | undefined, locale: string = 'zh-CN'): string {
  if (!date) return ''

  const localeMap: Record<string, string> = {
    'zh-CN': 'zh-cn',
    en: 'en',
  }
  dayjs.locale(localeMap[locale] || 'zh-cn')

  const d = dayjs(date)
  const now = dayjs()
  const diffMinutes = now.diff(d, 'minute')
  const diffHours = now.diff(d, 'hour')
  const diffDays = now.diff(d, 'day')

  if (diffMinutes < 1) return locale === 'zh-CN' ? '刚刚' : 'just now'
  if (diffMinutes < 60) return locale === 'zh-CN' ? `${diffMinutes}分钟前` : `${diffMinutes}m ago`
  if (diffHours < 24) return locale === 'zh-CN' ? `${diffHours}小时前` : `${diffHours}h ago`
  if (diffDays < 30) return locale === 'zh-CN' ? `${diffDays}天前` : `${diffDays}d ago`
  return formatDate(date)
}

export function formatNumber(n: number): string {
  if (n >= 1000000) return (n / 1000000).toFixed(1) + 'M'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'K'
  return n.toString()
}
