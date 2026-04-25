import { onCLS, onFCP, onFID, onLCP, onTTFB, type Metric } from 'web-vitals'

interface VitalsMetric {
  name: string
  value: number
  rating: 'good' | 'needs-improvement' | 'poor'
  delta?: number
}

const logMetric = (metric: Metric): void => {
  const vitalsMetric: VitalsMetric = {
    name: metric.name,
    value: Math.round(metric.value * 100) / 100,
    rating: metric.rating
  }

  // 开发环境输出到控制台
  if (import.meta.env.DEV) {
    console.log(`[Web Vitals] ${vitalsMetric.name}:`, vitalsMetric)
  }

  // 生产环境可以发送到分析服务
  if (import.meta.env.PROD) {
    // 示例：发送到分析端点
    // navigator.sendBeacon('/api/v1/analytics/vitals', JSON.stringify(vitalsMetric))
  }
}

export const useWebVitals = (): void => {
  onCLS(logMetric)
  onFCP(logMetric)
  onFID(logMetric)
  onLCP(logMetric)
  onTTFB(logMetric)
}
