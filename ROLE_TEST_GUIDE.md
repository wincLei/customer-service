# 角色区分测试指南

## 功能说明

系统现已实现角色区分：

- **管理员 (Admin)**：登录后看到统计数据Dashboard
- **客服 (Agent)**：登录后看到三栏工作台Workbench

## 测试账号

### 管理员账号

- 用户名：`admin` 或 `admin001`
- 密码：`admin123`
- 验证码：输入 `2`
- 登录后：自动跳转到 `/admin/dashboard`
- 界面：展示数据概览、满意度统计、客服状态

### 客服账号

- 用户名：`agent1` 或 `agent001`
- 密码：`admin123`
- 验证码：输入 `2`
- 登录后：自动跳转到 `/admin/workbench`
- 界面：三栏布局（会话列表 + 聊天窗口 + 用户信息）

## 测试步骤

### 1. 测试管理员Dashboard

```bash
# 访问登录页
http://localhost/login

# 登录admin账号
用户名: admin
密码: admin123
验证码: 2

# 预期结果
✓ 自动跳转到 /admin/dashboard
✓ 显示4个统计卡片：排队中、进行中会话、今日会话数、今日消息数
✓ 显示满意度统计和客服状态
✓ 左侧导航只显示"数据"和"设置"图标
```

### 2. 测试客服Workbench

```bash
# 退出admin账号，重新登录

# 登录agent账号
用户名: agent1
密码: admin123
验证码: 2

# 预期结果
✓ 自动跳转到 /admin/workbench
✓ 显示三栏布局
  - 左栏：会话列表（排队中/我的会话）
  - 中栏：聊天窗口
  - 右栏：用户信息面板
✓ 左侧导航显示"工作台"和"设置"图标
✓ 可以查看和处理会话
```

## 关键改动

### 1. 路由配置 (`router/index.ts`)

- 根据用户角色动态重定向
- admin → `/admin/dashboard`
- agent → `/admin/workbench`

### 2. AdminLayout组件

- 根据`userRole`显示不同的导航菜单
- admin：显示"数据"图标
- agent：显示"工作台"图标

### 3. 登录逻辑 (`Login.vue`)

- 根据用户名判断角色（临时方案）
  - 包含"admin" → role='admin'
  - 包含"agent" → role='agent'
- 保存role到localStorage
- 根据role跳转到不同页面

### 4. Dashboard页面

- 全新的统计数据界面
- 4个渐变色统计卡片
- 满意度统计（评分 + 进度条）
- 客服在线状态列表
- 每30秒自动刷新数据

### 5. Workbench页面

- 从localStorage获取agentId
- 根据当前登录用户加载对应的会话

## API依赖

Dashboard使用的API：

- `GET /api/admin/conversations/pending?projectId=1` - 获取排队数
- （其他统计数据暂时使用模拟数据）

Workbench使用的API：

- `GET /api/admin/conversations/pending?projectId=1` - 排队中会话
- `GET /api/admin/conversations/my?agentId={id}&projectId=1` - 我的会话
- `GET /api/admin/messages/conversation/{id}` - 会话消息
- `POST /api/admin/conversations/{id}/accept` - 接入会话
- `POST /api/admin/messages/send` - 发送消息

## 已知限制

1. **角色判断**：目前基于用户名包含"admin"或"agent"来判断，实际应从后端API返回
2. **AgentId获取**：从用户名提取数字ID（临时方案）
3. **统计数据**：部分数据为模拟数据，需要后端实现真实的统计API

## 下一步优化

1. 后端实现真实的登录API，返回用户完整信息（包含role和agentId）
2. 实现统计数据API（今日会话数、消息数、满意度等）
3. 添加D3.js图表展示趋势数据
4. 实现客服工作量分布图表
