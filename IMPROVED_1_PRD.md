# Mini-Customer-Service（极简客服系统）PRD v2.0

## 1. 产品概述

### 1.1 核心定位
为中小企业提供开箱即用的全渠道客服解决方案，支持Web/H5多端接入，基于WuKongIM实现低延迟实时通讯。

### 1.2 技术架构

**后端架构（Spring Boot 3.4.1 + JDK 21）**
- 模块化设计：
  - `admin-module`：管理端API（客服工作台、系统管理）
  - `portal-module`：用户端API（访客门户、H5接口）
  - `scheduler`：定时任务模块（数据清理、工单自动关闭）
  - `shared`：公共模块（实体、DTO、工具类、第三方服务集成）

**前端架构（Vue 3.4 + Vite 7.0 + TypeScript）**
- 统一前端工程，通过路由区分三大业务场景：
  - `/admin`：管理端（Element Plus）
  - `/portal`：访客门户（Element Plus）
  - `/mobile`和`/h5`：移动端（Vant UI）

**数据存储**
- PostgreSQL 15：主数据库
- Redis 7：缓存与会话管理
- 阿里云OSS：文件存储

## 2. 核心常量定义

### 2.1 会话状态（ConversationStatus）
```java
QUEUED = "queued"    // 排队中
ACTIVE = "active"    // 进行中
CLOSED = "closed"    // 已结束
```

### 2.2 消息类型（MessageType）
```java
TEXT = "text"        // 文本消息
IMAGE = "image"      // 图片消息
FILE = "file"        // 文件消息
```

### 2.3 工单状态（TicketStatus）
```java
OPEN = "open"                    // 待处理
PROCESSING = "processing"        // 处理中
RESOLVED = "resolved"            // 已解决
CLOSED = "closed"               // 已关闭
```

### 2.4 工单优先级（TicketPriority）
```java
LOW = "low"          // 低优先级
MEDIUM = "medium"    // 中优先级
HIGH = "high"        // 高优先级
URGENT = "urgent"    // 紧急
```

### 2.5 客服工作状态（WorkStatus）
```java
ONLINE = "online"    // 在线
OFFLINE = "offline"  // 离线
BUSY = "busy"        // 忙碌
```

### 2.6 角色类型（RoleCode）
```java
ADMIN = "admin"              // 管理员
SUPER_ADMIN = "super_admin"   // 超级管理员
AGENT = "agent"              // 客服
```

### 2.7 WuKongIM频道类型（WKChannelType）
```java
PERSONAL = 1           // 个人频道（一对一私聊）
GROUP = 2              // 群组频道
VISITOR = 10           // 访客频道（客服场景）
```

### 2.8 设备类型（DeviceType）
```java
WEB = 1   // Web端（客服工作台）
H5 = 2    // H5移动端（用户聊天）
```

## 3. 核心业务流程

### 3.1 会话管理流程
1. 访客发起咨询 → 创建会话（status=queued）
2. 客服接入会话 → 更新状态（status=active）
3. 消息收发 → 实时通讯
4. 结束会话 → 更新状态（status=closed）

### 3.2 消息发送流程
1. 前端通过WuKongIM SDK发送消息
2. 后端接收消息并持久化到messages表
3. 实时推送到对应频道
4. 更新会话的lastMessageContent和lastMessageTime

## 4. 数据模型设计

### 4.1 核心表结构

**projects表**
- 项目/租户管理
- 支持多项目数据隔离
- 配置项存储在config字段（JSONB）

**sys_users表**
- 系统用户管理
- 关联sys_roles表实现权限控制

**agents表**
- 客服坐席管理
- 包含工作状态、最大接待量等字段

**users表**
- 访客用户管理
- 支持游客和注册用户
- 包含设备类型、来源URL等信息

**conversations表**
- 会话管理
- 索引设计：
  - idx_conv_project_status：按项目和状态查询
  - idx_conv_agent_active：查询客服当前会话
  - idx_conv_user_history：用户历史会话
  - idx_conv_updated：会话列表排序

**messages表**
- 消息记录
- 使用JSONB存储消息内容
- 索引设计：
  - idx_messages_conv_time：加载历史记录
  - idx_messages_msg_id：消息去重

**tickets表**
- 工单管理
- 关联ticket_events表记录流转历史

## 5. API接口设计

### 5.1 管理端API（/api/admin）

**会话管理**
- GET /api/admin/conversations/pending - 获取排队会话
- GET /api/admin/conversations/my - 获取我的会话
- POST /api/admin/conversations/{id}/accept - 接入会话
- POST /api/admin/conversations/{id}/close - 结束会话
- GET /api/admin/conversations/statistics - 获取统计数据

**客服管理**
- GET/POST /api/admin/agents - 客服列表/创建
- PUT /api/admin/agents/{id} - 更新客服信息
- DELETE /api/admin/agents/{id} - 删除客服

**知识库管理**
- GET /api/admin/kb/articles - 文章列表
- POST /api/admin/kb/articles - 创建文章
- PUT /api/admin/kb/articles/{id} - 更新文章
- DELETE /api/admin/kb/articles/{id} - 删除文章

**工单管理**
- GET /api/admin/tickets - 工单列表
- POST /api/admin/tickets - 创建工单
- PUT /api/admin/tickets/{id} - 更新工单
- GET /api/admin/tickets/{id}/events - 工单流转记录

### 5.2 用户端API（/api/pub）

**认证**
- POST /api/pub/auth/visitor - 访客登录

**配置**
- GET /api/pub/config - 获取项目配置

**文件上传**
- POST /api/pub/file/upload - 文件上传

### 5.3 H5端API（/h5）

**会话**
- GET /h5/chat/init - 初始化会话
- POST /h5/chat/send - 发送消息

**用户**
- GET /h5/chat/user-info - 获取用户信息

## 6. 前端路由配置

### 6.1 管理端路由（/admin）
- /admin/dashboard - 数据仪表板
- /admin/workbench - 客服工作台
- /admin/projects - 项目管理
- /admin/users - 用户管理
- /admin/agents - 客服管理
- /admin/roles - 角色管理
- /admin/menus - 菜单管理
- /admin/knowledge - 知识库管理
- /admin/customers - 客户管理
- /admin/customer-tags - 客户标签管理
- /admin/quick-replies - 快捷回复管理
- /admin/tickets - 工单管理
- /admin/settings - 系统设置

### 6.2 门户路由（/portal）
- /portal - FAQ首页
- /portal/chat - Web聊天窗口

### 6.3 移动端路由（/mobile和/h5）
- /mobile/chat - H5聊天界面
- /h5/chat - H5聊天界面（兼容）

## 7. 前端常量配置

### 7.1 IM配置
```typescript
IMPayloadType = {
  TEXT: 1,
  IMAGE: 2,
  FILE: 3
}
IM_INITIAL_LOAD_LIMIT = 50      // 首次加载消息数
IM_LOAD_MORE_LIMIT = 30         // 加载更多消息数
AGENT_UID_PREFIX = 'agent_'     // 客服UID前缀
```

### 7.2 应用配置
```typescript
MAX_CHARS_PER_MESSAGE = 200          // 单条消息最大字数
MAX_CHARS_PER_MINUTE = 1000        // 每分钟最大总字数
RATE_LIMIT_WINDOW = 60000           // 频率限制时间窗口（毫秒）
WELCOME_INTERVAL = 300000           // 欢迎语展示间隔（毫秒）
SCROLL_THRESHOLD = 50              // 滚动加载触发阈值（像素）
API_TIMEOUT = 10000                 // API请求超时时间（毫秒）
DEFAULT_PAGE_SIZE = 10              // 默认分页大小
WORKBENCH_PAGE_SIZE = 20           // 工作台会话分页大小
```

### 7.3 文件上传配置
```typescript
MAX_UPLOAD_SIZE = 10485760          // 最大上传文件大小（10MB）
ALLOWED_IMAGE_TYPES = [
  'image/jpeg', 'image/png', 'image/gif', 'image/webp'
]
ALLOWED_FILE_TYPES = [
  'image/jpeg', 'image/png', 'image/gif', 'image/webp',
  'application/pdf',
  'application/msword',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  'application/vnd.ms-excel',
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  'text/plain'
]
```

## 8. 定时任务

### 8.1 数据清理任务（DataCleanupScheduler）
- 每天凌晨2点：清理过期聊天记录
- 每天凌晨3点：清理过期图片资源
- 每周一凌晨4点：清理过期用户记录
- 每周一凌晨5点：清理过期会话记录

### 8.2 工单自动关闭任务（TicketAutoCloseScheduler）
- 自动关闭长时间未处理的工单
- 根据优先级设置不同的超时时间

## 9. 部署架构

### 9.1 Docker容器化
- frontend：Nginx托管前端静态资源
- backend-admin：管理端API服务
- backend-portal：用户端API服务
- postgres：PostgreSQL数据库
- redis：Redis缓存
- wukongim：WuKongIM服务

### 9.2 Nginx配置要点
- 静态资源缓存：7天
- API代理：/api路径代理到后端服务
- WebSocket代理：/ws路径代理到WuKongIM
- SPA路由回退：所有路径回退到/index.html

## 10. 权限控制

### 10.1 基于角色的访问控制（RBAC）
- 使用@RequirePermission注解控制接口访问
- 角色代码：super_admin、admin、agent
- 菜单权限：通过sys_roles表的permissions字段（JSONB）配置

### 10.2 前端路由守卫
- 检查token有效性
- 检查菜单权限
- 无权限时重定向到有权限的页面

## 11. 国际化支持

### 11.1 语言配置
- 支持中文（zh）和英文（en）
- 使用Vue I18n实现多语言
- 后端使用I18nUtil实现多语言

### 11.2 翻译文件
- 前端：src/locales/zh.ts和src/locales/en.ts
- 后端：通过message.properties配置

## 12. 安全措施

### 12.1 认证与授权
- JWT Token认证
- Token过期时间配置
- 刷新Token机制

### 12.2 数据安全
- 密码哈希存储
- 敏感数据脱敏
- SQL注入防护
- XSS防护

## 13. 性能优化

### 13.1 前端优化
- Vite代码分割（Admin/Portal/H5独立chunk）
- 图片懒加载
- 消息虚拟滚动
- 静态资源缓存

### 13.2 后端优化
- 数据库连接池（HikariCP）
- Redis缓存（会话、用户信息）
- 数据库索引优化
- 批量查询优化

## 14. 监控与日志

### 14.1 日志管理
- 使用Logback进行日志管理
- 日志级别配置
- 日志文件滚动策略

### 14.2 监控指标
- API响应时间
- 消息发送成功率
- 系统资源使用率
- 错误率统计
