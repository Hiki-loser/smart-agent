import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Session, Message, UsageMetadata } from '@/types/chat'
import * as chatApi from '@/api/chat'
import { fetchEventSource } from '@microsoft/fetch-event-source'
import { API_BASE_URL, ENDPOINTS } from '@/config/api.config'
import { getAccessToken } from '@/utils/token'

const SSE_USAGE_PREFIX = '__SMART_AGENT_USAGE__:'

export const useChatStore = defineStore('chat', () => {
  const sessions = ref<Session[]>([])
  const currentSessionId = ref<string | null>(null)
  const messages = ref<Message[]>([])
  const isStreaming = ref(false)
  const streamingContent = ref('')
  const streamingError = ref<string | null>(null)
  const sessionsLoading = ref(false)
  const messagesLoading = ref(false)
  const abortController = ref<AbortController | null>(null)

  const currentSession = computed(() =>
    sessions.value.find((s) => s.id === currentSessionId.value) ?? null,
  )

  async function fetchSessions(): Promise<void> {
    sessionsLoading.value = true
    try {
      sessions.value = await chatApi.getSessions()
      // Sort by lastMessageAt descending
      sessions.value.sort((a, b) => {
        if (!a.lastMessageAt) return 1
        if (!b.lastMessageAt) return -1
        return new Date(b.lastMessageAt).getTime() - new Date(a.lastMessageAt).getTime()
      })
    } finally {
      sessionsLoading.value = false
    }
  }

  async function createSession(
    title: string,
    agentType: string,
    knowledgeBaseId?: string,
  ): Promise<Session | null> {
    try {
      const session = await chatApi.createSession({
        title: title || undefined,
        agentType: agentType || undefined,
        knowledgeBaseId,
      })
      sessions.value.unshift(session)
      return session
    } catch {
      return null
    }
  }

  function selectSession(sessionId: string): void {
    currentSessionId.value = sessionId
    fetchMessages(sessionId)
  }

  async function fetchMessages(sessionId: string): Promise<void> {
    messagesLoading.value = true
    try {
      messages.value = await chatApi.getMessages(sessionId)
    } catch {
      messages.value = []
    } finally {
      messagesLoading.value = false
    }
  }

  async function sendMessage(content: string, agentType?: string): Promise<boolean> {
    const sessionId = currentSessionId.value
    if (!sessionId || isStreaming.value) return false

    isStreaming.value = true
    streamingContent.value = ''
    streamingError.value = null

    // Add user message to the list immediately
    const userMessage: Message = {
      role: 'USER',
      content,
      createdAt: new Date().toISOString(),
    }
    messages.value.push(userMessage)

    // Add a placeholder AI message
    const aiMessage: Message = {
      role: 'ASSISTANT',
      content: '',
      createdAt: new Date().toISOString(),
    }
    messages.value.push(aiMessage)

    const ctrl = new AbortController()
    abortController.value = ctrl

    let usageMetadata: UsageMetadata | null = null

    try {
      await fetchEventSource(`${API_BASE_URL}${ENDPOINTS.CHAT.MESSAGES}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${getAccessToken()}`,
        },
        body: JSON.stringify({
          sessionId,
          content,
          agentType: agentType || undefined,
        }),
        signal: ctrl.signal,
        openWhenHidden: true,
        onmessage(ev) {
          const data = ev.data
          if (!data) return

          // Check for usage metadata prefix
          if (data.startsWith(SSE_USAGE_PREFIX)) {
            try {
              const usageJson = data.slice(SSE_USAGE_PREFIX.length)
              const usage = JSON.parse(usageJson)
              usageMetadata = {
                promptTokens: usage.promptTokens || usage.prompt_tokens || 0,
                completionTokens: usage.completionTokens || usage.completion_tokens || 0,
                totalTokens: usage.totalTokens || usage.total_tokens || 0,
                modelName: usage.modelName || usage.model_name,
                finishReason: usage.finishReason || usage.finish_reason,
              }
            } catch {
              // ignore parse errors for usage metadata
            }
            return
          }

          // Parse error from SSE
          try {
            const parsed = JSON.parse(data)
            if (parsed.error) {
              streamingError.value = parsed.error
              return
            }
          } catch {
            // not JSON, treat as text chunk
          }

          // Append text chunk
          if (typeof data === 'string' && !data.startsWith('{')) {
            streamingContent.value += data
            aiMessage.content = streamingContent.value
          }
        },
        onclose() {
          // Finalize the AI message with usage metadata
          if (usageMetadata) {
            aiMessage.modelName = usageMetadata.modelName
            aiMessage.promptTokens = usageMetadata.promptTokens
            aiMessage.completionTokens = usageMetadata.completionTokens
            aiMessage.totalTokens = usageMetadata.totalTokens
            aiMessage.finishReason = usageMetadata.finishReason
          }
          isStreaming.value = false
          abortController.value = null

          // Refresh sessions to update metadata
          fetchSessions()
        },
        onerror(err) {
          streamingError.value = String(err)
          throw err
        },
      })
    } catch {
      if (streamingError.value) {
        aiMessage.content = streamingContent.value || `[${streamingError.value}]`
      }
      isStreaming.value = false
      abortController.value = null
      return false
    }

    return true
  }

  function stopStreaming(): void {
    if (abortController.value) {
      abortController.value.abort()
      abortController.value = null
    }
    if (isStreaming.value) {
      isStreaming.value = false
      // Refresh sessions after stop
      fetchSessions()
    }
  }

  function clearCurrentSession(): void {
    currentSessionId.value = null
    messages.value = []
  }

  return {
    sessions,
    currentSessionId,
    messages,
    isStreaming,
    streamingContent,
    streamingError,
    sessionsLoading,
    messagesLoading,
    currentSession,
    fetchSessions,
    createSession,
    selectSession,
    fetchMessages,
    sendMessage,
    stopStreaming,
    clearCurrentSession,
  }
})
