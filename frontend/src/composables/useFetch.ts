import { ref, shallowRef } from 'vue'
import type { AxiosRequestConfig } from 'axios'
import request from '../utils/request'
import type { CancelTokenSource } from 'axios'
import { createCancelToken } from '../utils/request'

export interface FetchState<T> {
  data: T | null
  loading: boolean
  error: Error | null
}

export function useFetch<T>(url?: string, config?: AxiosRequestConfig) {
  const data = shallowRef<T | null>(null)
  const loading = ref(false)
  const error = shallowRef<Error | null>(null)
  let cancelSource: CancelTokenSource | null = null

  const fetch = async (fetchUrl?: string, fetchConfig?: AxiosRequestConfig) => {
    if (cancelSource) {
      cancelSource.cancel('请求被取消')
    }
    cancelSource = createCancelToken()
    loading.value = true
    error.value = null
    try {
      const res = await request.get(fetchUrl || url || '', { ...fetchConfig || config, cancelToken: cancelSource.token })
      data.value = res.data as T
    } catch (e) {
      if ((e as Error).message !== '请求被取消') {
        error.value = e as Error
      }
    } finally {
      loading.value = false
    }
  }

  const post = async (fetchUrl?: string, payload?: unknown, fetchConfig?: AxiosRequestConfig) => {
    if (cancelSource) {
      cancelSource.cancel('请求被取消')
    }
    cancelSource = createCancelToken()
    loading.value = true
    error.value = null
    try {
      const res = await request.post(fetchUrl || url || '', payload, { ...fetchConfig || config, cancelToken: cancelSource.token })
      data.value = res.data as T
    } catch (e) {
      if ((e as Error).message !== '请求被取消') {
        error.value = e as Error
      }
    } finally {
      loading.value = false
    }
  }

  const cancel = (): void => {
    if (cancelSource) {
      cancelSource.cancel('主动取消请求')
    }
  }

  return {
    data,
    loading,
    error,
    fetch,
    post,
    cancel
  }
}
