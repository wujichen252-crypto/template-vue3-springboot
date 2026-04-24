# 可配置模板仓库 CI/CD 设计指南（Spring Boot 后端适配版）

> **文档编号**：TECH-DEV-005  
> **归档日期**：2026-04-23  
> **关联项目**：NeuraMind 智脑笔记系统（Vue3 + Spring Boot 全栈模板）  
> **文档类型**：工程化实践与模板设计规范

---

## 目录

1. [与通用模板版本的核心差异](#一与通用模板版本的核心差异)
2. [设计目标与核心原则](#二设计目标与核心原则)
3. [模板仓库目录结构](#三模板仓库目录结构)
4. [可配置 CI 架构设计](#四可配置-ci-架构设计)
5. [配置文件详解](#五配置文件详解)
6. [Spring Boot 后端部署清单](#六spring-boot-后端部署清单)
7. [初始化与配置流程](#七初始化与配置流程)
8. [多环境支持方案](#八多环境支持方案)
9. [安全与权限设计](#九安全与权限设计)
10. [使用手册（给模板使用者）](#十使用手册给模板使用者)
11. [完整文件参考](#十一完整文件参考)

---

## 一、与通用模板版本的核心差异

| 维度 | Django-Ninja (Python) | Spring Boot (Java) |
|------|----------------------|-------------------|
| **构建产物** | 源码 tarball（解释型） | 可执行 JAR 文件（`mvn package`） |
| **构建工具** | pip + requirements.txt | Maven (`pom.xml`) 或 Gradle (`build.gradle`) |
| **运行环境** | 服务器需 Python + venv | 服务器需 JDK（建议 17/21），JAR 内嵌 Tomcat |
| **部署方式** | CI 上传源码，服务器端装依赖 | CI 编译 JAR 后上传，直接 `java -jar` 运行 |
| **数据库迁移** | `migrate` 命令 | Flyway / Liquibase 自动迁移，或手动执行 |
| **配置管理** | `settings.py` + 环境变量 | `application.yml` + Spring Profiles |
| **健康检查** | 自定义接口或 Django Admin | Spring Boot Actuator `/actuator/health` |
| **进程管理** | Gunicorn + systemd | 直接 `java -jar` + systemd |

**结论**：Spring Boot 的 CI 部署与 Go 类似，都是**"CI 编译出二进制产物后上传"**，但产物是 JAR 而非原生二进制，且构建依赖 Maven/Gradle 生态。

---

## 二、设计目标与核心原则

与通用模板一致：

1. **零硬编码**：YAML 中不出现 IP、路径、域名、Java 版本等具体值
2. **构建产物自治**：CI 负责编译出 JAR，服务器只负责运行
3. **构建工具兼容**：同时支持 Maven 和 Gradle，通过变量切换
4. **防御性编程**：未配置完成时，CI 优雅跳过而非报错
5. **多环境就绪**：通过 `spring.profiles.active` 区分环境
6. **宝塔兼容**：提供 systemd 和宝塔 Java 项目管理器两套方案

---

## 三、模板仓库目录结构

```
neuramind-template/                 ← 模板仓库根目录
├── .github/
│   └── workflows/
│       ├── ci-check.yml            ← 代码质量检查（无部署，开箱即用）
│       └── deploy.yml              ← 可配置部署流水线（未配置时自动跳过）
├── scripts/
│   ├── setup-ci.sh               ← 本地初始化脚本
│   └── neuramind.service         ← systemd 服务模板
├── frontend/                       ← Vue3 前端（目录名可配置）
├── backend/                        ← Spring Boot 后端（目录名可配置）
│   ├── pom.xml                     ← Maven 构建文件
│   └── src/
│       └── main/
│           ├── java/
│           └── resources/
│               └── application.yml
├── .env.example                    ← 环境变量模板
├── README.md
└── LICENSE
```

---

## 四、可配置 CI 架构设计

### 4.1 配置分层模型

```
┌─────────────────────────────────────────────┐
│  层 1：代码层（YAML 文件）                     │
│  └── 只定义流程逻辑，不出现任何具体值             │
│       │                                       │
│  层 2：仓库变量层（Settings → Variables）      │
│  └── 非敏感配置：IP、路径、Java版本、构建工具     │
│       │                                       │
│  层 3：仓库密钥层（Settings → Secrets）        │
│  └── 敏感信息：SSH私钥、数据库密码、JWT密钥      │
└─────────────────────────────────────────────┘
```

### 4.2 Spring Boot 特有命名规范

| 类型 | 前缀 | 示例 | 存放位置 |
|------|------|------|----------|
| 部署服务器相关 | `DEPLOY_` | `DEPLOY_SERVER_HOST` | Variables |
| 项目路径相关 | `PROJECT_` | `PROJECT_BACKEND_DIR` | Variables |
| Java/Spring 配置 | `JAVA_` / `SPRING_` | `JAVA_VERSION` | Variables |
| 构建工具配置 | `BUILD_` | `BUILD_TOOL` | Variables |
| 运行时开关 | `ENABLE_` | `ENABLE_FLYWAY` | Variables |
| 敏感凭据 | `SSH_` / `DB_` | `SSH_PRIVATE_KEY` | Secrets |

---

## 五、配置文件详解

### 5.1 代码质量检查（ci-check.yml）

**特点**：无需任何配置，从模板创建后立即生效。

```yaml
name: CI Check

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

env:
  FRONTEND_DIR: ${{ vars.PROJECT_FRONTEND_DIR || './frontend' }}
  BACKEND_DIR: ${{ vars.PROJECT_BACKEND_DIR || './backend' }}
  JAVA_VER: ${{ vars.JAVA_VERSION || '21' }}
  NODE_VER: ${{ vars.NODE_VERSION || '20' }}
  BUILD_TOOL: ${{ vars.BUILD_TOOL || 'maven' }}   # maven 或 gradle

jobs:
  frontend-check:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: ${{ env.FRONTEND_DIR }}
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: ${{ env.NODE_VER }}
          cache: 'npm'
          cache-dependency-path: '${{ env.FRONTEND_DIR }}/package-lock.json'
      - run: npm ci
      - run: npm run lint
      - run: npm run build

  backend-check:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: ${{ env.BACKEND_DIR }}
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VER }}
          distribution: 'temurin'   # Eclipse Temurin，开源免费
          cache: ${{ env.BUILD_TOOL }}
      
      # Maven 构建 + 测试
      - name: Build with Maven
        if: env.BUILD_TOOL == 'maven'
        run: mvn clean verify -B
      
      # Gradle 构建 + 测试
      - name: Build with Gradle
        if: env.BUILD_TOOL == 'gradle'
        run: ./gradlew clean build
      
      # 产物路径确认（用于调试）
      - name: List artifacts
        run: |
          if [ "${{ env.BUILD_TOOL }}" == "maven" ]; then
            ls -la target/*.jar || true
          else
            ls -la build/libs/*.jar || true
          fi
```

**设计要点**：
- `actions/setup-java@v4` 自动处理 JDK 和 Maven/Gradle 缓存
- `cache: 'maven'` 或 `cache: 'gradle'` 自动缓存依赖，大幅加速
- `mvn clean verify` 会执行编译、测试、打包全链路
- 同时支持 Maven 和 Gradle，通过 `BUILD_TOOL` 变量切换

---

### 5.2 可配置部署流水线（deploy.yml）

```yaml
name: Deploy

on:
  push:
    branches: [main]
  workflow_dispatch:
    inputs:
      environment:
        description: '部署环境'
        required: true
        default: 'production'
        type: choice
        options:
          - production
          - staging

env:
  FRONTEND_DIR: ${{ vars.PROJECT_FRONTEND_DIR || './frontend' }}
  BACKEND_DIR: ${{ vars.PROJECT_BACKEND_DIR || './backend' }}
  NODE_VER: ${{ vars.NODE_VERSION || '20' }}
  JAVA_VER: ${{ vars.JAVA_VERSION || '21' }}
  BUILD_TOOL: ${{ vars.BUILD_TOOL || 'maven' }}

jobs:
  # ==========================================
  # Job 1: 构建前端
  # ==========================================
  build-frontend:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: ${{ env.FRONTEND_DIR }}
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: ${{ env.NODE_VER }}
          cache: 'npm'
          cache-dependency-path: '${{ env.FRONTEND_DIR }}/package-lock.json'
      - run: npm ci
      - run: npm run build
      - uses: actions/upload-artifact@v4
        with:
          name: frontend-dist
          path: ${{ env.FRONTEND_DIR }}/dist
          retention-days: 3

  # ==========================================
  # Job 2: 构建后端 JAR
  # ==========================================
  build-backend:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: ${{ env.BACKEND_DIR }}
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VER }}
          distribution: 'temurin'
          cache: ${{ env.BUILD_TOOL }}
      
      - name: Build JAR (Maven)
        if: env.BUILD_TOOL == 'maven'
        run: mvn clean package -DskipTests=false -B
      
      - name: Build JAR (Gradle)
        if: env.BUILD_TOOL == 'gradle'
        run: ./gradlew clean build
      
      # 统一产物名称，方便后续步骤引用
      - name: Rename artifact
        run: |
          mkdir -p ../jars
          if [ "${{ env.BUILD_TOOL }}" == "maven" ]; then
            cp target/*.jar ../jars/app.jar
          else
            cp build/libs/*-SNAPSHOT.jar ../jars/app.jar 2>/dev/null || \
            cp build/libs/*.jar ../jars/app.jar
          fi
          ls -la ../jars/
      
      - uses: actions/upload-artifact@v4
        with:
          name: backend-jar
          path: ./jars/app.jar
          retention-days: 3

  # ==========================================
  # Job 3: 部署（带前置条件检查）
  # ==========================================
  deploy:
    needs: [build-frontend, build-backend]
    runs-on: ubuntu-latest
    
    # 防御性条件：关键变量未配置时直接跳过
    if: |
      vars.DEPLOY_SERVER_HOST != '' &&
      vars.DEPLOY_FRONTEND_PATH != '' &&
      vars.DEPLOY_BACKEND_PATH != '' &&
      secrets.SSH_PRIVATE_KEY != ''
    
    steps:
      # 下载前端产物
      - name: Download frontend artifact
        uses: actions/download-artifact@v4
        with:
          name: frontend-dist
          path: ./dist
      
      # 下载后端 JAR
      - name: Download backend artifact
        uses: actions/download-artifact@v4
        with:
          name: backend-jar
          path: ./jars

      # 部署前端静态文件
      - name: Deploy Frontend
        uses: appleboy/scp-action@v0.1.7
        with:
          host: ${{ vars.DEPLOY_SERVER_HOST }}
          username: ${{ vars.DEPLOY_SERVER_USER || 'root' }}
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          port: ${{ vars.DEPLOY_SERVER_PORT || '22' }}
          source: "dist/*"
          target: ${{ vars.DEPLOY_FRONTEND_PATH }}
          strip_components: 1

      # 部署后端 JAR
      - name: Deploy Backend JAR
        uses: appleboy/scp-action@v0.1.7
        with:
          host: ${{ vars.DEPLOY_SERVER_HOST }}
          username: ${{ vars.DEPLOY_SERVER_USER || 'root' }}
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          port: ${{ vars.DEPLOY_SERVER_PORT || '22' }}
          source: "jars/app.jar"
          target: ${{ vars.DEPLOY_BACKEND_PATH }}/

      # 服务器端替换、迁移、重启
      - name: Execute Backend Deployment
        uses: appleboy/ssh-action@v1.0.3
        with:
          host: ${{ vars.DEPLOY_SERVER_HOST }}
          username: ${{ vars.DEPLOY_SERVER_USER || 'root' }}
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          port: ${{ vars.DEPLOY_SERVER_PORT || '22' }}
          script: |
            set -e
            
            BACKEND_PATH="${{ vars.DEPLOY_BACKEND_PATH }}"
            JAR_NAME="${{ vars.DEPLOY_JAR_NAME || 'app.jar' }}"
            SERVICE_NAME="${{ vars.DEPLOY_SERVICE_NAME || 'neuramind' }}"
            APP_PORT="${{ vars.APP_PORT || '8080' }}"
            JAVA_OPTS="${{ vars.JAVA_OPTS || '-Xms256m -Xmx512m' }}"
            SPRING_PROFILE="${{ vars.SPRING_PROFILE || 'production' }}"
            ENABLE_FLYWAY="${{ vars.ENABLE_FLYWAY || 'true' }}"
            RESTART_CMD="${{ vars.DEPLOY_RESTART_COMMAND || 'sudo systemctl restart neuramind' }}"
            
            echo "===== 开始部署 Spring Boot 后端 ====="
            
            cd "$BACKEND_PATH"
            
            # 备份旧 JAR
            if [ -f "$JAR_NAME" ]; then
              BACKUP_NAME="${JAR_NAME}.bak.$(date +%Y%m%d_%H%M%S)"
              cp "$JAR_NAME" "$BACKUP_NAME"
              echo "已备份旧版本: $BACKUP_NAME"
            fi
            
            # 替换新 JAR（SCP 上传的是 app.jar，可能需重命名）
            if [ "$JAR_NAME" != "app.jar" ]; then
              mv app.jar "$JAR_NAME"
            fi
            
            # 如果启用了 Flyway，且配置了数据库，可执行迁移
            # 注意：生产环境通常由应用启动时自动执行，这里仅做备用
            if [ "$ENABLE_FLYWAY" == "true" ] && command -v java &> /dev/null; then
              echo "Flyway 迁移由 Spring Boot 启动时自动执行"
            fi
            
            # 重启服务
            echo "重启服务: $SERVICE_NAME"
            eval "$RESTART_CMD"
            
            # 健康检查（Spring Boot Actuator）
            sleep 8
            HEALTH_URL="http://127.0.0.1:${APP_PORT}/actuator/health"
            echo "健康检查: $HEALTH_URL"
            
            if curl -sf "$HEALTH_URL" > /dev/null 2>&1; then
              echo "健康检查通过 ✅"
            else
              echo "健康检查未通过，等待 5 秒重试..."
              sleep 5
              if curl -sf "$HEALTH_URL" > /dev/null 2>&1; then
                echo "健康检查通过 ✅"
              else
                echo "健康检查失败 ❌，请手动检查日志: journalctl -u $SERVICE_NAME -n 50"
                # 可选：自动回滚
                # if [ -f "$BACKUP_NAME" ]; then
                #   cp "$BACKUP_NAME" "$JAR_NAME"
                #   eval "$RESTART_CMD"
                # fi
                exit 1
              fi
            fi
            
            echo "===== 后端部署完成 ====="

      # 可选：部署通知
      - name: Notify
        if: vars.DEPLOY_NOTIFY_WEBHOOK != ''
        run: |
          curl -X POST ${{ vars.DEPLOY_NOTIFY_WEBHOOK }} \
            -H 'Content-Type: application/json' \
            -d '{"msg":"[Spring Boot] 部署成功: ${{ github.repository }}@${{ github.sha }}"}' || true
```

---

## 六、Spring Boot 后端部署清单

从模板创建新仓库后，在 **Settings → Secrets and variables → Actions** 中配置：

### Secrets（加密）

| 名称 | 必填 | 获取方式 |
|------|------|----------|
| `SSH_PRIVATE_KEY` | ✅ | 服务器执行 `cat ~/.ssh/id_rsa`，粘贴全文 |

### Variables（明文）

| 名称 | 必填 | 示例 | 说明 |
|------|------|------|------|
| `DEPLOY_SERVER_HOST` | ✅ | `123.456.78.90` | 服务器公网 IP |
| `DEPLOY_SERVER_USER` | ❌ | `root` | SSH 用户名，默认 root |
| `DEPLOY_SERVER_PORT` | ❌ | `22` | SSH 端口，默认 22 |
| `DEPLOY_FRONTEND_PATH` | ✅ | `/www/wwwroot/neuramind` | 前端部署目录 |
| `DEPLOY_BACKEND_PATH` | ✅ | `/www/neuramind` | 后端 JAR 存放目录 |
| `DEPLOY_JAR_NAME` | ❌ | `neuramind.jar` | 服务器端 JAR 文件名，默认 `app.jar` |
| `DEPLOY_SERVICE_NAME` | ❌ | `neuramind` | systemd 服务名 |
| `DEPLOY_RESTART_COMMAND` | ❌ | `sudo systemctl restart neuramind` | 重启命令 |
| `PROJECT_FRONTEND_DIR` | ❌ | `./frontend` | 前端代码相对路径 |
| `PROJECT_BACKEND_DIR` | ❌ | `./backend` | 后端代码相对路径 |
| `BUILD_TOOL` | ❌ | `maven` | `maven` 或 `gradle` |
| `JAVA_VERSION` | ❌ | `21` | CI 构建用 JDK 版本（建议 17 或 21） |
| `NODE_VERSION` | ❌ | `20` | CI 构建用 Node.js 版本 |
| `APP_PORT` | ❌ | `8080` | Spring Boot 服务端口（用于健康检查） |
| `SPRING_PROFILE` | ❌ | `production` | 激活的 Spring Profile |
| `JAVA_OPTS` | ❌ | `-Xms256m -Xmx512m` | JVM 启动参数 |
| `ENABLE_FLYWAY` | ❌ | `true` | 是否由应用自动执行数据库迁移 |
| `DEPLOY_NOTIFY_WEBHOOK` | ❌ | `https://oapi.dingtalk.com/...` | 可选，部署成功通知地址 |

---

## 七、初始化与配置流程

### 7.1 服务器端准备

```bash
# 1. 确保服务器有 JDK 17+（宝塔软件商店可安装，或手动）
java -version

# 2. 创建项目目录
mkdir -p /www/neuramind
cd /www/neuramind

# 3. 生成 SSH 密钥对（用于 GitHub Actions 连接）
ssh-keygen -t rsa -b 4096 -C "github-actions" -f ~/.ssh/github_actions
cat ~/.ssh/github_actions.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys

# 4. 把私钥填到 GitHub Secrets：SSH_PRIVATE_KEY
cat ~/.ssh/github_actions
```

### 7.2 配置 systemd 服务

在服务器创建 `/etc/systemd/system/neuramind.service`：

```ini
[Unit]
Description=NeuraMind Spring Boot API
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/www/neuramind
# 注意：ExecStart 中的 jar 名要与 DEPLOY_JAR_NAME 一致
ExecStart=/usr/bin/java -Xms256m -Xmx512m -jar /www/neuramind/neuramind.jar
Restart=always
RestartSec=5
# Spring Profile 和数据库等敏感配置通过环境变量注入
Environment="SPRING_PROFILES_ACTIVE=production"
Environment="SPRING_DATASOURCE_URL=jdbc:mysql://127.0.0.1:3306/neuramind"
Environment="SPRING_DATASOURCE_USERNAME=root"
Environment="SPRING_DATASOURCE_PASSWORD=你的数据库密码"
Environment="JWT_SECRET=你的JWT密钥"

[Install]
WantedBy=multi-user.target
```

启用服务：

```bash
sudo systemctl daemon-reload
sudo systemctl enable neuramind
sudo systemctl start neuramind
```

### 7.3 宝塔面板 Nginx 配置

宝塔 → 网站 → 你的站点 → 设置 → 配置文件：

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 前端静态文件
    location / {
        root /www/wwwroot/neuramind;
        try_files $uri $uri/ /index.html;
    }
    
    # Spring Boot API 反向代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # Spring Boot Actuator 健康检查（建议限制 IP）
    location /actuator/ {
        allow 127.0.0.1;
        deny all;
        proxy_pass http://127.0.0.1:8080/actuator/;
    }
}
```

### 7.4 Spring Boot 项目必要配置

确保 `backend/src/main/resources/application.yml` 包含：

```yaml
server:
  port: 8080

spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:sqlite:neuramind.db}
    username: ${SPRING_DATASOURCE_USERNAME:}
    password: ${SPRING_DATASOURCE_PASSWORD:}

# 启用 Actuator 健康检查（用于 CI 部署验证）
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: when_authorized
```

---

## 八、多环境支持方案

通过 **Spring Profiles** 实现：

```yaml
# application-development.yml
spring:
  datasource:
    url: jdbc:sqlite:db.sqlite3
  jpa:
    hibernate:
      ddl-auto: create-drop

# application-production.yml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
```

CI 中通过变量注入：

```bash
Environment="SPRING_PROFILES_ACTIVE=${{ vars.SPRING_PROFILE || 'production' }}"
```

---

## 九、安全与权限设计

### Spring Boot 特有安全项

| 风险 | 防护措施 |
|------|----------|
| **JAR 内源码泄露** | 使用 `spring-boot-maven-plugin`，确保依赖打包在内部，不暴露源码 |
| **Actuator 未授权访问** | Nginx 限制 `/actuator` 只允许 127.0.0.1；或配置 Spring Security |
| **数据库密码硬编码** | `application.yml` 使用 `${}` 占位符，实际值由服务器环境变量注入 |
| **JWT 密钥泄露** | 生产密钥只存服务器环境变量和 GitHub Secrets，绝不提交仓库 |
| **依赖漏洞** | 定期执行 `mvn org.owasp:dependency-check-maven:check` |

### 模板仓库安全

- **`application.yml` 中所有敏感值使用环境变量占位符**
- **提供 `.env.example` 但留空或标注 `change-me`**
- **README 中明确警告修改默认密钥**

---

## 十、使用手册（给模板使用者）

将此段放入模板仓库的 **README.md**：

```markdown
## 🚀 从模板创建后必做（Spring Boot 后端版）

### 1. 配置仓库变量
进入仓库 **Settings → Secrets and variables → Actions → Variables**，添加：

| 变量名 | 示例值 |
|--------|--------|
| `DEPLOY_SERVER_HOST` | `123.456.78.90` |
| `DEPLOY_FRONTEND_PATH` | `/www/wwwroot/neuramind` |
| `DEPLOY_BACKEND_PATH` | `/www/neuramind` |
| `DEPLOY_JAR_NAME` | `neuramind.jar` |
| `BUILD_TOOL` | `maven` |
| `JAVA_VERSION` | `21` |

### 2. 配置密钥
进入 **Secrets**，添加：

- `SSH_PRIVATE_KEY`：你的服务器私钥（`cat ~/.ssh/id_rsa`）

### 3. 服务器准备
确保服务器已：
1. 安装 JDK 17+（`java -version`）
2. 创建目录 `/www/neuramind`
3. 添加 SSH 公钥授权
4. 创建 systemd 服务（见项目 `scripts/neuramind.service` 模板）
5. 配置 Nginx 反向代理到 `127.0.0.1:8080`

### 4. 配置 Spring Boot 环境变量
编辑服务器 `/etc/systemd/system/neuramind.service`，填入：
- 数据库连接地址、用户名、密码
- JWT 密钥
- 其他业务所需 Secrets

然后执行：
```bash
sudo systemctl daemon-reload
sudo systemctl restart neuramind
```

### 5. 首次部署
```bash
git push origin main
```
然后去 **Actions** 标签页查看部署状态。观察后端日志：
```bash
sudo journalctl -u neuramind -f
```

### 6. 健康检查
部署成功后访问：
```bash
curl http://127.0.0.1:8080/actuator/health
```
应返回 `{"status":"UP"}`。
```

---

## 十一、完整文件参考

### `scripts/setup-ci.sh`

```bash
#!/bin/bash
echo "=========================================="
echo "  NeuraMind (Spring Boot) CI 配置向导"
echo "=========================================="
echo ""

read -p "请输入服务器 IP: " HOST
read -p "请输入前端部署路径 (默认: /www/wwwroot/neuramind): " FRONTEND_PATH
FRONTEND_PATH=${FRONTEND_PATH:-/www/wwwroot/neuramind}
read -p "请输入后端部署路径 (默认: /www/neuramind): " BACKEND_PATH
BACKEND_PATH=${BACKEND_PATH:-/www/neuramind}
read -p "请输入 JAR 文件名 (默认: app.jar): " JAR_NAME
JAR_NAME=${JAR_NAME:-app.jar}

echo ""
echo "请在 GitHub 仓库页面手动配置以下 Secrets 和 Variables："
echo ""
echo "【Secrets】"
echo "  SSH_PRIVATE_KEY"
echo ""
echo "【Variables - 必填】"
echo "  DEPLOY_SERVER_HOST = $HOST"
echo "  DEPLOY_FRONTEND_PATH = $FRONTEND_PATH"
echo "  DEPLOY_BACKEND_PATH = $BACKEND_PATH"
echo "  DEPLOY_JAR_NAME = $JAR_NAME"
echo ""
echo "【Variables - 建议配置】"
echo "  BUILD_TOOL = maven          (或 gradle)"
echo "  JAVA_VERSION = 21"
echo "  SPRING_PROFILE = production"
echo "  DEPLOY_RESTART_COMMAND = sudo systemctl restart neuramind"
echo ""
echo "配置完成后，执行 git push 即可触发首次部署。"
echo "=========================================="
```

### `scripts/neuramind.service`（systemd 模板）

```ini
[Unit]
Description=NeuraMind Spring Boot API
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/www/neuramind
ExecStart=/usr/bin/java -Xms256m -Xmx512m -jar /www/neuramind/neuramind.jar
Restart=always
RestartSec=5
Environment="SPRING_PROFILES_ACTIVE=production"
Environment="SPRING_DATASOURCE_URL=jdbc:mysql://127.0.0.1:3306/neuramind"
Environment="SPRING_DATASOURCE_USERNAME=root"
Environment="SPRING_DATASOURCE_PASSWORD=change-me"
Environment="JWT_SECRET=change-me"

[Install]
WantedBy=multi-user.target
```

### `.env.example`

```bash
# Spring Boot 本地开发环境配置
# 生产环境通过服务器 systemd Environment 注入，不依赖此文件

SPRING_PROFILES_ACTIVE=development
SPRING_DATASOURCE_URL=jdbc:sqlite:db.sqlite3
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=

# JWT
JWT_SECRET=local-dev-key-change-me-in-production

# Server
SERVER_PORT=8080
```

### `pom.xml` 关键片段参考

```xml
<properties>
    <java.version>21</java.version>
    <spring-boot.version>3.2.0</spring-boot.version>
</properties>

<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Boot Actuator（健康检查必备） -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    
    <!-- 数据库驱动（按需选择） -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Flyway 迁移（可选） -->
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <!-- 打包为可执行 fat jar -->
                <executable>true</executable>
            </configuration>
        </plugin>
    </plugins>
</build>
```

---

## 总结

| 你（模板作者）需要做的 | 使用者需要做的 |
|----------------------|---------------|
| 写好 `.github/workflows/*.yml`，全部用 `vars`/`secrets` | 在 GitHub 网页填入服务器 IP、路径、JAR 名 |
| 提供 `scripts/neuramind.service` systemd 模板 | 在服务器安装 JDK、配置 systemd、填入数据库密码 |
| 提供 `.env.example` 和配置清单 | 把 SSH 私钥粘贴到 Secrets，修改生产环境密钥 |
| 确保 `if:` 条件防止未配置时失败 | `git push` 触发部署，观察 `journalctl` 日志 |

> **核心思想**：Spring Boot 的 CI 部署与 Go 类似，都是**"CI 编译出产物后上传"**。产物是 JAR 文件，服务器通过 `java -jar` 运行。关键是利用 Spring Boot Actuator 做健康检查，确保部署成功后服务真正可用。

---

*归档人：AI 助手*  
*关联文档：TECH-DEV-003《可配置模板仓库 CI/CD 设计指南（通用版）》、TECH-DEV-004《Django-Ninja 后端适配版》*  
*技术标签：`模板仓库` `GitHub Actions` `Spring Boot` `Java` `Maven` `Gradle` `Vue3` `自动化部署`*