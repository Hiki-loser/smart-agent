<script setup lang="ts">
import { onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useChatStore } from '@/stores/chat.store'
import { useChat } from '@/composables/useChat'
import ChatMessageList from '@/components/chat/ChatMessageList.vue'
import ChatInput from '@/components/chat/ChatInput.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const chatStore = useChatStore()
const { handleSendMessage, handleStopStreaming } = useChat()

const sessionId = route.params.sessionId as string | undefined

// Load sessions on mount
onMounted(async () => {
  await chatStore.fetchSessions()

  if (sessionId) {
    chatStore.selectSession(sessionId)
  } else {
    chatStore.clearCurrentSession()
  }
})

// Watch route param changes
watch(
  () => route.params.sessionId,
  (newId) => {
    if (newId) {
      chatStore.selectSession(newId)
    } else {
      chatStore.clearCurrentSession()
    }
  },
)

function onSend(content: string, agentType: string) {
  handleSendMessage(content, agentType)
}

function onStop() {
  handleStopStreaming()
}

async function onNewSession() {
  const session = await chatStore.createSession(
    t('chat.defaultSessionTitle'),
    'default',
  )
  if (session) {
    router.push(`/chat/${session.id}`)
  }
}
</script>

<template>
  <div class="chat-view">
    <!-- No session selected: empty state -->
    <template v-if="!chatStore.currentSessionId">
      <div class="chat-empty-wrapper">
        <EmptyState
          :title="(t('chat.noMessages') as string)"
          :description="(t('chat.noMessagesHint') as string)"
          :action-text="(t('chat.newSession') as string)"
          @action="onNewSession"
        />
      </div>
    </template>

    <!-- Session active: show messages and input -->
    <template v-else>
      <!-- Loading -->
      <LoadingSpinner
        v-if="chatStore.messagesLoading"
        :text="(t('common.loading') as string)"
        size="md"
        class="chat-loading"
      />

      <!-- Message list -->
      <ChatMessageList
        v-else
        :messages="chatStore.messages"
        :is-streaming="chatStore.isStreaming"
        :streaming-content="chatStore.streamingContent"
      />

      <!-- Input bar -->
      <ChatInput
        :disabled="false"
        :is-streaming="chatStore.isStreaming"
        @send="onSend"
        @stop="onStop"
      />
    </template>
  </div>
</template>

<style lang="scss" scoped>
.chat-view {
  display: flex;
  flex-direction: column;
  height: 100%;
  position: relative;
}

.chat-empty-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-loading {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
