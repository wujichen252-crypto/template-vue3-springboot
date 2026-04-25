import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { useFetch } from './useFetch'
import request from '../utils/request'

// Mock request module
vi.mock('../utils/request', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn()
  },
  createCancelToken: vi.fn(() => ({
    token: 'test-token',
    cancel: vi.fn()
  }))
}))

describe('useFetch', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('initialization', () => {
    it('should initialize with default state', () => {
      const { data, loading, error } = useFetch()

      expect(data.value).toBeNull()
      expect(loading.value).toBe(false)
      expect(error.value).toBeNull()
    })

    it('should initialize with url', () => {
      const { data, loading } = useFetch('/api/test')

      expect(data.value).toBeNull()
      expect(loading.value).toBe(false)
    })
  })

  describe('fetch', () => {
    it('should fetch data successfully', async () => {
      const mockData = { id: 1, name: 'test' }
      vi.mocked(request.get).mockResolvedValueOnce({ data: mockData })

      const { data, loading, fetch } = useFetch()

      await fetch('/api/test')

      expect(data.value).toEqual(mockData)
      expect(loading.value).toBe(false)
    })

    it('should handle fetch error', async () => {
      const mockError = new Error('Network error')
      vi.mocked(request.get).mockRejectedValueOnce(mockError)

      const { error, loading, fetch } = useFetch()

      await fetch('/api/test')

      expect(error.value).toEqual(mockError)
      expect(loading.value).toBe(false)
    })

    it('should set loading during fetch', async () => {
      vi.mocked(request.get).mockImplementation(() => new Promise(resolve => {
        setTimeout(() => resolve({ data: {} }), 100)
      }))

      const { loading, fetch } = useFetch()

      const fetchPromise = fetch('/api/test')
      expect(loading.value).toBe(true)

      await fetchPromise
      expect(loading.value).toBe(false)
    })

    it('should use provided url in initialization', async () => {
      const mockData = { result: 'success' }
      vi.mocked(request.get).mockResolvedValueOnce({ data: mockData })

      const { data, fetch } = useFetch('/api/initial')

      await fetch()

      expect(request.get).toHaveBeenCalledWith('/api/initial', expect.any(Object))
    })
  })

  describe('post', () => {
    it('should post data successfully', async () => {
      const mockResponse = { id: 1, created: true }
      vi.mocked(request.post).mockResolvedValueOnce({ data: mockResponse })

      const { data, post } = useFetch()

      await post('/api/create', { name: 'test' })

      expect(data.value).toEqual(mockResponse)
    })

    it('should handle post error', async () => {
      const mockError = new Error('Server error')
      vi.mocked(request.post).mockRejectedValueOnce(mockError)

      const { error, post } = useFetch()

      await post('/api/create', {})

      expect(error.value).toEqual(mockError)
    })

    it('should send correct payload', async () => {
      vi.mocked(request.post).mockResolvedValueOnce({ data: {} })

      const { post } = useFetch()
      const payload = { name: 'test', value: 123 }

      await post('/api/create', payload)

      expect(request.post).toHaveBeenCalledWith('/api/create', payload, expect.any(Object))
    })
  })

  describe('cancel', () => {
    it('should cancel request', () => {
      const { cancel } = useFetch()

      // Should not throw
      expect(() => cancel()).not.toThrow()
    })
  })
})
