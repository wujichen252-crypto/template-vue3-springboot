export interface User {
  id: number
  username: string
  email: string
  avatarUrl: string
  status: number
  createdAt: string
  updatedAt: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  email: string
}

export interface TokenResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
}

export interface UserResponse {
  id: number
  username: string
  email: string
  avatarUrl: string
}

export interface PageQuery {
  page?: number
  pageSize?: number
}
