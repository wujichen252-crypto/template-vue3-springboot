rootProject.name = "template-api"

// 启用版本目录（Version Catalog）
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url = uri("https://maven.aliyun.com/repository/public") } // 国内加速
        maven { url = uri("https://maven.aliyun.com/repository/spring") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
    }
}
