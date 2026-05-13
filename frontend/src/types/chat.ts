export interface Session {
  id: string
  title: string
  agentType: string
  messageCount: number
  roundCount: number
  lastMessageAt: string
  shouldCreateNewSession?: boolean
  sessionHint?: string
}

export interface Message {
  role: 'USER' | 'ASSISTANT'
  content: string
  modelName?: string
  promptTokens?: number
  completionTokens?: number
  totalTokens?: number
  finishReason?: string
  createdAt?: string
}

export interface ChatRequest {
  sessionId: string
  content: string
  agentType?: string
}

export interface CreateSessionRequest {
  title?: string
  agentType?: string
  knowledgeBaseId?: string
}

export interface UsageMetadata {
  promptTokens: number
  completionTokens: number
  totalTokens: number
  modelName?: string
  finishReason?: string
}
