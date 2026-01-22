# 快速启动指南

## 🚀 最快5分钟启动项目

### 方式1: 使用Docker Compose（推荐）

```bash
# 1. 克隆或进入项目目录
cd /Users/leijiang/Documents/vs-workspace/mini-customer-service

# 2. 启动所有服务
docker-compose up -d

# 3. 等待所有服务就绪（约30-60秒）
docker-compose ps

# 4. 访问应用
# 管理后台: http://localhost/admin
# 用户门户: http://localhost/portal
# H5移动端: http://localhost/mobile/chat
```

### 方式2: 本地开发（需要本地环境）

```bash
# 启动必要的服务
docker-compose up -d postgres redis wukongim rocketmq-namesrv rocketmq-broker

# 后端开发
cd backend
mvn clean install
mvn spring-boot:run -pl admin-module

# 前端开发（新终端）
cd frontend
npm install
npm run dev
```

## 📋 默认登录信息

### 管理后台

- URL: http://localhost/admin
- 用户名: agent001
- 密码: (在init.sql中密码哈希值为 $2a$10$demo_hash_001，需实现真实认证)

### 示例用户

- 项目ID: 1
- 用户UID: user_001 / user_002

## 🔍 验证服务状态

```bash
# 查看所有容器
docker-compose ps

# 检查后端API
curl http://localhost:8080/api/admin/conversations/pending

# 检查WuKongIM
curl http://localhost:5000/health

# 查看日志
docker-compose logs -f backend-api
docker-compose logs -f frontend
```

## 📊 数据库连接

```bash
# PostgreSQL
host: localhost
port: 5432
database: customer_service
user: postgres
password: postgres123

# 执行SQL查询
psql -h localhost -U postgres -d customer_service

# 查看示例数据
SELECT * FROM projects;
SELECT * FROM agents;
SELECT * FROM users;
```

## 🛠️ 常用命令

```bash
# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose down

# 重启特定服务
docker-compose restart backend-api

# 查看实时日志
docker-compose logs -f backend-api

# 进入容器
docker-compose exec backend-api bash

# 重建镜像
docker-compose build --no-cache

# 清理所有数据（谨慎使用！）
docker-compose down -v
```

## 🧪 测试API

### 访客登录

```bash
curl -X POST http://localhost/api/pub/auth/visitor \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": "1",
    "deviceType": "mobile",
    "nickName": "李先生",
    "phone": "13800000001"
  }'
```

### 获取项目配置

```bash
curl http://localhost/api/pub/config?projectId=1
```

### 获取排队中的会话

```bash
curl http://localhost/api/admin/conversations/pending?projectId=1
```

## 📱 H5移动端测试

访问带参数的URL：

```
http://localhost/mobile/chat?token=xxx&project_id=1&uid=user_001&nick_name=李先生&device_type=mobile
```

## 🐛 常见问题

### 1. 无法连接到数据库

```bash
# 检查PostgreSQL是否运行
docker-compose ps | grep postgres

# 重新初始化数据库
docker-compose down postgres
docker volume rm mini-customer-service_postgres_data
docker-compose up -d postgres
```

### 2. 前端无法访问

```bash
# 检查Nginx是否运行
docker-compose ps | grep frontend

# 查看Nginx日志
docker-compose logs frontend
```

### 3. WebSocket连接失败

```bash
# 检查WuKongIM
curl http://localhost:5000/health

# 检查Nginx代理配置
cat nginx/default.conf | grep -A 10 "location /ws/"
```

## 📈 性能优化

### 查看Docker资源使用

```bash
docker stats
```

### 前端构建优化

```bash
# 查看代码分割
npm run build

# 分析bundle大小
npm install -g webpack-bundle-analyzer
```

## 🔐 安全建议

在生产部署前：

1. [ ] 修改所有默认密码
2. [ ] 配置SSL/TLS证书
3. [ ] 启用CORS白名单
4. [ ] 配置防火墙规则
5. [ ] 启用数据库备份
6. [ ] 配置日志审计

## 📚 更多信息

- 详细文档: [README.md](README.md)
- PRD文档: [PRD-3.md](PRD-3.md)
- 生成清单: [GENERATION_CHECKLIST.md](GENERATION_CHECKLIST.md)
- Nginx配置: [nginx/default.conf](nginx/default.conf)
- SQL脚本: [sql/init.sql](sql/init.sql)

---

**提示**: 首次启动时可能需要等待一段时间以让所有服务就绪。建议使用 `docker-compose logs -f` 查看实时日志。
