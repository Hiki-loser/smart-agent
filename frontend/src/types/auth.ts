export interface LoginRequest {
  username: string
  password: string
  deviceType?: string
  deviceId?: string
}

export interface RegisterRequest {
  username: string
  password: string
  nickname?: string
  deviceType?: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: UserInfo
}

export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar: string | null
  status: number
  roleId: number
  createTime: string
  updateTime: string
}

export interface TokenInfo {
  accessToken: string
  refreshToken: string
  expiresAt: number
}
