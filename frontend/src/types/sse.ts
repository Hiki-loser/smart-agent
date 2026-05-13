export interface SSEEvent {
  data: string
  event?: string
  id?: string
  retry?: number
}

export type StreamState = 'idle' | 'connecting' | 'streaming' | 'completed' | 'error'

export interface StreamCallbacks {
  onMessage: (chunk: string) => void
  onUsage: (usage: { promptTokens: number; completionTokens: number; totalTokens: number }) => void
  onComplete: () => void
  onError: (error: string) => void
}
