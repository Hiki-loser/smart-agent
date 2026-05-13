import http from './index'
import { ENDPOINTS } from '@/config/api.config'
import type { Session, Message, CreateSessionRequest } from '@/types/chat'

export async function getSessions(): Promise<Session[]> {
  return http.get(ENDPOINTS.CHAT.SESSIONS)
}

export async function createSession(data: CreateSessionRequest): Promise<Session> {
  return http.post(ENDPOINTS.CHAT.SESSIONS, data)
}

export async function getMessages(sessionId: string): Promise<Message[]> {
  return http.get(ENDPOINTS.CHAT.SESSION_MESSAGES(sessionId))
}
