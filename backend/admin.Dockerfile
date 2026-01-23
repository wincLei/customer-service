# Admin 模块运行镜像 (使用本地预编译的 JAR)
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# 从本地复制预编译的 JAR 文件
COPY admin-module/target/admin-module-1.0.0.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
