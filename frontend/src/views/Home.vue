<script setup lang="ts">
</script>

<template>
  <div class="home-container">
    <div class="hero">
      <h1 class="hero-title">
        Vue3 + Spring Boot 模板项目
      </h1>
      <p class="hero-subtitle">
        前后端分离架构，快速启动你的下一个项目
      </p>
      <div class="hero-actions">
        <router-link
          to="/login"
          class="btn btn-primary"
        >
          快速开始
        </router-link>
        <router-link
          to="/dashboard"
          class="btn btn-secondary"
        >
          查看示例
        </router-link>
      </div>
    </div>
    <div class="features">
      <div class="feature-item">
        <h3>Vue 3 + TypeScript</h3>
        <p>使用最新的 Vue 3 Composition API 和 TypeScript 开发</p>
      </div>
      <div class="feature-item">
        <h3>Spring Boot 3</h3>
        <p>现代化的 Java 后端框架，安全认证与 RESTful API</p>
      </div>
      <div class="feature-item">
        <h3>黑白极简设计</h3>
        <p>简洁优雅的 UI 设计风格</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  padding: 80px 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.hero {
  text-align: center;
  padding: 60px 0;
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  color: #000000;
  margin-bottom: 16px;
}

.hero-subtitle {
  font-size: 20px;
  color: #666666;
  margin-bottom: 40px;
}

.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.btn {
  padding: 12px 32px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.3s;
}

.btn-primary {
  background-color: #000000;
  color: #FFFFFF;
}

.btn-primary:hover {
  background-color: #333333;
  text-decoration: none;
}

.btn-secondary {
  background-color: #FFFFFF;
  color: #000000;
  border: 1px solid #000000;
}

.btn-secondary:hover {
  background-color: #F5F5F5;
  text-decoration: none;
}

.features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 32px;
  margin-top: 80px;
}

.feature-item {
  padding: 32px;
  background: #FFFFFF;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.feature-item h3 {
  font-size: 20px;
  font-weight: 600;
  color: #000000;
  margin-bottom: 12px;
}

.feature-item p {
  font-size: 16px;
  color: #666666;
  line-height: 1.6;
}
</style>
