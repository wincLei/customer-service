# Mini-Customer-Service（极简客服系统）

这是一套完整的极简客服系统，支持Web、H5移动端和管理后台，集成WuKongIM实时通讯。

## 项目架构

```
customer-service/
├── backend/                      # 后端服务
│   ├── admin-module/            # 管理端API (客服工作台、管理后台)
│   ├── portal-module/           # 用户端API (访客门户、H5接口)
│   ├── scheduler/               # 定时任务模块
│   └── shared/                  # 公共模块 (实体、DTO、工具类)
├── frontend/                    # 统一前端工程 (Vue3 + Vite + TypeScript)
│   ├── src/
│   │   ├── layouts/            # 布局组件 (AdminLayout, PortalLayout, MobileLayout)
│   │   ├── views/              # 业务视图
│   │   │   ├── admin/          # 客服工作台
│   │   │   ├── portal/         # 访客门户
│   │   │   └── h5/             # H5移动端
│   │   └── router/             # 路由配置
│   ├── Dockerfile              # 前端容器镜像
│   └── vite.config.ts          # Vite配置 (代码分割)
├── nginx/                      # Nginx配置
│   └── default.conf            # 反向代理配置
├── sql/                        # 数据库初始化脚本
│   └── init.sql               # PostgreSQL 初始化脚本
├── docker-compose.yml          # Docker容器编排
└── README.md                   # 本文件
```

## 技术栈

### 后端

- **框架**: Spring Boot 3.4.1
- **语言**: Java 21+
- **数据库**: PostgreSQL 15
- **缓存**: Redis 7
- **认证**: JWT (io.jsonwebtoken)
- **构建**: Maven
- **消息队列**: RocketMQ 5.1.4

### 前端

- **框架**: Vue 3.4.0
- **构建工具**: Vite 7.0
- **语言**: TypeScript 5.3
- **状态管理**: Pinia 2.1
- **路由**: Vue Router 4.2
- **UI框架**:
  - Element Plus (管理端/PC端)
  - Vant UI (H5端)
  - TailwindCSS
- **图表**: D3.js 7.8
- **图标**: Phosphor Icons

### 中间件

- **即时通讯**: WuKongIM
- **Web服务**: Nginx
- **容器化**: Docker & Docker Compose

## 快速开始

### 前置条件

- Docker & Docker Compose
- Node.js v22.12.0 (本地开发)
- Java 21+ (本地开发)
- Maven 3.9+ (本地开发)

### 使用Docker快速部署

```bash
# 1. 克隆项目
git clone <repository-url>
cd customer-service

# 2. 启动所有服务
docker-compose up -d

# 3. 等待服务启动完成（约30秒）
docker-compose ps

# 4. 访问应用
# 管理后台: http://localhost/admin
# 用户门户: http://localhost/portal
# H5移动端: http://localhost/mobile/chat
```

### 本地开发

#### 后端开发

```bash
# 进入后端目录
cd backend

# 配置数据库连接 (application.yml)
# 启动 PostgreSQL 和 Redis
docker run -d -p 5432:5432 \
  -e POSTGRES_DB=customer_service \
  -e POSTGRES_PASSWORD=postgres123 \
  postgres:15-alpine

docker run -d -p 6379:6379 redis:7-alpine

# 执行数据库初始化
psql -h localhost -U postgres -d customer_service -f ../sql/init.sql

# 启动管理模块
mvn spring-boot:run -pl admin-module

# 或启动用户门户模块
mvn spring-boot:run -pl portal-module
```

#### 前端开发

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview
```

## 核心功能模块

### 1. 管理后台 (`/admin`)

#### 1.1 客服工作台 (`/admin/chat`)

- **三栏布局**: 会话列表 + 聊天窗口 + 用户信息
- **会话管理**: 全部、排队中、我的筛选
- **消息功能**: 文本、图片、文件、表情支持
- **用户信息面板**:
  - Tab 1: 用户资料 (IP归属地、设备、浏览器、标签)
  - Tab 2: 知识库搜索
  - Tab 3: 快捷回复库
- **实时提醒**: 浏览器通知、声音提醒、标题闪烁

#### 1.2 仪表板 (`/admin/dashboard`)

- 实时统计 (总会话、进行中、满意度、在线客服)
- 今日概览图表 (D3.js)
- 最近会话列表

#### 1.3 系统设置 (`/admin/settings`)

- 项目配置 (欢迎语、主题色、工作时间)
- 客服管理 (添加、编辑、删除)
- 知识库管理
- 权限管理

### 2. 用户门户 (`/portal`)

#### 2.1 FAQ首页 (`/portal`)

- 搜索框 (全文检索)
- 热门问题展示
- 问题分类展示
- 联系客服按钮

#### 2.2 Web聊天窗口 (`/portal/chat`)

- 在线客服咨询
- 支持文本、图片、文件发送
- 会话评价功能

### 3. H5移动端 (`/mobile/chat`)

#### 3.1 完整功能

- **URL参数支持**:
  - `token`: 认证令牌
  - `project_id`: 项目ID
  - `uid`: 用户ID
  - `avatar`: 用户头像
  - `nick_name`: 用户昵称
  - `phone`: 用户电话
  - `device_type`: 设备类型
- **消息类型**: 文本、语音、表情、图片、文件
- **底部工具**: 输入框、表情面板、发送按钮
- **快捷操作**: 结束会话、评价反馈

### 4. 数据模型

#### 核心表设计

- `projects`: 项目/租户隔离
- `agents`: 客服账户管理
- `users`: 访客用户信息
- `conversations`: 会话管理
- `messages`: 消息记录
- `group_chats`: 多人协作群聊
- `kb_articles`: 知识库
- `tickets`: 工单系统

详见 `sql/init.sql`

## API端点

### 公共API (`/api/pub/`)

- `POST /api/pub/auth/visitor` - 访客登录
- `GET /api/pub/config` - 获取项目配置
- `POST /api/pub/file/upload` - 文件上传

### 管理端API (`/admin/`)

- `POST /admin/auth/login` - 客服登录
- `GET /api/admin/conversations/pending` - 排队会话
- `GET /api/admin/conversations/my` - 我的会话
- `POST /api/admin/user/{userId}/tag` - 打标签
- `POST /api/admin/user/{userId}/block` - 拉黑用户
- `POST /api/admin/group-chats` - 创建群聊

### 用户端API (H5/Portal)

- `GET /h5/chat/init` - 初始化会话
- `POST /h5/chat/send` - 发送消息
- `GET /api/faq` - 获取FAQ列表

## Nginx配置详解

```nginx
# 静态资源 - 7天缓存
location ~* \.(js|css|png|jpg|...)$ {
    expires 7d;
}

# 后端API代理
location /api/ {
    proxy_pass http://backend-api:8080/;
}

# WebSocket 代理 (WuKongIM)
location /ws/ {
    proxy_pass http://wukongim:5200/;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "Upgrade";
}

# SPA 路由回退
location / {
    try_files $uri $uri/ /index.html;
}
```

## 部署指南

### 生产环境部署

```bash
# 1. 构建镜像
docker-compose build

# 2. 启动服务
docker-compose up -d

# 3. 查看日志
docker-compose logs -f

# 4. 访问应用
# 通过 http://your-domain.com 访问
```

### 环境变量配置

后端服务环境变量 (`docker-compose.yml`):

```yaml
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/customer_service
SPRING_DATASOURCE_USERNAME: postgres
SPRING_DATASOURCE_PASSWORD: postgres123
SPRING_REDIS_HOST: redis
ROCKETMQ_NAMESRV_ADDR: rocketmq-namesrv:9876
WUKONGIM_API_URL: http://wukongim:5000
```

## 定时任务

`scheduler` 模块包含以下定时清理任务:

1. **每天2点**: 清理过期聊天记录
2. **每天3点**: 清理过期图片资源
3. **每周一4点**: 清理过期用户记录
4. **每周一5点**: 清理过期会话记录

## 安全性

- **认证**: JWT Token
- **数据传输**: HTTPS (需配置证书)
- **敏感词过滤**: 需在业务逻辑中实现
- **XSS防护**: Vue3自动escaping
- **CSRF防护**: 需配置CSRF token
- **数据脱敏**: API返回脱敏处理

## 性能优化

### 前端优化

- Vite代码分割 (Admin/Portal/H5独立chunk)
- 图片懒加载
- 消息虚拟滚动 (大量消息场景)

### 后端优化

- 数据库连接池 (HikariCP)
- Redis缓存 (会话、用户信息)
- 消息队列异步处理
- 数据库索引优化

### 网络优化

- Nginx gzip压缩
- CDN分发静态资源
- WebSocket长连接 (WuKongIM)

## 监控与告警

需配置:

- 应用性能监控 (APM)
- 日志收集 (ELK)
- 告警规则 (AlertManager)

## 故障排查

### 常见问题

#### 1. 服务无法连接

```bash
# 检查容器状态
docker-compose ps

# 查看服务日志
docker-compose logs backend-api
docker-compose logs frontend
```

#### 2. 数据库连接错误

```bash
# 检查PostgreSQL是否运行
docker ps | grep postgres

# 重新初始化数据库
docker-compose down
docker volume rm customer-service_postgres_data
docker-compose up -d
```

#### 3. WebSocket连接失败

```bash
# 检查WuKongIM是否运行
curl http://localhost:5000/health

# 查看WebSocket代理配置
cat nginx/default.conf | grep ws
```

## 开发规范

### 后端

- Entity类: `com.customer_service.shared.entity`
- Repository: `*.repository`
- Controller: `*.controller`
- Service: `*.service` (TODO)
- DTO: `com.customer_service.shared.dto`

### 前端

- 组件: `src/components`
- 布局: `src/layouts`
- 页面: `src/views`
- 路由: `src/router`
- 工具: `src/utils`
- API: `src/api`

## 许可证

MIT License

## 联系方式

项目文档: [PRD-3.md](PRD-3.md)

---

**最后更新**: 2026年1月20日
