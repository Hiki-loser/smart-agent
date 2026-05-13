<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import type { Message } from '@/types/chat'
import ChatBubble from './ChatBubble.vue'
import StreamingBubble from './StreamingBubble.vue'
import { isNearBottom, scrollToBottom } from '@/utils/dom'

const props = defineProps<{
  messages: Message[]
  isStreaming: boolean
  streamingContent: string
}>()

const listRef = ref<HTMLElement | null>(null)
const shouldAutoScroll = ref(true)

// Auto-scroll when new messages arrive, but only if user is near bottom
watch(
  () => props.messages.length,
  async () => {
    await nextTick()
    if (shouldAutoScroll.value && listRef.value) {
      scrollToBottom(listRef.value)
    }
  },
)

// Always scroll during streaming
watch(
  () => props.streamingContent,
  async () => {
    if (props.isStreaming) {
      await nextTick()
      if (listRef.value) {
        scrollToBottom(listRef.value)
      }
    }
  },
)

function handleScroll() {
  if (listRef.value) {
    shouldAutoScroll.value = isNearBottom(listRef.value)
  }
}
</script>

<template>
  <div
    ref="listRef"
    class="message-list"
    @scroll="handleScroll"
  >
    <div class="message-list-inner">
      <!-- Render completed messages -->
      <ChatBubble
        v-for="(msg, idx) in messages"
        :key="idx"
        :message="msg"
        :is-streaming="false"
      />

      <!-- Streaming bubble overlay (always at the bottom) -->
      <StreamingBubble
        v-if="isStreaming"
        :content="streamingContent"
        :is-streaming="isStreaming"
      />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.message-list {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 16px 24px;
}

.message-list-inner {
  max-width: 820px;
  margin: 0 auto;
}

@media (max-width: 768px) {
  .message-list {
    padding: 12px 16px;
  }
}
</style>
