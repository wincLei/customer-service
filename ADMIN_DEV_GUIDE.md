# Admin模块开发指南

## 概述

本文档描述了admin-module（客服工作台）的核心功能实现。按照PRD-3.md的设计，实现了三栏布局的客服工作台，包括会话管理、消息收发、用户信息管理等核心功能。

## 已完成的功能

### 1. 后端架构

#### 1.1 数据模型（Entity层）

已创建的核心实体类：

- **Project**: 项目/租户信息
- **Agent**: 客服账号
- **User**: 用户信息
- **Conversation**: 会话
- **Message**: 消息
- **KbCategory**: 知识库分类
- **KbArticle**: 知识库文章
- **UserTag**: 用户标签
- **QuickReply**: 快捷回复

所有实体都包含：

- JPA注解配置
- 数据库索引优化
- @PrePersist生命周期方法
- JSONB字段支持（extInfo）

#### 1.2 Repository层

创建了对应的JPA Repository接口：

- `ProjectRepository`: 通过appKey查询项目
- `AgentRepository`: 客服查询（按项目、状态）
- `UserRepository`: 用户查询（按项目、UID、手机号）
- `ConversationRepository`: 会话查询（排队、进行中、历史记录）
  - `findByProjectIdAndStatusOrderByLastMessageTimeDesc()`: 获取指定状态的会话列表
  - `countQueuedByProjectId()`: 统计排队数量
- `MessageRepository`: 消息查询（按会话、分页加载）
- `KbArticleRepository`: 知识库文章（搜索、分类过滤）
- `UserTagRepository`: 用户标签管理
- `QuickReplyRepository`: 快捷回复查询

#### 1.3 Service层

核心业务逻辑服务：

**ConversationService**:

- `getQueuedConversations()`: 获取排队中的会话
- `getAgentConversations()`: 获取客服的会话列表
- `acceptConversation()`: 客服接入会话
- `closeConversation()`: 结束会话
- `getQueueCount()`: 获取排队数量

**MessageService**:

- `getConversationMessages()`: 获取会话消息
- `getConversationMessagesPage()`: 分页获取消息
- `sendMessage()`: 发送消息（自动更新会话时间）

**UserService**:

- `getUserByUid()`: 获取用户信息
- `createOrUpdateUser()`: 创建或更新用户
- `addUserTag()`: 添加用户标签
- `removeUserTag()`: 删除用户标签
- `getUserTags()`: 获取用户标签列表

**KbArticleService**:

- `getPublishedArticles()`: 获取已发布文章
- `getArticlesByCategory()`: 按分类获取文章
- `searchArticles()`: 搜索文章
- `createArticle()`: 创建文章
- `updateArticle()`: 更新文章
- `incrementHitCount()`: 增加文章命中次数

#### 1.4 Controller层

RESTful API接口：

**ConversationController** (`/api/admin/conversations`):

- `GET /pending?projectId={id}`: 获取排队会话列表
- `GET /my?agentId={id}`: 获取我的会话列表
- `POST /{id}/accept?agentId={id}`: 接入会话
- `POST /{id}/close`: 结束会话

**MessageController** (`/api/admin/messages`):

- `GET /conversation/{conversationId}`: 获取会话消息
- `GET /conversation/{conversationId}/page`: 分页获取消息
- `POST /send`: 发送消息

**UserController** (`/api/admin/users`):

- `GET /{uid}?projectId={id}`: 获取用户信息
- `GET /{userId}/tags`: 获取用户标签
- `POST /{userId}/tags`: 添加用户标签
- `DELETE /{userId}/tags/{tagName}`: 删除用户标签

**KbArticleController** (`/api/admin/kb/articles`):

- `GET ?projectId={id}`: 获取文章列表
- `GET /category/{categoryId}?projectId={id}`: 按分类获取
- `GET /search?projectId={id}&keyword={keyword}`: 搜索文章
- `POST`: 创建文章
- `PUT /{id}`: 更新文章
- `POST /{id}/hit`: 增加命中次数

### 2. 前端实现

#### 2.1 AdminLayout布局

位置: `frontend/src/layouts/AdminLayout.vue`

功能：

- 左侧导航栏（60px宽）
  - 工作台图标
  - 数据图标
  - 设置图标
- 顶部栏（50px高）
  - 系统标题
  - 用户信息
  - 退出登录
- 主内容区域（router-view）

样式特点：

- 深蓝色侧边栏 (#1e3a5f)
- 激活状态用蓝色高亮 (#409eff)
- 响应式布局适配

#### 2.2 Workbench工作台

位置: `frontend/src/views/admin/Workbench.vue`

**三栏布局**：

1. **左栏 - 会话列表** (320px)
   - 标签页切换：排队中 / 我的会话
   - 排队数量badge显示
   - 会话项包含：头像、昵称、时间、最后消息
   - 点击选中会话（高亮显示）
   - "接入"按钮（仅排队会话）

2. **中栏 - 聊天窗口** (flex:1)
   - 聊天头部
     - 用户头像和昵称
     - "用户信息"和"结束会话"按钮
   - 消息列表
     - 用户消息：左对齐，白色气泡
     - 客服消息：右对齐，蓝色气泡
     - 时间格式化显示
     - 自动滚动到底部
   - 输入区域
     - 工具栏：知识库、快捷回复按钮
     - 多行文本输入框
     - Ctrl+Enter快捷发送
     - 发送按钮

3. **右栏 - 用户信息面板** (340px，可隐藏)
   - 基本信息标签页
     - 用户ID、昵称
     - 会话状态标签
     - 用户标签管理（添加/删除）
   - 会话记录标签页
     - 历史会话列表
     - 时间显示

**核心功能**：

- ✅ 实时获取会话列表（5秒刷新）
- ✅ 实时获取消息列表（3秒刷新）
- ✅ 客服接入排队会话
- ✅ 发送文本消息
- ✅ 结束会话
- ✅ 用户标签管理
- ✅ 时间格式化（刚刚/X分钟前/X小时前/日期）
- ✅ 消息自动滚动
- ✅ Element Plus图标集成

#### 2.3 路由配置

更新了路由：

- 默认路由指向 `/admin/workbench`
- 登录后重定向到 `/admin/workbench`
- 工作台、数据看板、设置三个主要路由

### 3. 测试数据

创建了完整的测试数据脚本：`sql/test-data.sql`

包含：

- 1个测试项目
- 2个测试客服（agent1, agent2）
- 3个测试用户
- 3个会话（2个排队，1个进行中）
- 10条消息
- 4个知识库分类
- 3篇知识库文章
- 4条快捷回复
- 3个用户标签

## 使用指南

### 启动服务

```bash
# 停止现有服务
docker-compose down

# 重新构建并启动
docker-compose up -d --build

# 查看日志
docker-compose logs -f backend-api
```

### 导入测试数据

```bash
# 进入数据库容器
docker exec -it mini-customer-service-postgres psql -U postgres -d customer_service

# 或直接执行SQL文件
docker exec -i mini-customer-service-postgres psql -U postgres -d customer_service < sql/test-data.sql
```

### 访问系统

1. 打开浏览器访问: `http://localhost`
2. 登录账号: `admin` / `admin123`
3. 自动跳转到工作台

### API测试

```bash
# 获取排队会话
curl -H "Authorization: Bearer {token}" \
  http://localhost/api/admin/conversations/pending?projectId=1

# 获取我的会话
curl -H "Authorization: Bearer {token}" \
  http://localhost/api/admin/conversations/my?agentId=1

# 接入会话
curl -X POST -H "Authorization: Bearer {token}" \
  http://localhost/api/admin/conversations/1/accept?agentId=1

# 发送消息
curl -X POST -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{"conversationId":1,"senderId":1,"senderType":"agent","contentType":"text","content":"您好"}' \
  http://localhost/api/admin/messages/send
```

## 待完成功能

根据PRD-3.md，以下功能暂未实现（可在后续迭代中完成）：

### 后端

- [ ] WuKongIM集成（实时消息推送）
- [ ] 会话分配算法（负载均衡）
- [ ] 质检评分功能
- [ ] 数据统计API
- [ ] 系统管理API（项目、客服、权限）

### 前端

- [ ] 知识库面板UI
- [ ] 快捷回复面板UI
- [ ] 数据看板页面
- [ ] 系统设置页面
- [ ] WebSocket实时消息
- [ ] 消息撤回、引用
- [ ] 富文本编辑器
- [ ] 图片、文件上传

## 技术栈

### 后端

- Spring Boot 3.x
- JPA / Hibernate
- PostgreSQL 15
- Redis (缓存、验证码)
- Lombok

### 前端

- Vue 3 + TypeScript
- Vite 7.0
- Element Plus
- Vue Router
- Pinia (状态管理，待集成)

## 目录结构

```
backend/
  admin-module/
    src/main/java/com/customer_service/admin/
      controller/     # REST API
      service/        # 业务逻辑
      repository/     # 数据访问
      config/         # 配置类
  shared/
    src/main/java/com/customer_service/shared/
      entity/         # JPA实体
      repository/     # 共享Repository
      dto/            # 数据传输对象

frontend/
  src/
    layouts/          # 布局组件
      AdminLayout.vue
    views/
      admin/          # 管理端页面
        Workbench.vue # 工作台
        Dashboard.vue # 数据看板
        Settings.vue  # 设置
    router/           # 路由配置
    api/              # API调用
```

## 注意事项

1. **事务管理**: Service层方法使用`@Transactional`确保数据一致性
2. **权限控制**: 所有API需要Bearer Token认证
3. **多租户隔离**: 所有查询都必须带projectId进行数据隔离
4. **性能优化**:
   - Repository查询添加了索引
   - 消息列表支持分页加载
   - 前端定时刷新间隔合理（3-5秒）
5. **代码规范**:
   - 使用Lombok简化代码
   - 统一使用ApiResponse包装返回结果
   - 日志使用slf4j

## 下一步计划

1. **集成WuKongIM**
   - 替换轮询为WebSocket推送
   - 实现实时消息通知
2. **完善知识库功能**
   - 知识库搜索面板
   - 智能推荐
   - 文章预览和插入

3. **快捷回复功能**
   - 快捷回复选择面板
   - 支持模板变量
   - 个人快捷回复管理

4. **数据统计**
   - 实时工作量统计
   - 会话时长分析
   - 满意度评分

5. **系统管理**
   - 项目管理界面
   - 客服账号管理
   - 权限分配

## 参考文档

- [PRD-3.md](PRD-3.md) - 产品需求文档
- [README.md](README.md) - 项目说明
- [DEV_SETUP.md](DEV_SETUP.md) - 开发环境配置
