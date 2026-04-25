import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useWebVitals } from './useWebVitals'
import * as webVitals from 'web-vitals'

// Mock web-vitals module
vi.mock('web-vitals', () => ({
  onCLS: vi.fn(),
  onFCP: vi.fn(),
  onFID: vi.fn(),
  onLCP: vi.fn(),
  onTTFB: vi.fn()
}))

describe('useWebVitals', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should register all web vitals callbacks', () => {
    useWebVitals()

    expect(webVitals.onCLS).toHaveBeenCalled()
    expect(webVitals.onFCP).toHaveBeenCalled()
    expect(webVitals.onFID).toHaveBeenCalled()
    expect(webVitals.onLCP).toHaveBeenCalled()
    expect(webVitals.onTTFB).toHaveBeenCalled()
  })

  it('should call callbacks with logMetric function', () => {
    useWebVitals()

    const mockMetric = {
      name: 'CLS',
      value: 0.1,
      rating: 'good'
    }

    // Simulate calling the callback
    const clsCallback = vi.mocked(webVitals.onCLS).mock.calls[0][0]
    
    // Should not throw when called
    expect(() => clsCallback(mockMetric as any)).not.toThrow()
  })
})
