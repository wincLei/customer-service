# 项目更名总结

## 📝 更名信息

- **旧名称**: customer-service（全渠道在线客服系统）
- **新名称**: Mini-Customer-Service（极简客服系统）
- **更新日期**: 2026年1月22日

## ✅ 已更新的文件

### 📄 文档文件

- [x] README.md - 项目标题、描述、目录结构
- [x] PRD-3.md - PRD 标题和产品愿景
- [x] QUICK_START.md - 快速开始指南
- [x] DEV_SETUP.md - 开发环境设置
- [x] GENERATION_REPORT.md - 生成报告
- [x] sql/init.sql - 数据库初始化脚本注释

### 🎨 前端文件

- [x] frontend/package.json - 项目名称和描述
- [x] frontend/index.html - 页面标题
- [x] frontend/src/views/Login.vue - 登录页面标题（极简客服系统）
- [x] frontend/src/layouts/AdminLayout.vue - 管理后台标题
- [x] frontend/src/views/admin/Settings.vue - 默认项目名称和欢迎消息

### ⚙️ 后端配置文件

- [x] backend/pom.xml - 根 POM groupId、artifactId、name、description
- [x] backend/admin-module/pom.xml - groupId、parent、依赖
- [x] backend/portal-module/pom.xml - groupId、parent、依赖
- [x] backend/shared/pom.xml - groupId、parent
- [x] backend/scheduler/pom.xml - groupId、parent、依赖
- [x] backend/admin-module/src/main/resources/application.yml - 应用名称、JWT secret、WuKongIM app-id、RocketMQ groups
- [x] backend/portal-module/src/main/resources/application.yml - 应用名称、JWT secret
- [x] backend/portal-module/.../PublicConfigController.java - 欢迎消息

### 🐳 Docker 配置

- [x] docker-compose.yml - 所有容器名称和网络名称
  - mini-customer-service-postgres
  - mini-customer-service-redis
  - mini-customer-service-backend
  - mini-customer-service-portal-api
  - mini-customer-service-frontend
  - mini-customer-service-net（网络名称）

## 🔄 更新的标识符

### GroupId 和 ArtifactId

```xml
<!-- 旧 -->
<groupId>com.customer-service</groupId>
<artifactId>customer-service</artifactId>

<!-- 新 -->
<groupId>com.mini-customer-service</groupId>
<artifactId>mini-customer-service</artifactId>
```

### 应用名称

```yaml
# 旧
spring.application.name: customer-service-admin
spring.application.name: customer-service-portal

# 新
spring.application.name: mini-customer-service-admin
spring.application.name: mini-customer-service-portal
```

### Docker 容器和网络

```yaml
# 旧
container_name: customer-service-*
networks: customer-service-net

# 新
container_name: mini-customer-service-*
networks: mini-customer-service-net
```

## 💡 重要提示

1. **包名未更改**: Java 包名仍为 `com.customer_service.*`（下划线），未改为 `com.mini_customer_service`，以避免大规模重构
2. **数据库名称未更改**: 数据库名仍为 `customer_service`，保持向后兼容
3. **WuKongIM App ID**: 已更新为 `mini-customer-service`
4. **JWT Secret**: 已更新为带 `mini-` 前缀的新值
5. **RocketMQ Groups**: 已更新为 `mini-customer-service-producer/consumer`

## 🚀 后续步骤

如需重新构建和部署，请执行：

```bash
# 停止旧容器
docker-compose down

# 清理旧 volumes（可选）
docker volume rm customer-service_postgres_data
docker volume rm customer-service_redis_data

# 重新构建和启动
docker-compose up -d --build
```

## ⚠️ 注意事项

- 如果已有运行中的服务，请先备份数据
- 更改后首次启动可能需要重新初始化数据库
- 建议在测试环境验证后再部署到生产环境
