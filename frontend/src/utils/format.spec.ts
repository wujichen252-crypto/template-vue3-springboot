import { describe, it, expect } from 'vitest'
import { formatDate, formatNumber, formatFileSize } from './format'

describe('format', () => {
  describe('formatDate', () => {
    it('should format date with default format', () => {
      const date = new Date('2024-01-15 10:30:45')
      expect(formatDate(date)).toBe('2024-01-15 10:30:45')
    })

    it('should format date with custom format', () => {
      const date = new Date('2024-01-15 10:30:45')
      expect(formatDate(date, 'YYYY年MM月DD日')).toBe('2024年01月15日')
    })

    it('should format date string', () => {
      expect(formatDate('2024-01-15T10:30:45')).toBe('2024-01-15 10:30:45')
    })

    it('should format with only date', () => {
      const date = new Date('2024-01-15 10:30:45')
      expect(formatDate(date, 'YYYY-MM-DD')).toBe('2024-01-15')
    })

    it('should format with only time', () => {
      const date = new Date('2024-01-15 10:30:45')
      expect(formatDate(date, 'HH:mm:ss')).toBe('10:30:45')
    })
  })

  describe('formatNumber', () => {
    it('should format number with default 2 decimals', () => {
      expect(formatNumber(3.14159)).toBe('3.14')
    })

    it('should format number with custom decimals', () => {
      expect(formatNumber(3.14159, 4)).toBe('3.1416')
    })

    it('should format integer', () => {
      expect(formatNumber(42)).toBe('42.00')
    })

    it('should format zero', () => {
      expect(formatNumber(0)).toBe('0.00')
    })

    it('should format negative number', () => {
      expect(formatNumber(-3.14)).toBe('-3.14')
    })
  })

  describe('formatFileSize', () => {
    it('should format 0 bytes', () => {
      expect(formatFileSize(0)).toBe('0 B')
    })

    it('should format bytes', () => {
      expect(formatFileSize(512)).toBe('512 B')
    })

    it('should format kilobytes', () => {
      expect(formatFileSize(1024)).toBe('1 KB')
    })

    it('should format megabytes', () => {
      expect(formatFileSize(1024 * 1024)).toBe('1 MB')
    })

    it('should format gigabytes', () => {
      expect(formatFileSize(1024 * 1024 * 1024)).toBe('1 GB')
    })

    it('should format with decimal', () => {
      expect(formatFileSize(1536)).toBe('1.5 KB')
    })

    it('should format large file', () => {
      expect(formatFileSize(1024 * 1024 * 1024 * 2.5)).toBe('2.5 GB')
    })
  })
})
