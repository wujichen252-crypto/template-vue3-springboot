# 部署手册

## 服务器要求

- CPU: 2 核+
- 内存: 4GB+
- 硬盘: 50GB+
- 操作系统: Ubuntu 20.04 / CentOS 7+

## 后端部署

### 方式一：直接运行 JAR

1. 上传 JAR 文件到服务器

```bash
scp target/template-0.0.1-SNAPSHOT.jar user@server:/path/to/app/
```

2. 创建配置文件

```bash
mkdir -p /path/to/app/config
```

3. 编辑 `application-prod.yml` 或创建外部配置文件

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/template_db
    username: root
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379

jwt:
  secret: your-production-secret-key-must-be-long-enough
  access-expire: 7200000
  refresh-expire: 604800000
```

4. 启动应用

```bash
cd /path/to/app
java -jar template-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

5. 配置 Systemd 服务

```ini
[Unit]
Description=Template API
After=network.target mysql.service redis.service

[Service]
Type=simple
User=www-data
WorkingDirectory=/path/to/app
ExecStart=/usr/bin/java -jar template-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
Restart=always

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl enable template-api
sudo systemctl start template-api
```

### 方式二：Docker 部署

1. 构建镜像

```bash
cd backend
docker build -t template-api:latest .
```

2. 运行容器

```bash
docker run -d \
  --name template-api \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/template_db \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=your_password \
  -e JWT_SECRET=your-secret-key \
  template-api:latest
```

### 方式三：宝塔面板部署

1. 安装 宝塔 Java 项目管理器
2. 点击"添加项目"
3. 选择上传的 JAR 文件
4. 配置端口：8080
5. 配置环境变量或启动参数
6. 设置开机启动

## 前端部署

### 静态部署

1. 构建生产版本

```bash
cd frontend
npm install
npm run build
```

2. 构建产物在 `dist/` 目录

3. 配置 Nginx

```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /path/to/frontend/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 数据库初始化

1. 创建数据库

```sql
CREATE DATABASE template_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本

```bash
mysql -u root -p template_db < backend/src/main/resources/db/migration/V1__init.sql
```

## 环境变量

### 后端环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| SPRING_PROFILES_ACTIVE | 环境 | dev |
| SPRING_DATASOURCE_URL | 数据库URL | - |
| SPRING_DATASOURCE_USERNAME | 数据库用户名 | root |
| SPRING_DATASOURCE_PASSWORD | 数据库密码 | - |
| SPRING_DATA_REDIS_HOST | Redis主机 | localhost |
| SPRING_DATA_REDIS_PORT | Redis端口 | 6379 |
| JWT_SECRET | JWT密钥 | - |

### 前端环境变量

| 变量名 | 说明 |
|--------|------|
| VITE_API_BASE_URL | API基础路径 |

## 验证部署

```bash
# 检查后端健康状态
curl http://localhost:8080/api/v1/auth/login -X POST -H "Content-Type: application/json" -d '{"username":"test","password":"test"}'

# 检查前端页面
curl http://localhost:80
```
