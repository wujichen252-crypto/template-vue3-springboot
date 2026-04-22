# Template Vue3 SpringBoot

前后端分离模板项目，前端 Vue 3 + TypeScript + Vite，后端 Spring Boot 3.2 + MyBatis-Plus。

## 项目结构

```
template-vue3-springboot/
├── frontend/          # Vue3 前端项目
├── backend/          # Spring Boot 后端项目
├── docs/             # 项目文档
└── .trae/           # 规则配置
```

## 快速开始

### 前置要求

- Node.js 18+
- Maven 3.9+
- Java 17+
- MySQL 8+
- Redis 7+

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

访问 http://localhost:3000

### 后端启动

```bash
cd backend

# 配置数据库连接
# 编辑 src/main/resources/application-dev.yml

# 执行数据库初始化 SQL
mysql -u root -p < src/main/resources/db/migration/V1__init.sql

# 编译并启动
mvn clean install
mvn spring-boot:run
```

后端 API 将在 http://localhost:8080 启动。

## 技术栈

### 前端
- Vue 3.4 + TypeScript
- Vite 5
- Pinia 状态管理
- Vue Router 4
- Element Plus UI
- Tailwind CSS

### 后端
- Spring Boot 3.2
- Spring Security 6
- MyBatis-Plus 3.5
- MySQL 8
- Redis 7
- JWT (jjwt 0.12)

## API 接口

基础路径: `/api/v1`

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| POST | /auth/register | 用户注册 | 否 |
| POST | /auth/login | 用户登录 | 否 |
| POST | /auth/refresh | 刷新 Token | 否 |
| POST | /auth/logout | 退出登录 | 是 |
| GET | /users/me | 获取当前用户 | 是 |

## 统一响应格式

```json
{
  "code": 200,
  "data": {},
  "msg": "ok",
  "requestId": "uuid"
}
```

## 开发指南

### 前端开发

```bash
# 代码格式化
npm run format

# 代码检查
npm run lint

# 生产构建
npm run build
```

### 后端开发

```bash
# 开发启动
mvn spring-boot:run

# 生产构建
mvn clean package -DskipTests
java -jar target/template-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## 部署

### Docker 部署后端

```bash
cd backend
docker build -t template-api .
docker run -d -p 8080:8080 --name template-api template-api
```

### 宝塔部署

1. 安装 宝塔 Java 项目管理器
2. 上传 `target/template-0.0.1-SNAPSHOT.jar`
3. 配置端口 8080
4. 设置启动参数 `--spring.profiles.active=prod`

## License

MIT
