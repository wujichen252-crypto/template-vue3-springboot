import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserResponse } from '../types/user'
import { authApi, userApi } from '../api/user'
import { setAccessToken, setRefreshToken, setUserInfo, clearAuth, getAccessToken } from '../utils/storage'

export const useAuthStore = defineStore('auth', () => {
  const userInfo = ref<UserResponse | null>(null)
  const accessToken = ref<string | null>(getAccessToken())

  const isLoggedIn = computed(() => !!accessToken.value)

  const login = async (username: string, password: string): Promise<void> => {
    const res = await authApi.login({ username, password })
    const data = res.data as { accessToken: string; refreshToken: string }
    setAccessToken(data.accessToken)
    setRefreshToken(data.refreshToken)
    accessToken.value = data.accessToken
    await fetchUserInfo()
  }

  const register = async (username: string, password: string, email: string): Promise<void> => {
    const res = await authApi.register({ username, password, email })
    const data = res.data as UserResponse
    setUserInfo(data)
    userInfo.value = data
  }

  const logout = async (): Promise<void> => {
    try {
      await authApi.logout()
    } finally {
      clearAuth()
      accessToken.value = null
      userInfo.value = null
    }
  }

  const fetchUserInfo = async (): Promise<void> => {
    if (!accessToken.value) return
    try {
      const res = await userApi.getCurrentUser()
      userInfo.value = res.data as UserResponse
      setUserInfo(userInfo.value)
    } catch {
      clearAuth()
      accessToken.value = null
      userInfo.value = null
    }
  }

  return {
    userInfo,
    accessToken,
    isLoggedIn,
    login,
    register,
    logout,
    fetchUserInfo
  }
})
