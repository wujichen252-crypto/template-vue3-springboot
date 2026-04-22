# 产品需求文档

## 概述

`template-vue3-springboot` 是一个前后端分离的基础模板项目，旨在帮助开发者快速搭建现代化的 Web 应用。

## 目标用户

- 需要快速启动新项目的开发者
- 希望使用 Vue 3 + Spring Boot 技术栈的团队
- 需要完整用户认证流程的中小型项目

## 功能需求

### 用户模块

- [x] 用户注册
- [x] 用户登录
- [x] JWT Token 认证
- [x] Token 刷新
- [x] 退出登录
- [x] 获取当前用户信息

### 前端模块

- [x] 登录/注册页面
- [x] 首页
- [x] 仪表盘（需认证）
- [x] 404 页面
- [x] Axios 请求封装
- [x] 路由守卫
- [x] 黑白极简主题

### 后端模块

- [x] RESTful API 设计
- [x] 统一响应格式
- [x] 全局异常处理
- [x] JWT 安全认证
- [x] MyBatis-Plus 集成
- [x] Redis 缓存配置
- [x] CORS 跨域配置

## 非功能需求

### 性能
- 前端：路由懒加载、组件异步导入、图片懒加载
- 后端：连接池优化、SQL 日志按环境控制

### 安全
- 密码 BCrypt 加密存储
- JWT Access Token (2小时) + Refresh Token (7天)
- Spring Security 配置
- 参数校验

### 可维护性
- 分层架构：Controller -> Service -> Mapper
- 统一错误码管理
- 代码注释完整

## 技术选型

### 前端
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4 | 渐进式框架 |
| TypeScript | 5.4 | 类型安全 |
| Vite | 5.2 | 快速构建 |
| Pinia | 2.1 | 状态管理 |
| Element Plus | 2.7 | UI 组件库 |
| Tailwind CSS | 3.4 | 原子化 CSS |

### 后端
| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | LTS 版本 |
| Spring Boot | 3.2 | 框架 |
| Spring Security | 6 | 安全框架 |
| MyBatis-Plus | 3.5 | ORM 框架 |
| MySQL | 8 | 数据库 |
| Redis | 7 | 缓存 |
| JWT | 0.12 | 认证令牌 |

## 目录结构

见 [../README.md](../README.md)

## 交付标准

- [x] `cd frontend && npm install && npm run dev` → 看到首页
- [x] `cd backend && mvn clean install` → 编译成功
- [x] 数据库初始化脚本已提供
- [x] 登录接口可正常调用
- [x] 认证后的请求能正常访问受保护资源
