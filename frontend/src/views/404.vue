<script setup lang="ts">
import { useRouter, type Router } from 'vue-router'

const router: Router = useRouter()

const goHome = (): void => {
  router.push('/')
}
</script>

<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <h1 class="error-code">404</h1>
      <p class="error-message">页面未找到</p>
      <p class="error-description">抱歉，您访问的页面不存在或已被删除</p>
      <el-button type="primary" size="large" @click="goHome">
        返回首页
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #F5F5F5;
}

.not-found-content {
  text-align: center;
}

.error-code {
  font-size: 120px;
  font-weight: 700;
  color: #000000;
  margin: 0;
  line-height: 1;
}

.error-message {
  font-size: 24px;
  color: #333333;
  margin: 16px 0;
}

.error-description {
  font-size: 16px;
  color: #666666;
  margin-bottom: 32px;
}
</style>
