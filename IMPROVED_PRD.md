# 极简客服系统 - 改进版PRD

## 1. 产品定位与目标

### 1.1 核心价值主张
**一句话描述**：为中小企业提供开箱即用的全渠道客服解决方案

### 1.2 目标用户画像
- **主要用户**：中小企业的客服团队（10-100人规模）
- **次要用户**：网站访客、注册用户
- **决策者**：IT负责人、运营总监

### 1.3 成功指标
- 首屏加载时间 < 2秒
- 消息延迟 < 200ms
- 系统可用性 99.9%
- 客服平均响应时间 < 30秒

## 2. MVP功能范围（第一阶段）

### 2.1 核心功能清单

#### 2.1.1 访客端功能
```
[必须实现]
✓ 实时聊天（文本、图片）
✓ FAQ自助查询
✓ 工单提交
✓ 会话历史查看
✓ 多语言支持（中英）

[后续迭代]
○ 语音消息
○ 文件传输
○ 表情包
○ 满意度评价
```

#### 2.1.2 客服端功能
```
[必须实现]
✓ 三栏式工作台布局
✓ 实时消息接收
✓ 快捷回复
✓ 用户标签管理
✓ 会话转移
✓ 离线消息推送

[后续迭代]
○ 知识库搜索
○ 多人协作聊天室
○ 数据统计看板
○ 客户画像展示
```

#### 2.1.3 管理端功能
```
[必须实现]
✓ 项目管理
✓ 客服账号管理
✓ 基础系统设置
✓ 简单数据统计

[后续迭代]
○ 权限管理
○ 报表分析
○ 敏感词过滤
○ 质检功能
```

### 2.2 技术架构约束

#### 2.2.1 前端技术栈
```
必须使用：
- Vue 3 Composition API
- TypeScript
- Element Plus (PC端)
- Vant UI (移动端)
- Vue Router 4
- Pinia状态管理

禁止使用：
- jQuery
- 传统Options API
- 第三方UI框架（除指定外）
```

#### 2.2.2 后端技术栈
```
必须遵循：
- Spring Boot 3.x
- JDK 21+
- PostgreSQL 15+
- Redis 7+
- RESTful API设计

接口规范：
- 统一响应格式
- JWT认证
- 参数校验
- 异常处理
```

## 3. 详细的用户旅程地图

### 3.1 访客使用流程

#### 场景1：首次咨询
```
1. 访客进入网站
   ↓
2. 点击客服悬浮球
   ↓
3. 系统自动创建会话
   ↓
4. 显示欢迎语和FAQ推荐
   ↓
5. 访客选择：
   - 直接咨询客服
   - 查看FAQ自助解决
   - 提交工单
```

#### 场景2：会话中断后恢复
```
1. 访客重新访问
   ↓
2. 系统识别历史会话
   ↓
3. 显示"继续上次对话"选项
   ↓
4. 恢复聊天记录
```

### 3.2 客服工作流程

#### 场景：处理客户咨询
```
1. 客服登录工作台
   ↓
2. 查看排队列表
   ↓
3. 接入新会话
   ↓
4. 查看用户信息和历史记录
   ↓
5. 使用快捷回复或知识库
   ↓
6. 解决问题或转交他人
   ↓
7. 结束会话并邀请评价
```

## 4. 具体的界面设计规范

### 4.1 设计系统

#### 4.1.1 配色方案
```
主色调：#1890FF (科技蓝)
辅助色：#52C41A (成功绿)、#FF4D4F (警告红)
背景色：#F0F2F5 (浅灰)
文字色：#1F1F1F (深灰)、#8C8C8C (中灰)
```

#### 4.1.2 组件规范
```
按钮高度：32px (小)、40px (中)、48px (大)
圆角：4px (按钮)、8px (卡片)
阴影：0 2px 8px rgba(0,0,0,0.1)
字体：PingFang SC、Roboto
```

### 4.2 关键页面原型说明

#### 4.2.1 客服工作台布局
```
┌─────────────┬─────────────────┬──────────────┐
│             │                 │              │
│  导航菜单   │   会话列表      │   用户信息   │
│  (60px)     │   (300px)       │   (280px)    │
│             │                 │              │
├─────────────┼─────────────────┼──────────────┤
│             │                 │              │
│             │   聊天区域      │              │
│             │   (弹性填充)    │              │
│             │                 │              │
├─────────────┴─────────────────┴──────────────┤
│                   输入区域                    │
│                   (固定高度)                  │
└───────────────────────────────────────────────┘
```

## 5. API接口详细设计

### 5.1 认证接口

#### 5.1.1 客服登录
```
POST /api/admin/auth/login
请求体：
{
  "username": "string",
  "password": "string",
  "captcha": "string"
}

响应：
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "jwt_token",
    "userInfo": {
      "id": 1,
      "username": "agent001",
      "nickname": "张客服"
    }
  }
}
```

### 5.2 核心业务接口

#### 5.2.1 获取会话列表
```
GET /api/admin/conversations?page=1&size=20&status=active
响应：
{
  "code": 0,
  "data": {
    "list": [
      {
        "id": 1,
        "userId": 1001,
        "userName": "访客123",
        "lastMessage": "你好",
        "lastMessageTime": "2024-01-01T10:00:00Z",
        "unreadCount": 3,
        "status": "active"
      }
    ],
    "pagination": {
      "page": 1,
      "size": 20,
      "total": 100
    }
  }
}
```

## 6. 数据库设计详细说明

### 6.1 核心表结构

#### 6.1.1 项目表 (projects)
```sql
CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    app_key VARCHAR(64) UNIQUE NOT NULL,
    app_secret VARCHAR(128) NOT NULL,
    config JSONB DEFAULT '{}',
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

#### 6.1.2 用户表 (users)
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id),
    uid VARCHAR(100) NOT NULL,
    nickname VARCHAR(100),
    avatar VARCHAR(500),
    phone VARCHAR(20),
    email VARCHAR(100),
    device_type VARCHAR(20),
    extra_info JSONB DEFAULT '{}',
    last_active_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(project_id, uid)
);
```

## 7. 部署与运维要求

### 7.1 Docker部署配置

#### 7.1.1 docker-compose.yml
```yaml
version: "3.8"
services:
  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - backend
      
  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=postgres
    depends_on:
      - postgres
      - redis
      
  postgres:
    image: postgres:15
    environment:
      - POSTGRES_DB=customer_service
      - POSTGRES_USER=admin
      - POSTGRES_PASSWORD=password
    volumes:
      - postgres_data:/var/lib/postgresql/data
      
  redis:
    image: redis:7-alpine
    
volumes:
  postgres_data:
```

### 7.2 监控告警配置
```
必须监控的指标：
- 系统CPU使用率 > 80%
- 内存使用率 > 85%
- 数据库连接数 > 80%
- API响应时间 > 1000ms
- 错误率 > 1%

告警方式：
- 邮件通知
- 钉钉机器人
- 短信告警（严重级别）
```

## 8. 测试验收标准

### 8.1 功能测试用例

#### 8.1.1 核心功能测试
```
测试场景：访客发起咨询
前置条件：系统正常运行
测试步骤：
1. 打开访客页面
2. 点击客服按钮
3. 发送"你好"消息
4. 等待客服回复
预期结果：
✓ 页面正常加载
✓ 能够建立会话
✓ 消息能够正常发送和接收
✓ 界面响应流畅
```

### 8.2 性能测试标准
```
并发用户数：1000
平均响应时间：< 200ms
系统吞吐量：1000 QPS
内存使用率：< 70%
CPU使用率：< 60%
```

## 9. 项目管理与交付

### 9.1 开发里程碑

#### 阶段一：基础框架 (2周)
- [ ] 项目脚手架搭建
- [ ] 基础路由配置
- [ ] 用户认证系统
- [ ] 数据库表结构创建

#### 阶段二：核心功能 (4周)
- [ ] 访客聊天功能
- [ ] 客服工作台
- [ ] 消息实时通讯
- [ ] 基础管理功能

#### 阶段三：完善优化 (2周)
- [ ] 界面美化
- [ ] 性能优化
- [ ] 测试覆盖
- [ ] 部署文档

### 9.2 交付物清单
```
必须交付：
✓ 完整可运行的源代码
✓ Docker部署文件
✓ 数据库初始化脚本
✓ API接口文档
✓ 用户操作手册
✓ 系统部署文档

可选交付：
○ 单元测试代码
○ 性能测试报告
○ 安全测试报告
```

## 10. 风险评估与应对

### 10.1 技术风险
```
风险：WebSocket连接不稳定
应对：实现断线重连机制，消息确认机制

风险：数据库性能瓶颈
应对：合理设计索引，实施读写分离

风险：第三方服务不可用
应对：实现降级方案，本地缓存机制
```

### 10.2 业务风险
```
风险：用户量超出预期
应对：预留水平扩展能力

风险：功能需求变更
应对：采用敏捷开发模式，快速迭代
```

---
*本文档版本：v1.0*
*最后更新：2024年1月*