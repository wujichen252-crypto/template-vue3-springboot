<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, type Router } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router: Router = useRouter()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const loading = ref(false)

const handleLogin = async (): Promise<void> => {
  if (!username.value || !password.value) {
    return
  }
  loading.value = true
  try {
    await authStore.login(username.value, password.value)
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="login-title">登录</h1>
      <el-form @submit.prevent="handleLogin" class="login-form">
        <el-form-item>
          <el-input
            v-model="username"
            placeholder="用户名"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="password"
            type="password"
            placeholder="密码"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            native-type="submit"
            class="login-button"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <router-link to="/register">还没有账号？去注册</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #F5F5F5;
}

.login-card {
  width: 100%;
  max-width: 400px;
  padding: 40px;
  background: #FFFFFF;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.login-title {
  font-size: 24px;
  font-weight: 600;
  color: #000000;
  text-align: center;
  margin-bottom: 32px;
}

.login-form {
  margin-top: 16px;
}

.login-button {
  width: 100%;
}

.login-footer {
  text-align: center;
  margin-top: 16px;
}

.login-footer a {
  color: #2563EB;
  text-decoration: none;
}

.login-footer a:hover {
  text-decoration: underline;
}
</style>
