<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import MarkdownRenderer from '@/components/common/MarkdownRenderer.vue'

const props = defineProps<{
  content: string
  isStreaming: boolean
}>()

const { t } = useI18n()

const isEmpty = computed(() => !props.content && props.isStreaming)
</script>

<template>
  <div class="streaming-bubble animate-slide-in-left">
    <div class="bubble-avatar">
      <div class="ai-avatar">🤖</div>
    </div>
    <div class="bubble-content">
      <div class="bubble-body">
        <!-- Thinking indicator when no content yet -->
        <div v-if="isEmpty" class="thinking-indicator">
          <span class="dot"></span>
          <span class="dot"></span>
          <span class="dot"></span>
          <span class="thinking-text">{{ t('chat.thinking') }}</span>
        </div>

        <!-- Streaming content -->
        <MarkdownRenderer
          v-else
          :content="content"
          :is-streaming="isStreaming"
        />
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.streaming-bubble {
  display: flex;
  gap: 10px;
  padding: 8px 0;
  animation-duration: 0.35s;
  animation-fill-mode: both;
  animation-timing-function: ease;
  animation-name: slide-in-left;
}

.bubble-avatar {
  flex-shrink: 0;
  margin-top: 4px;
}

.ai-avatar {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.bubble-content {
  max-width: 75%;
  min-width: 0;
}

.bubble-body {
  padding: 12px 16px;
  border-radius: var(--radius-lg);
  border-bottom-left-radius: var(--radius-sm);
  background-color: var(--color-bg-secondary);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-sm);
}

.thinking-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 0;

  .dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background-color: var(--color-primary);
    animation: pulse-soft 1.5s ease-in-out infinite;

    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }

  .thinking-text {
    margin-left: 8px;
    font-size: var(--font-size-sm);
    color: var(--color-text-muted);
  }
}
</style>
