<script setup lang="ts">
import { computed } from 'vue'
import type { Message } from '@/types/chat'
import { useAuthStore } from '@/stores/auth.store'
import UserAvatar from '@/components/common/UserAvatar.vue'
import MarkdownRenderer from '@/components/common/MarkdownRenderer.vue'
import TokenUsageBadge from './TokenUsageBadge.vue'

const props = defineProps<{
  message: Message
  isStreaming?: boolean
}>()

const authStore = useAuthStore()

const isUser = computed(() => props.message.role === 'USER')
const hasTokens = computed(() =>
  props.message.promptTokens != null || props.message.completionTokens != null,
)
</script>

<template>
  <div
    class="chat-bubble"
    :class="{
      'bubble-user': isUser,
      'bubble-ai': !isUser,
      'animate-slide-in-right': isUser,
      'animate-slide-in-left': !isUser,
    }"
  >
    <!-- AI avatar -->
    <div v-if="!isUser" class="bubble-avatar">
      <div class="ai-avatar">🤖</div>
    </div>

    <!-- Message content -->
    <div class="bubble-content">
      <div class="bubble-body">
        <template v-if="isUser">
          <p class="user-text">{{ message.content }}</p>
        </template>
        <template v-else>
          <MarkdownRenderer
            :content="message.content"
            :is-streaming="isStreaming ?? false"
          />
          <TokenUsageBadge
            v-if="hasTokens && !isStreaming"
            :prompt-tokens="message.promptTokens ?? 0"
            :completion-tokens="message.completionTokens ?? 0"
            :total-tokens="message.totalTokens ?? 0"
            :model-name="message.modelName"
            :finish-reason="message.finishReason"
          />
        </template>
      </div>
    </div>

    <!-- User avatar -->
    <div v-if="isUser" class="bubble-avatar">
      <UserAvatar
        :src="authStore.user?.avatar ?? null"
        :nickname="authStore.user?.nickname || authStore.user?.username || '?'"
        size="sm"
      />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.chat-bubble {
  display: flex;
  gap: 10px;
  padding: 8px 0;
  animation-duration: 0.35s;
  animation-fill-mode: both;
  animation-timing-function: ease;

  &.bubble-user {
    flex-direction: row-reverse;
  }
}

.animate-slide-in-right {
  animation-name: slide-in-right;
}

.animate-slide-in-left {
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
  word-break: break-word;
  overflow-wrap: break-word;

  .bubble-user & {
    background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
    color: var(--color-text-on-primary);
    border-bottom-right-radius: var(--radius-sm);
  }

  .bubble-ai & {
    background-color: var(--color-bg-secondary);
    border: 1px solid var(--color-border-light);
    border-bottom-left-radius: var(--radius-sm);
    box-shadow: var(--shadow-sm);
  }
}

.user-text {
  line-height: 1.6;
  white-space: pre-wrap;
}
</style>
