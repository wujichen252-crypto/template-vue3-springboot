<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const userInfo = ref(authStore.userInfo)

onMounted(async () => {
  if (!userInfo.value) {
    await authStore.fetchUserInfo()
    userInfo.value = authStore.userInfo
  }
})
</script>

<template>
  <div class="dashboard-container">
    <div class="dashboard-header">
      <h1 class="dashboard-title">仪表盘</h1>
      <div class="user-info">
        <span class="username">{{ userInfo?.username || 'User' }}</span>
        <el-button @click="authStore.logout()" type="danger" size="small">
          退出登录
        </el-button>
      </div>
    </div>
    <div class="dashboard-content">
      <el-card class="welcome-card">
        <template #header>
          <span>欢迎回来</span>
        </template>
        <div class="welcome-content">
          <p>您好，{{ userInfo?.username || 'User' }}！</p>
          <p v-if="userInfo?.email">邮箱：{{ userInfo.email }}</p>
        </div>
      </el-card>
      <el-card class="stats-card">
        <template #header>
          <span>系统信息</span>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="前端框架">Vue 3.4 + TypeScript</el-descriptions-item>
          <el-descriptions-item label="后端框架">Spring Boot 3.2</el-descriptions-item>
          <el-descriptions-item label="UI 库">Element Plus</el-descriptions-item>
          <el-descriptions-item label="样式方案">Tailwind CSS</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.dashboard-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding-bottom: 16px;
  border-bottom: 1px solid #E5E5E5;
}

.dashboard-title {
  font-size: 28px;
  font-weight: 600;
  color: #000000;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.username {
  font-size: 16px;
  color: #666666;
}

.dashboard-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 24px;
}

.welcome-card,
.stats-card {
  border-radius: 8px;
}

.welcome-content p {
  margin: 8px 0;
  color: #666666;
}
</style>
