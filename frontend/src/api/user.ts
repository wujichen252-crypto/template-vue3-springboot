import type { ApiResult, PageResult, PageQuery } from '../types/api'
import type { User, LoginRequest, RegisterRequest, TokenResponse, UserResponse } from '../types/user'
import request from '../utils/request'

export const authApi = {
  login(data: LoginRequest): Promise<ApiResult<TokenResponse>> {
    return request.post('/v1/auth/login', data)
  },

  register(data: RegisterRequest): Promise<ApiResult<UserResponse>> {
    return request.post('/v1/auth/register', data)
  },

  refreshToken(refreshToken: string): Promise<ApiResult<TokenResponse>> {
    return request.post('/v1/auth/refresh', {}, {
      headers: {
        'X-Refresh-Token': refreshToken
      }
    })
  },

  logout(): Promise<ApiResult<void>> {
    return request.post('/v1/auth/logout')
  }
}

export const userApi = {
  getCurrentUser(): Promise<ApiResult<UserResponse>> {
    return request.get('/v1/users/me')
  },

  updateProfile(data: Partial<User>): Promise<ApiResult<UserResponse>> {
    return request.put('/v1/users/me', data)
  },

  listUsers(params: PageQuery): Promise<ApiResult<PageResult<User>>> {
    return request.get('/v1/users', { params })
  },

  getUserById(id: number): Promise<ApiResult<UserResponse>> {
    return request.get(`/v1/users/${id}`)
  }
}
