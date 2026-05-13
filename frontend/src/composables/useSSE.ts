import { ref } from 'vue'
import { fetchEventSource } from '@microsoft/fetch-event-source'
import { API_BASE_URL, SSE_TIMEOUT } from '@/config/api.config'
import { getAccessToken } from '@/utils/token'
import type { StreamCallbacks, StreamState } from '@/types/sse'

export function useSSE() {
  const state = ref<StreamState>('idle')
  const error = ref<string | null>(null)
  const abortController = ref<AbortController | null>(null)

  async function startStream(
    url: string,
    body: Record<string, unknown>,
    callbacks: StreamCallbacks,
  ): Promise<void> {
    state.value = 'connecting'
    error.value = null

    const ctrl = new AbortController()
    abortController.value = ctrl

    // Timeout
    const timeoutId = setTimeout(() => {
      ctrl.abort()
    }, SSE_TIMEOUT)

    try {
      await fetchEventSource(`${API_BASE_URL}${url}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${getAccessToken()}`,
        },
        body: JSON.stringify(body),
        signal: ctrl.signal,
        openWhenHidden: true,
        onopen() {
          state.value = 'streaming'
          return Promise.resolve()
        },
        onmessage(ev) {
          const data = ev.data
          if (!data) return

          // Check for usage metadata
          if (typeof data === 'string' && data.startsWith('__SMART_AGENT_USAGE__:')) {
            try {
              const usageJson = data.slice('__SMART_AGENT_USAGE__:'.length)
              const usage = JSON.parse(usageJson)
              callbacks.onUsage({
                promptTokens: usage.promptTokens || 0,
                completionTokens: usage.completionTokens || 0,
                totalTokens: usage.totalTokens || 0,
              })
            } catch {
              // ignore
            }
            return
          }

          // Check for error
          try {
            const parsed = JSON.parse(data)
            if (parsed.error) {
              callbacks.onError(parsed.error)
              return
            }
          } catch {
            // not JSON, forward as text
          }

          if (typeof data === 'string') {
            callbacks.onMessage(data)
          }
        },
        onclose() {
          clearTimeout(timeoutId)
          state.value = 'completed'
          abortController.value = null
          callbacks.onComplete()
        },
        onerror(err) {
          clearTimeout(timeoutId)
          state.value = 'error'
          error.value = String(err)
          abortController.value = null
          callbacks.onError(String(err))
          throw err
        },
      })
    } catch {
      clearTimeout(timeoutId)
      if (state.value === 'connecting' || state.value === 'streaming') {
        state.value = 'error'
      }
      abortController.value = null
    }
  }

  function stopStream(): void {
    if (abortController.value) {
      abortController.value.abort()
      abortController.value = null
    }
    state.value = 'completed'
  }

  return { state, error, startStream, stopStream }
}
