# 客服系统 PRD

## 1. 产品概述

- **产品愿景**：打造一款稳定、高效且界面现代化的全渠道在线客服系统。支持多端接入（Web/H5），通过统一的前端架构降低维护成本，借助 WuKongIM 实现低延迟可靠通讯，参考行业优秀的 UI 交互体验，提升客服工作效率。
- **UI 设计参考**：
  - 界面风格、布局结构参考 [CRMChat](https://gitee.com/ZhongBangKeJi/CRMChat)。
  - **核心特征**：极简扁平化设计、三栏式布局（菜单-列表-会话）、清晰的信息层级、专业的配色方案（以蓝白灰为主）。
- **技术栈**：
  - **后端**：Spring Boot（模块化单体），JDK 21+
  - **前端**：Vue3 + Vite 7.0 + TypeScript + Pinia + Vue Router + D3.js（**单仓库多模块架构**）+ node v22.12.0
  - **UI 框架**：Element Plus (Admin/PC端) / Vant UI (H5端) / TailwindCSS
  - **IM 中间件**：[WuKongIM](https://github.com/WuKongIM/WuKongIM)
  - **数据存储**：PostgreSQL, Redis，阿里云对象存储oss(存储上传的文件)
  - **消息队列**：RocketMQ
- **目标用户**：网站/应用运营方的客服团队与终端用户（访客/注册用户）。
- **项目结构**：
  ```
  ├── docker-compose.yml           # 容器编排
  ├── backend/                     # 后端主目录
  │   ├── admin-module/            # 管理端API
  │   ├── portal-module/           # 用户端API
  │   ├── scheduler/               # 定时任务,清理过期聊天记录、图片资源、用户记录、会话记录
  │   └── shared/                  # 公共模块
  ├── frontend/                    # 统一前端工程
  │   ├── src/
  │   │   ├── layouts/             # 布局 (AdminLayout, MobileLayout, WebLayout)
  │   │   ├── views/
  │   │   │   ├── admin/           # 客服工作台 & 管理后台
  │   │   │   ├── portal/          # Web 访客咨询门户
  │   │   │   └── h5/              # H5 移动端聊天
  │   │   └── router/              # 路由配置
  │   └── package.json
  └── nginx/                       # Nginx配置
  ```

## 2. 角色与权限

- **访客/用户 (User)**：
  - 能够访问 Web 门户或 H5 链接。
  - 权限：发起咨询、发送消息、会话评价、查看FAQ、提交工单、查看工单进度。
- **客服 (Agent)**：
  - 登录管理后台工作台。
  - 权限：实时对话、转接/协作、处理工单、快捷回复、知识库检索、标签管理、基于当前会话创建多人聊天室、拉黑、查看历史记录、结束会话、邀请评价。
- **管理员 (Admin)**：
  - 登录管理后台工作台。
  - 权限：排班管理、坐席配置、报表查看、质检、敏感词策略、SLA 策略配置、项目管理、知识库管理、系统配置、渠道配置、权限管理、数据治理、监控与告警。

## 3. 核心业务流程

- **路由分发流程**：
  - 用户访问 `/admin` -> 加载管理端资源 -> 客服/管理员登录 -> 进入工作台（CRMChat 风格）。
  - 用户访问 `/portal` -> 加载 Web 门户资源 -> 访客查看 FAQ/发起咨询/提交工单。
  - 用户访问 `/mobile/chat` -> 加载 H5 资源 -> 手机端全屏聊天,支持文本、图片、文件、表情等消息类型；访客查看 FAQ/提交工单。
- **实时通讯流程**：
  - 前端初始化 WuKongIM SDK -> 连接 WS -> 鉴权（Token）-> 消息收发 -> 界面渲染。

## 4. 功能模块详解

### 4.1 统一前端架构 (Frontend)

基于 Vue Router 的 `routes` 配置区分三大业务场景：

#### A. 管理后台 & 客服工作台 (`/admin`)

> **UI 规范**：参考 CRMChat PC 端。左侧一级导航条（极窄），次级为列表/功能区，右侧为主操作区。

1.  **工作台 (Workbench)**：
    - **三栏布局**：
      - 左侧：会话列表（显示未读红点、最后一条消息、时间、用户昵称）。支持按“全部”、“排队中”、“我的”筛选。
      - 中间：聊天窗口（气泡式对话、支持富文本、图片预览、文件下载、消息撤回）。顶部显示用户状态（在线/离线/输入中）。
      - 右侧：用户信息与工具栏（用户画像、标签管理、历史工单、快捷回复库、知识库搜索面板）。
    - **消息提醒**：浏览器通知、声音提醒（滴滴声）、网页 Title 闪烁。
2.  **系统管理**：
    - **项目管理**：创建不同业务线项目（Project），隔离知识库和用户数据。
    - **坐席与分组**：添加客服账号，配置技能组，设置最大接待量。
    - **知识库**：富文本编辑 FAQ，支持类目管理。
    - **数据报表**：基于 D3.js 渲染的实时大屏（排队数、吞吐量）与历史报表（满意度、响应时长）。
    - **敏感词与黑名单**：设置拦截策略。

#### B. Web 访客门户 (`/portal`)

> **UI 规范**：悬浮球或独立咨询页，简洁清爽。

- **功能**：
  - FAQ 自助查询（搜索框 + 热门问题）。
  - 发起人工咨询（支持匿名或登录态）。
  - 留言板/工单提交（无客服在线时触发）。

#### C. H5 移动端聊天 (`/mobile/chat`)

> **UI 规范**：仿微信/主流 IM 布局，适配移动端触控。

- **URL 参数解析**：
  - 自动解析 URL 中的 `token`, `project_id`, `uid`, `avatar` , `nick_name` , `phone` , `device_type`等参数进行静默登录或身份绑定。
- **功能**：
  - 底部输入框：支持文本、语音（录音）、表情面板、更多（发图/文件）、留言板/工单提交（无客服在线时触发）。
  - 顶部导航：显示客服昵称/状态，支持“结束会话”按钮。
  - 评价页：会话结束后的星级评价弹窗。

### 4.2 后端服务 (Backend)

- **Auth & User Module**：处理 JWT 签发、WuKongIM Token 交换、访客身份生成。
- **Chat Module**：消息持久化、会话状态管理（排队/进行中/结束）、历史记录查询。
- **Project Module**：多租户/多项目逻辑隔离。
- **Extension Module**：标签、拉黑、知识库、自动回复机器人逻辑。

## 5. 系统架构与集成

### 5.1 前端路由策略

```javascript
// router/index.ts 伪代码
const routes = [
  {
    path: "/admin",
    component: () => import("@/layouts/AdminLayout.vue"),
    meta: { requiresAuth: true, role: "agent" },
    children: [
      { path: "dashboard", component: Dashboard },
      { path: "chat", component: Workbench }, // CRMChat风格工作台
      { path: "settings", component: Settings },
    ],
  },
  {
    path: "/portal",
    component: () => import("@/layouts/PortalLayout.vue"),
    children: [
      { path: "", component: FAQHome },
      { path: "chat", component: WebChatWindow },
    ],
  },
  {
    path: "/mobile/chat",
    component: () => import("@/layouts/MobileLayout.vue"),
    children: [
      { path: "", component: H5Chat }, // 纯H5聊天界面
    ],
  },
];
```

### 5.2 部署架构 (Docker Optimized)

Nginx 作为统一入口，根据路径分发静态资源或代理 API。

```yaml
version: "3.8"
services:
  # 后端服务
  backend-api:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      - DB_HOST=postgres
      - REDIS_HOST=redis
      - IM_SERVER=wukongim

  # 统一前端 (Nginx hosting built files)
  frontend:
    image: nginx:alpine
    volumes:
      - ./frontend/dist:/usr/share/nginx/html
      - ./nginx/default.conf:/etc/nginx/conf.d/default.conf
    ports:
      - "80:80"
    depends_on:
      - backend-api

  # 基础组件
  wukongim:
    image: wukongim/wukongim:latest
    ports:
      - "5000:5000" # IM API
      - "5100:5100" # IM TCP
      - "5200:5200" # IM WS
  postgres:
    image: postgres:15
  redis:
    image: redis:7
  rocketmq:
    image: apache/rocketmq:5.1.4
```

### 5.3 Nginx 配置示例

由于是单页面应用 (SPA)，需要处理 History 模式的路由回退。

```nginx
server {
    listen 80;

    # 静态资源根目录
    root /usr/share/nginx/html;
    index index.html;

    # 统一前端入口
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 后端 API 代理
    location /api/ {
        proxy_pass http://backend-api:8080/;
        proxy_set_header Host $host;
    }

    # WuKongIM WebSocket 代理
    location /ws/ {
        proxy_pass http://wukongim:5200/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "Upgrade";
    }
}
```

## 6. 数据模型设计 (Schema Design)

> **设计原则**：
>
> 1.  **物理隔离与逻辑隔离**：所有业务表必须包含 `project_id` 以支持多项目/多租户。
> 2.  **读写分离优化**：针对高频查询字段（如 `status`, `created_at`, `project_id`）建立索引。
> 3.  **扩展性**：使用 `JSONB` 类型存储消息内容和用户扩展属性，减少 schema 变更。

### 6.1 核心系统与租户 (System & Tenant)

```sql
-- 项目表 (租户)
CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    app_key VARCHAR(64) UNIQUE, -- 用于API鉴权
    app_secret VARCHAR(128),
    config JSONB DEFAULT '{}', -- 存储欢迎语、主题色、工作时间等配置
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 客服/管理员表 (系统用户)
CREATE TABLE agents (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id),
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    avatar VARCHAR(500),
    email VARCHAR(100),
    role VARCHAR(20) DEFAULT 'agent', -- agent, supervisor, admin
    status VARCHAR(20) DEFAULT 'offline', -- offline, online, busy
    max_load INTEGER DEFAULT 5, -- 最大接待量
    current_load INTEGER DEFAULT 0, -- 当前接待量
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
-- 索引
CREATE INDEX idx_agents_project_username ON agents(project_id, username);
CREATE INDEX idx_agents_status ON agents(project_id, status); -- 用于分配算法查找在线客服

-- 快捷回复表
CREATE TABLE quick_replies (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id),
    creator_id BIGINT, -- 创建人，为空则是公共库
    content TEXT NOT NULL,
    category VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_quick_replies_project ON quick_replies(project_id);
```

### 6.2 用户与 CRM 扩展 (User & CRM)

```sql
-- 访客/用户表
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id),
    uid VARCHAR(100) NOT NULL, -- 外部系统的唯一ID，映射 WuKongIM 的 UID
    nickname VARCHAR(100),
    avatar VARCHAR(500),
    email VARCHAR(100),
    phone VARCHAR(20),
    device_type VARCHAR(20), -- pc, mobile, tablet
    source_url TEXT, -- 来源页面
    open_id VARCHAR(100), -- 微信/第三方 OpenID
    city VARCHAR(50), -- IP 解析城市
    extra_info JSONB, -- 存储其他自定义参数
    last_active_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(project_id, uid) -- 确保同一项目下 UID 唯一
);
-- 索引
CREATE INDEX idx_users_project_phone ON users(project_id, phone);
CREATE INDEX idx_users_project_uid ON users(project_id, uid);

-- 用户标签关联表
CREATE TABLE user_tags (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    tag_name VARCHAR(50) NOT NULL,
    tagged_by BIGINT, -- 打标签的客服ID
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, tag_name) -- 防止重复打标
);
CREATE INDEX idx_user_tags_project_tag ON user_tags(project_id, tag_name); -- 用于按标签筛选用户

-- 用户备注表 (客服笔记)
CREATE TABLE user_remarks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    agent_id BIGINT NOT NULL, -- 添加备注的客服
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_user_remarks_user ON user_remarks(user_id);

-- 用户拉黑表
CREATE TABLE user_blocks (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    reason TEXT,
    blocked_by BIGINT,
    block_until TIMESTAMP WITH TIME ZONE, -- 封禁截止时间，NULL 为永久
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_user_blocks_check ON user_blocks(user_id, block_until); -- 登录鉴权时快速检查
```

### 6.3 会话与消息 (Conversation & Chat)

```sql
-- 会话表 (核心)
CREATE TABLE conversations (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    agent_id BIGINT REFERENCES agents(id), -- 当前接待客服，为空则未分配
    status VARCHAR(20) NOT NULL DEFAULT 'queued', -- queued(排队), active(进行中), closed(已结束)
    priority INTEGER DEFAULT 0, -- 优先级，用于VIP排队
    score INTEGER, -- 评分 (1-5)
    score_remark TEXT, -- 评价内容
    last_message_content TEXT, -- 冗余字段，用于列表展示
    last_message_time TIMESTAMP WITH TIME ZONE, -- 用于列表排序
    started_at TIMESTAMP WITH TIME ZONE, -- 客服接入时间
    ended_at TIMESTAMP WITH TIME ZONE, -- 结束时间
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
-- 索引
CREATE INDEX idx_conv_project_status ON conversations(project_id, status); -- 查找排队中或进行中的会话
CREATE INDEX idx_conv_agent_active ON conversations(agent_id, status) WHERE status = 'active'; -- 查找客服当前的会话
CREATE INDEX idx_conv_user_history ON conversations(user_id, created_at DESC); -- 用户历史会话
CREATE INDEX idx_conv_updated ON conversations(project_id, last_message_time DESC); -- 会话列表默认排序

-- 消息记录表 (持久化与审计)
-- 虽然 WuKongIM 有存储，但业务系统通常需要一份自己的拷贝用于复杂查询和关联
CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    conversation_id BIGINT NOT NULL REFERENCES conversations(id),
    msg_id VARCHAR(64) NOT NULL, -- WuKongIM 的消息ID
    sender_type VARCHAR(10) NOT NULL, -- user, agent, system
    sender_id BIGINT NOT NULL, -- user_id 或 agent_id
    msg_type VARCHAR(20) NOT NULL, -- text, image, file, voice, rich
    content JSONB NOT NULL, -- 消息内容结构体
    is_revoked BOOLEAN DEFAULT FALSE, -- 是否撤回
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
-- 索引
CREATE INDEX idx_messages_conv_time ON messages(conversation_id, created_at); -- 加载历史记录
CREATE INDEX idx_messages_msg_id ON messages(msg_id); -- 消息去重或状态更新

-- 多人协作/临时群聊表
CREATE TABLE group_chats (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id),
    name VARCHAR(100),
    owner_agent_id BIGINT NOT NULL, -- 创建人
    related_conversation_id BIGINT REFERENCES conversations(id), -- 关联的主会话
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 群聊成员表
CREATE TABLE group_chat_members (
    id BIGSERIAL PRIMARY KEY,
    group_chat_id BIGINT NOT NULL REFERENCES group_chats(id),
    member_type VARCHAR(10) NOT NULL, -- agent, user
    member_id BIGINT NOT NULL,
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(group_chat_id, member_type, member_id)
);
```

### 6.4 知识库 (Knowledge Base)

```sql
-- 知识库分类
CREATE TABLE kb_categories (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id),
    parent_id BIGINT REFERENCES kb_categories(id),
    name VARCHAR(100) NOT NULL,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 知识库文章
CREATE TABLE kb_articles (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id),
    category_id BIGINT REFERENCES kb_categories(id),
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL, -- 富文本内容
    view_count INTEGER DEFAULT 0,
    hit_count INTEGER DEFAULT 0, -- 客服引用/发送次数
    is_published BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
-- 索引
-- 注意：实际生产中建议使用 PostgreSQL 的 GIN 索引 + tsvector 进行全文检索
CREATE INDEX idx_kb_articles_project_title ON kb_articles(project_id, title);
CREATE INDEX idx_kb_articles_category ON kb_articles(category_id);
```

### 6.5 工单系统 (Tickets)

```sql
-- 工单表
CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    creator_type VARCHAR(10) NOT NULL, -- user (留言), agent (代提)
    assignee_id BIGINT REFERENCES agents(id), -- 当前处理人
    title VARCHAR(200) NOT NULL,
    description TEXT,
    priority VARCHAR(10) DEFAULT 'medium', -- low, medium, high, urgent
    status VARCHAR(20) DEFAULT 'open', -- open, processing, resolved, closed
    contact_info VARCHAR(100), -- 用户留下的联系方式
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
-- 索引
CREATE INDEX idx_tickets_project_status ON tickets(project_id, status);
CREATE INDEX idx_tickets_user ON tickets(user_id);
CREATE INDEX idx_tickets_assignee ON tickets(assignee_id);

-- 工单流转记录
CREATE TABLE ticket_events (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL REFERENCES tickets(id),
    operator_type VARCHAR(10), -- agent, system, user
    operator_id BIGINT,
    action VARCHAR(50) NOT NULL, -- create, reply, close, transfer
    content TEXT, -- 备注或回复内容
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_ticket_events_ticket ON ticket_events(ticket_id, created_at);
```

### 6.6 补充说明

1.  **关于 `deleted_at`**：
    - 为了数据合规（GDPR等）和清理方便，大部分表（尤其是 `messages`, `users`, `conversations`）建议通过 `scheduler` 定时任务将过期数据归档到冷存储或物理删除，因此设计中未强制添加 `is_deleted` 软删除字段，通过 `status` 或业务逻辑控制可见性。
2.  **全文检索优化**：
    - 针对 `messages` 和 `kb_articles` 的搜索，后续可在 PostgreSQL 中开启 `pg_trgm` 扩展或使用 ElasticSearch 辅助，初期可直接使用 SQL `LIKE` 或 Postgres 自带的 `to_tsvector`。
3.  **WuKongIM 映射**：
    - `users.uid` 对应 WuKongIM 的用户 ID。
    - `agents.id` 对应 WuKongIM 的客服 ID（通常加前缀区分，如 `agent_101`）。
    - `messages.msg_id` 对应 WuKongIM 的唯一消息 ID，用于确保消息不丢失和去重。

## 7. API

### 公共/用户端 (`/api/pub/...`)

- `POST /api/pub/auth/visitor`: 访客匿名登录，获取 Token。
- `GET /api/pub/config`: 获取当前项目的配置（欢迎语、UI颜色配置）。
- `POST /api/pub/file/upload`: 文件/图片上传。

### Admin后端（admin-backend）

- `POST /admin/auth/login`
- `POST /admin/projects`
- `GET /admin/projects/{id}/knowledge-base`
- `POST /admin/user/{userId}/tags`
- `POST /admin/user/{userId}/block`
- `GET /admin/group-chats`
- `POST /admin/group-chats`
- `DELETE /admin/group-chats/{id}`
- `GET /api/admin/conversations/pending`: 获取排队中的会话。
- `GET /api/admin/conversations/my`: 获取当前客服的会话列表。
- `POST /api/admin/chat/group/create`: 创建多人协作群聊。
- `POST /api/admin/user/{uid}/tag`: 打标签。

### H5用户端后端（frontend-backend）

- `GET /h5/chat/init`（支持参数解析与用户信息绑定）
- `POST /h5/chat/send`（支持表情类型）
- `GET /h5/chat/user-info`（返回用户标签、备注等）

### Web用户端后端（frontend-backend）

- `GET /api/faq?project_id=`（支持按项目筛选）
- `POST /api/conversations/start`（支持传递 device_type, project_id 等）

## 8. 分配与流控策略

- 会话分配：
  - 轮询、最少会话、技能匹配、优先级。
- 队列与 SLA：
  - 排队提示、超时告警、溢出策略。
- 权限与可见性：
  - 主管可查看所有会话；客服仅能查看自己/本组会话与工单。

## 9. 非功能需求

- 安全：
  - 输入校验、XSS/CSRF 防护、敏感词过滤、数据脱敏、日志审计。
- 性能：
  - 峰值同时在线会话 1000+；消息端到端延迟<200ms。
- 可用性：
  - 断网重连、消息幂等、失败重试。
- 监控与告警：
  - 连接成功率、消息发送成功率、平均处理时长、错误率。

## 10. UI/UX 详细要求 (参考 CRMChat)

### 10.1 整体风格

- **配色**：主色调建议使用 #1890FF (科技蓝) 或 #0066CC，背景色 #F0F2F5。
- **字体**：Inter, Roboto, PingFang SC。
- **图标**：使用 Phosphor Icons 或 Element Plus Icons。

### 10.2 客服工作台 (Admin Route)

- **左侧导航栏**：收缩为图标模式（工作台、历史、统计、设置），点击展开子菜单。
- **会话列表**：
  - 选中态高亮。
  - 头像右下角显示渠道图标（Web/H5）。
  - 支持右键菜单（置顶、结束会话）。
- **聊天区域**：
  - 消息气泡区分：左侧为用户（白底黑字），右侧为客服（蓝底白字）。
  - 输入框上方需有快捷工具栏：表情、图片、文件、**快捷回复（点击直接发送）**。
- **右侧侧边栏 (Info Panel)**：
  - **Tab 1: 用户资料** - 显示 IP归属地、设备、浏览器、自定义标签（支持增删）。
  - **Tab 2: 知识库** - 搜索输入框，搜索结果点击可直接引用到输入框。

## 11. 非功能需求

- **首屏加载优化**：由于是一个大前端项目，必须配置 Vite 进行**代码分割 (Code Splitting)**。
  - 将 Admin、Portal、H5 的依赖拆分为不同的 Chunk，利用动态导入 (`import()`) 实现按需加载，避免访客加载了庞大的管理后台代码。
- **响应式布局**：Admin 端主要针对 PC 分辨率（>1280px），H5 端针对移动端分辨率。

## 12. 里程碑与交付

- **M1 基础架构**：搭建 Vue3 + Vite 单一工程，配置好路由和 Layout，完成 Nginx 代理配置。
- **M2 核心通讯**：集成 WuKongIM，实现 `/mobile/chat` 与 `/admin` 的文字互通。
- **M3 UI 复刻**：按照 CRMChat 风格完成管理端工作台 UI 开发（三栏布局、右侧资料卡）。
- **M4 业务增强**：实现参数传递绑定用户、标签管理、知识库搜索。
- **M5 完善交付**：Docker 镜像构建、部署文档、性能测试。

## 13. 验收标准

- 支持 URL 参数传递并正确绑定用户信息。
- 客服端标签、备注、拉黑功能可用。
- 多人聊天室创建、邀请、删除流程完整。
- 项目知识库隔离与检索正常。
- Docker容器化部署成功，各服务正常运行。

## 14. 风险与对策

- 参数传递安全：Token 加密、参数校验防篡改。
- 多人聊天室并发控制：成员管理、消息广播优化。
- 数据清理策略：分批删除、避免锁表。
- Docker网络通信：确保容器间网络互通，配置正确的服务发现。

## 15. 术语

- 项目：客服服务的独立业务单元，可拥有独立知识库。
- 用户标签：客服为用户打上的自定义分类标记。
- 多人聊天室：客服创建的临时群聊，可邀请多个用户参与。
- 容器化：使用Docker将应用打包为独立容器运行的技术。
