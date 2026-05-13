import { useChatStore } from '@/stores/chat.store'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'

export function useChat() {
  const chatStore = useChatStore()
  const { t } = useI18n()

  async function handleSendMessage(content: string, agentType?: string): Promise<boolean> {
    if (!content.trim()) return false

    // If no session exists, create one first
    if (!chatStore.currentSessionId) {
      const session = await chatStore.createSession(
        t('chat.defaultSessionTitle'),
        agentType || 'default',
      )
      if (!session) {
        ElMessage.error(t('error.unknownError'))
        return false
      }
      chatStore.selectSession(session.id)
    }

    const success = await chatStore.sendMessage(content, agentType)
    if (!success && chatStore.streamingError) {
      ElMessage.error(chatStore.streamingError)
    }
    return success
  }

  function handleStopStreaming(): void {
    chatStore.stopStreaming()
  }

  return {
    handleSendMessage,
    handleStopStreaming,
  }
}
