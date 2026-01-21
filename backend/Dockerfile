# 后端构建阶段
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 复制 pom.xml
COPY pom.xml .
COPY admin-module ./admin-module
COPY portal-module ./portal-module
COPY scheduler ./scheduler
COPY shared ./shared

# 构建项目
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# 从构建阶段复制 JAR 文件
COPY --from=builder /build/admin-module/target/admin-module-1.0.0.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
