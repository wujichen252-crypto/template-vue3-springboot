<script setup lang="ts">
import { ref, onErrorCaptured } from 'vue'

interface Props {
  fallback?: string
}

withDefaults(defineProps<Props>(), {
  fallback: '页面发生错误'
})

const hasError = ref(false)
const errorMessage = ref('')

onErrorCaptured((err: Error) => {
  hasError.value = true
  errorMessage.value = err.message || '未知错误'
  console.error('Error captured:', err)
  return false
})

const handleRetry = (): void => {
  hasError.value = false
  errorMessage.value = ''
  window.location.reload()
}
</script>

<template>
  <div v-if="hasError" class="error-boundary">
    <div class="error-content">
      <div class="error-icon">⚠️</div>
      <h2 class="error-title">出错了</h2>
      <p class="error-message">{{ errorMessage || fallback }}</p>
      <button class="retry-button" @click="handleRetry">重试</button>
    </div>
  </div>
  <slot v-else />
</template>

<style scoped>
.error-boundary {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #F5F5F5;
}

.error-content {
  text-align: center;
  padding: 40px;
  background: #FFFFFF;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  max-width: 400px;
}

.error-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.error-title {
  font-size: 24px;
  font-weight: 600;
  color: #000000;
  margin-bottom: 8px;
}

.error-message {
  font-size: 14px;
  color: #666666;
  margin-bottom: 24px;
}

.retry-button {
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 500;
  color: #FFFFFF;
  background-color: #2563EB;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.retry-button:hover {
  background-color: #1d4ed8;
}
</style>
