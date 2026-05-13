<script setup lang="ts">
import { computed } from 'vue'
import type { Session } from '@/types/chat'
import { useI18n } from 'vue-i18n'

const props = defineProps<{
  session: Session
  active: boolean
}>()

defineEmits<{
  click: []
  delete: []
}>()

const { t } = useI18n()

const relativeTime = computed(() => {
  if (!props.session.lastMessageAt) return ''
  // Simple relative time
  const now = Date.now()
  const then = new Date(props.session.lastMessageAt).getTime()
  const diffMs = now - then
  const diffMin = Math.floor(diffMs / 60000)
  const diffHr = Math.floor(diffMs / 3600000)
  const diffDay = Math.floor(diffMs / 86400000)

  if (diffMin < 1) return t('time.justNow')
  if (diffMin < 60) return t('time.minutesAgo', { n: diffMin })
  if (diffHr < 24) return t('time.hoursAgo', { n: diffHr })
  if (diffDay < 30) return t('time.daysAgo', { n: diffDay })
  return t('time.older')
})

const isLong = computed(() => props.session.shouldCreateNewSession === true)
</script>

<template>
  <div
    class="session-item"
    :class="{ active, 'is-long': isLong }"
    @click="$emit('click')"
  >
    <div class="session-info">
      <div class="session-title-line">
        <span class="session-title">{{ session.title }}</span>
        <span v-if="isLong" class="session-long-dot" :title="(session.sessionHint as string)">●</span>
      </div>
      <div class="session-meta">
        <span class="session-time">{{ relativeTime }}</span>
        <span class="session-count">{{ t('chat.messageCount', { count: session.messageCount }) }}</span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.session-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover {
    background-color: var(--color-border-light);
  }

  &.active {
    background-color: var(--color-primary-light);
    border-left: 3px solid var(--color-primary);
    padding-left: 9px;
  }

  &.is-long .session-long-dot {
    color: var(--color-warning);
    font-size: 8px;
    animation: pulse-soft 2s ease-in-out infinite;
  }
}

.session-info {
  flex: 1;
  min-width: 0;
}

.session-title-line {
  display: flex;
  align-items: center;
  gap: 6px;
}

.session-title {
  font-size: 0.875rem;
  color: var(--color-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 500;
}

.session-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 2px;
}

.session-time,
.session-count {
  font-size: 0.7rem;
  color: var(--color-text-muted);
}
</style>
