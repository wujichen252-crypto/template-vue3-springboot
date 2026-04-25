import { describe, it, expect, beforeEach } from 'vitest'
import {
  getAccessToken,
  setAccessToken,
  removeAccessToken,
  getRefreshToken,
  setRefreshToken,
  removeRefreshToken,
  getUserInfo,
  setUserInfo,
  removeUserInfo,
  clearAuth
} from './storage'

describe('storage', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  describe('access token', () => {
    it('should set and get access token', () => {
      const token = 'test-access-token'
      setAccessToken(token)
      expect(getAccessToken()).toBe(token)
    })

    it('should return null when access token not set', () => {
      expect(getAccessToken()).toBeNull()
    })

    it('should remove access token', () => {
      setAccessToken('test-token')
      removeAccessToken()
      expect(getAccessToken()).toBeNull()
    })
  })

  describe('refresh token', () => {
    it('should set and get refresh token', () => {
      const token = 'test-refresh-token'
      setRefreshToken(token)
      expect(getRefreshToken()).toBe(token)
    })

    it('should return null when refresh token not set', () => {
      expect(getRefreshToken()).toBeNull()
    })

    it('should remove refresh token', () => {
      setRefreshToken('test-token')
      removeRefreshToken()
      expect(getRefreshToken()).toBeNull()
    })
  })

  describe('user info', () => {
    it('should set and get user info', () => {
      const userInfo = { id: 1, username: 'test' }
      setUserInfo(userInfo)
      expect(getUserInfo()).toEqual(userInfo)
    })

    it('should return null when user info not set', () => {
      expect(getUserInfo()).toBeNull()
    })

    it('should remove user info', () => {
      setUserInfo({ id: 1 })
      removeUserInfo()
      expect(getUserInfo()).toBeNull()
    })

    it('should handle complex user info object', () => {
      const userInfo = {
        id: 1,
        username: 'test',
        email: 'test@example.com',
        roles: ['admin', 'user']
      }
      setUserInfo(userInfo)
      expect(getUserInfo()).toEqual(userInfo)
    })
  })

  describe('clear auth', () => {
    it('should clear all auth data', () => {
      setAccessToken('access')
      setRefreshToken('refresh')
      setUserInfo({ id: 1 })

      clearAuth()

      expect(getAccessToken()).toBeNull()
      expect(getRefreshToken()).toBeNull()
      expect(getUserInfo()).toBeNull()
    })
  })
})
