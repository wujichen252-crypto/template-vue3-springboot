plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.ktlint)
    jacoco
}

group = "com.template"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    maven { url = uri("https://maven.aliyun.com/repository/spring") }
}

dependencies {
    // Spring Boot 基础依赖（使用 bundles 批量引入）
    implementation(libs.bundles.spring.basic)

    // MyBatis Plus
    implementation(libs.mybatis.plus.boot)
    implementation(libs.mybatis.plus.jsqlparser)

    // 数据库相关
    runtimeOnly(libs.mysql.connector)
    implementation(libs.flyway.core)
    implementation(libs.flyway.mysql)

    // JWT
    implementation(libs.bundles.jjwt)

    // Kotlin 支持
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.jackson.kotlin)

    // Lombok（编译期使用）
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // 开发工具
    developmentOnly(libs.spring.devtools)

    // 测试依赖
    testImplementation(libs.spring.test)
    testImplementation(libs.assertj)
    testImplementation(libs.security.test)
    testImplementation(libs.h2)
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

// Kotlin 编译选项
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

// Spring Boot 打包配置
springBoot {
    buildInfo()
}

// 任务配置
tasks.jar {
    enabled = false
}

tasks.bootJar {
    enabled = true
    archiveFileName.set("template-api.jar")
}

// ==========================================
// JaCoCo 测试覆盖率配置
// ==========================================
jacoco {
    toolVersion = "0.8.11"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// ==========================================
// ktlint 配置
// ==========================================
ktlint {
    version.set("1.2.1")
    verbose.set(true)
    outputToConsole.set(true)
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
}

// ==========================================
// 自定义任务
// ==========================================

// 打印项目信息
tasks.register("projectInfo") {
    group = "help"
    description = "打印项目构建信息"
    doFirst {
        println("========================================")
        println("Project: $group:$name:$version")
        println("Java Version: ${java.sourceCompatibility}")
        println("Gradle Version: ${gradle.gradleVersion}")
        println("========================================")
    }
}

// 快速开发任务 - 跳过测试和检查
tasks.register("quickBuild") {
    group = "build"
    description = "快速构建（跳过测试和代码检查）"
    dependsOn("clean", "bootJar")
    tasks.findByName("bootJar")?.apply {
        enabled = true
    }
}
