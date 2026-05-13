export interface User {
  id: number
  username: string
  nickname: string
  avatar: string | null
  status: number
  roleId: number
  createTime: string
  updateTime: string
}

export interface UserUpdateRequest {
  nickname?: string
  avatar?: string
}

export interface ApiKey {
  id: number
  keyValue: string
  name: string
  status: number
  expireAt: string | null
  lastUsedAt: string | null
  createTime: string
}
