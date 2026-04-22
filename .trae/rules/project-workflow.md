# 项目工作流程

## 开发流程

### 1. 分支管理

```
main          # 主分支，保持稳定
develop       # 开发分支
feature/*     # 功能分支
bugfix/*      # 修复分支
hotfix/*      # 紧急修复分支
```

### 2. 开发步骤

1. 从 `develop` 创建功能分支
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/user-auth
   ```

2. 开发并提交
   ```bash
   git add .
   git commit -m "feat: 添加用户认证功能"
   ```

3. 推送并创建 PR
   ```bash
   git push origin feature/user-auth
   ```

4. Code Review 通过后合并到 `develop`

### 3. 发布流程

1. `develop` 合并到 `main`
2. 创建 Tag
   ```bash
   git tag -a v1.0.0 -m "Release v1.0.0"
   git push origin v1.0.0
   ```
3. 构建发布包

## 代码审查

### 提交前检查

- [ ] 代码符合项目规范
- [ ] 必要的注释已添加
- [ ] 没有未使用的导入/变量
- [ ] 单元测试通过
- [ ] `npm run lint` / `mvn clean compile` 通过

### PR 要求

- 标题清晰描述变更内容
- 关联相关 Issue
- 包含测试情况说明
- 添加必要的文档更新

## 环境管理

### 环境分类

| 环境 | 用途 | 数据库 |
|------|------|--------|
| dev | 开发调试 | 本地 MySQL |
| test | 测试 | 测试服务器 |
| prod | 生产 | 生产数据库 |

### 配置优先级

1. 命令行参数
2. 环境变量
3. 配置文件 (`application-{env}.yml`)
4. 默认值

## 前后端协作

### 接口约定

1. 后端先定义接口契约（API 文档）
2. 前端基于契约开发
3. 接口变更需双方确认

### 联调流程

1. 后端启动开发服务器
2. 前端配置代理到后端地址
3. 进行接口联调
4. 修复问题并更新文档

## 部署流程

### 开发环境

- 前端：`npm run dev`
- 后端：`mvn spring-boot:run`

### 生产环境

1. 构建前端
   ```bash
   cd frontend
   npm run build
   ```

2. 构建后端
   ```bash
   cd backend
   mvn clean package -DskipTests
   ```

3. 部署 JAR/Docker

## 监控与日志

### 日志级别

| 级别 | 使用场景 |
|------|----------|
| DEBUG | 开发调试 |
| INFO | 正常业务日志 |
| WARN | 警告信息 |
| ERROR | 错误信息 |

### 日志格式

```
时间 | 级别 | 请求ID | 类名 | 消息
2024-01-01 12:00:00 | INFO | abc123 | UserService | 用户登录成功
```

## 应急处理

### 问题报告

1. 记录问题现象和时间
2. 收集相关日志
3. 定位问题原因
4. 制定修复方案

### 紧急修复

1. 创建 `hotfix/*` 分支
2. 修复并测试
3. 合并到 `main` 和 `develop`
4. 发布修复版本
