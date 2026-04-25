# 阶段一：依赖缓存层
FROM gradle:8.7-jdk17-alpine AS deps
WORKDIR /app

# 复制 Gradle 相关文件
COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle/libs.versions.toml gradle/

# 下载依赖（利用 Docker 缓存层）
RUN gradle dependencies --no-daemon

# 阶段二：编译
FROM gradle:8.7-jdk17-alpine AS builder
WORKDIR /app

# 复用依赖缓存
COPY --from=deps /home/gradle/.gradle /home/gradle/.gradle

# 复制源代码
COPY . .

# 构建可执行 JAR
RUN gradle bootJar --no-daemon

# 阶段三：运行
FROM eclipse-temurin:17-jre-alpine

# 创建非 root 用户
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# 复制构建产物
COPY --from=builder /app/build/libs/*.jar app.jar

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 切换非 root 用户
USER appuser

EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=5s --retries=3 --start-period=60s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM 参数优化
ENTRYPOINT ["sh", "-c", "java \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:G1HeapRegionSize=16m \
  -XX:InitiatingHeapOccupancyPercent=45 \
  -XX:+UseStringDeduplication \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -Djava.security.egd=file:/dev/./urandom \
  -jar app.jar \
  --spring.profiles.active=${SPRING_PROFILES_ACTIVE:prod}"]
