-- ============================================================
-- Mini-Customer-Service（极简客服系统）数据库初始化脚本
-- PostgreSQL 15+
-- ============================================================

-- 项目表 (租户)
CREATE TABLE IF NOT EXISTS projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    app_key VARCHAR(64) UNIQUE NOT NULL,
    app_secret VARCHAR(128),
    config JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_projects_app_key ON projects(app_key);

-- ============================================================
-- 系统用户与权限管理
-- ============================================================

-- 系统角色表
CREATE TABLE IF NOT EXISTS sys_roles (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    permissions JSONB DEFAULT '[]',
    is_system BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sys_roles_code ON sys_roles(code);

-- 系统菜单表
CREATE TABLE IF NOT EXISTS sys_menus (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) DEFAULT 'menu',
    parent_id BIGINT REFERENCES sys_menus(id),
    path VARCHAR(200),
    icon VARCHAR(100),
    sort_order INTEGER DEFAULT 0,
    is_enabled BOOLEAN DEFAULT TRUE,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sys_menus_code ON sys_menus(code);
CREATE INDEX IF NOT EXISTS idx_sys_menus_parent ON sys_menus(parent_id);

-- 系统用户表（登录账号管理，不包含业务信息）
CREATE TABLE IF NOT EXISTS sys_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar VARCHAR(500),
    role_id BIGINT REFERENCES sys_roles(id),
    status VARCHAR(20) DEFAULT 'active',
    last_login_at TIMESTAMP WITH TIME ZONE,
    last_login_ip VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sys_users_username ON sys_users(username);
CREATE INDEX IF NOT EXISTS idx_sys_users_role ON sys_users(role_id);

-- 用户-项目关联表（用于控制客服能访问哪些项目）
CREATE TABLE IF NOT EXISTS user_projects (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES sys_users(id) ON DELETE CASCADE,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, project_id)
);

CREATE INDEX IF NOT EXISTS idx_user_projects_user ON user_projects(user_id);
CREATE INDEX IF NOT EXISTS idx_user_projects_project ON user_projects(project_id);

-- 客服/坐席表（客服业务信息，一个用户只能有一个客服记录）
CREATE TABLE IF NOT EXISTS agents (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES sys_users(id) ON DELETE CASCADE,
    nickname VARCHAR(50),
    work_status VARCHAR(20) DEFAULT 'offline',
    max_load INTEGER DEFAULT 5,
    current_load INTEGER DEFAULT 0,
    skill_groups TEXT DEFAULT '[]',
    welcome_message TEXT,
    auto_reply_enabled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_agents_user ON agents(user_id);
CREATE INDEX IF NOT EXISTS idx_agents_status ON agents(work_status);

-- 快捷回复表
CREATE TABLE IF NOT EXISTS quick_replies (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id),
    creator_id BIGINT REFERENCES agents(id),
    content TEXT NOT NULL,
    category VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_quick_replies_project ON quick_replies(project_id);

-- 访客/用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id),
    uid VARCHAR(100) NOT NULL,
    external_uid VARCHAR(100),  -- 外部系统的唯一ID（可为空，表示游客）
    is_guest BOOLEAN DEFAULT TRUE,  -- 是否游客
    nickname VARCHAR(100),
    avatar VARCHAR(500),
    email VARCHAR(100),
    phone VARCHAR(20),
    device_type VARCHAR(20),
    source_url TEXT,
    open_id VARCHAR(100),
    city VARCHAR(50),
    extra_info JSONB,
    merged_from_id BIGINT,  -- 合并来源用户ID（用于标记已被合并的用户）
    last_active_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(project_id, uid)
);

CREATE INDEX IF NOT EXISTS idx_users_project_phone ON users(project_id, phone);
CREATE INDEX IF NOT EXISTS idx_users_project_uid ON users(project_id, uid);
CREATE INDEX IF NOT EXISTS idx_users_project_external_uid ON users(project_id, external_uid);
CREATE INDEX IF NOT EXISTS idx_users_is_guest ON users(project_id, is_guest);

-- 用户标签定义表（按项目管理标签）
CREATE TABLE IF NOT EXISTS customer_tags (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id),
    name VARCHAR(50) NOT NULL,
    color VARCHAR(20) DEFAULT '#409EFF',  -- 标签颜色
    description VARCHAR(200),
    sort_order INTEGER DEFAULT 0,
    created_by BIGINT REFERENCES sys_users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(project_id, name)
);

CREATE INDEX IF NOT EXISTS idx_customer_tags_project ON customer_tags(project_id);

-- 用户标签关联表（多对多）
CREATE TABLE IF NOT EXISTS user_tag_relations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tag_id BIGINT NOT NULL REFERENCES customer_tags(id) ON DELETE CASCADE,
    tagged_by BIGINT REFERENCES sys_users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, tag_id)
);

CREATE INDEX IF NOT EXISTS idx_user_tag_relations_user ON user_tag_relations(user_id);
CREATE INDEX IF NOT EXISTS idx_user_tag_relations_tag ON user_tag_relations(tag_id);

-- 用户分组表（可选，用于批量管理用户）
CREATE TABLE IF NOT EXISTS customer_groups (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id),
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    is_system BOOLEAN DEFAULT FALSE,  -- 是否系统内置分组
    sort_order INTEGER DEFAULT 0,
    created_by BIGINT REFERENCES sys_users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(project_id, name)
);

CREATE INDEX IF NOT EXISTS idx_customer_groups_project ON customer_groups(project_id);

-- 用户分组关联表
CREATE TABLE IF NOT EXISTS user_group_relations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    group_id BIGINT NOT NULL REFERENCES customer_groups(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, group_id)
);

CREATE INDEX IF NOT EXISTS idx_user_group_relations_user ON user_group_relations(user_id);
CREATE INDEX IF NOT EXISTS idx_user_group_relations_group ON user_group_relations(group_id);

-- 旧版用户标签表（保留兼容，后续可删除）
CREATE TABLE IF NOT EXISTS user_tags (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    tag_name VARCHAR(50) NOT NULL,
    tagged_by BIGINT REFERENCES agents(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, tag_name)
);

CREATE INDEX IF NOT EXISTS idx_user_tags_project_tag ON user_tags(project_id, tag_name);

-- 用户备注表
CREATE TABLE IF NOT EXISTS user_remarks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    agent_id BIGINT NOT NULL REFERENCES agents(id),
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_remarks_user ON user_remarks(user_id);

-- 用户拉黑表
CREATE TABLE IF NOT EXISTS user_blocks (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    reason TEXT,
    blocked_by BIGINT REFERENCES agents(id),
    block_until TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_blocks_check ON user_blocks(user_id, block_until);

-- 会话表 (核心)
CREATE TABLE IF NOT EXISTS conversations (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    agent_id BIGINT REFERENCES agents(id),
    status VARCHAR(20) NOT NULL DEFAULT 'queued',
    priority INTEGER DEFAULT 0,
    score INTEGER,
    score_remark TEXT,
    last_message_content TEXT,
    last_message_time TIMESTAMP WITH TIME ZONE,
    started_at TIMESTAMP WITH TIME ZONE,
    ended_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_conv_project_status ON conversations(project_id, status);
CREATE INDEX IF NOT EXISTS idx_conv_agent_active ON conversations(agent_id, status) WHERE status = 'active';
CREATE INDEX IF NOT EXISTS idx_conv_user_history ON conversations(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_conv_updated ON conversations(project_id, last_message_time DESC);

-- 消息记录表
CREATE TABLE IF NOT EXISTS messages (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    conversation_id BIGINT NOT NULL REFERENCES conversations(id),
    msg_id VARCHAR(64) NOT NULL UNIQUE,
    sender_type VARCHAR(10) NOT NULL,
    sender_id BIGINT NOT NULL,
    msg_type VARCHAR(20) NOT NULL,
    content JSONB NOT NULL,
    is_revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_messages_conv_time ON messages(conversation_id, created_at);
CREATE INDEX IF NOT EXISTS idx_messages_msg_id ON messages(msg_id);

-- 多人协作/临时群聊表
CREATE TABLE IF NOT EXISTS group_chats (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id),
    name VARCHAR(100),
    owner_agent_id BIGINT NOT NULL REFERENCES agents(id),
    related_conversation_id BIGINT REFERENCES conversations(id),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 群聊成员表
CREATE TABLE IF NOT EXISTS group_chat_members (
    id BIGSERIAL PRIMARY KEY,
    group_chat_id BIGINT NOT NULL REFERENCES group_chats(id),
    member_type VARCHAR(10) NOT NULL,
    member_id BIGINT NOT NULL,
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(group_chat_id, member_type, member_id)
);

-- 知识库分类
CREATE TABLE IF NOT EXISTS kb_categories (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id),
    parent_id BIGINT REFERENCES kb_categories(id),
    name VARCHAR(100) NOT NULL,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 知识库文章
CREATE TABLE IF NOT EXISTS kb_articles (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id),
    category_id BIGINT REFERENCES kb_categories(id),
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    view_count INTEGER DEFAULT 0,
    hit_count INTEGER DEFAULT 0,
    is_published BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_kb_articles_project_title ON kb_articles(project_id, title);
CREATE INDEX IF NOT EXISTS idx_kb_articles_category ON kb_articles(category_id);

-- 工单表
CREATE TABLE IF NOT EXISTS tickets (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    creator_type VARCHAR(10) NOT NULL,
    assignee_id BIGINT REFERENCES agents(id),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    priority VARCHAR(10) DEFAULT 'medium',
    status VARCHAR(20) DEFAULT 'open',
    contact_info VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tickets_project_status ON tickets(project_id, status);
CREATE INDEX IF NOT EXISTS idx_tickets_user ON tickets(user_id);
CREATE INDEX IF NOT EXISTS idx_tickets_assignee ON tickets(assignee_id);

-- 工单流转记录
CREATE TABLE IF NOT EXISTS ticket_events (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL REFERENCES tickets(id),
    operator_type VARCHAR(10),
    operator_id BIGINT,
    action VARCHAR(50) NOT NULL,
    content TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ticket_events_ticket ON ticket_events(ticket_id, created_at);

-- ============================================================
-- 初始化示例数据
-- ============================================================

-- 插入示例项目
INSERT INTO projects (name, description, app_key, app_secret, config)
VALUES (
    '示例项目',
    '这是一个示例客服项目',
    'demo_app_key_001',
    'demo_app_secret_001',
    '{"welcomeMessage": "欢迎咨询，我们随时准备为您服务", "themeColor": "#1890FF"}'
) ON CONFLICT DO NOTHING;

-- 插入系统角色
INSERT INTO sys_roles (code, name, description, permissions, is_system) VALUES
('super_admin', '超级管理员', '拥有系统所有权限', '{"menus": ["dashboard", "workbench", "projects", "system", "users", "agents", "roles", "menus", "settings", "client", "customers", "customer-tags"], "actions": ["*"]}', TRUE),
('admin', '管理员', '项目管理员，可以管理客服和查看统计', '{"menus": ["dashboard", "projects", "knowledge", "system", "users", "agents", "roles", "menus", "settings", "client", "customers", "customer-tags"], "actions": ["dashboard:view", "project:manage", "kb:manage", "user:manage", "agent:manage", "role:manage", "menu:manage", "settings:manage", "customer:manage"]}', TRUE),
('agent', '客服', '普通客服，可以接待用户', '{"menus": ["workbench", "knowledge", "settings", "client", "customers", "customer-tags"], "actions": ["workbench", "conversation:handle", "kb:view", "kb:manage", "customer:view"]}', TRUE),
('viewer', '观察员', '只能查看不能操作', '{"menus": ["dashboard"], "actions": ["dashboard:view"]}', FALSE)
ON CONFLICT (code) DO NOTHING;

-- 插入系统菜单（一级菜单）
INSERT INTO sys_menus (code, name, type, parent_id, path, icon, sort_order, is_enabled, description) VALUES
('dashboard', '数据概览', 'menu', NULL, '/admin/dashboard', 'DataAnalysis', 1, TRUE, '系统数据统计和概览'),
('workbench', '客服工作台', 'menu', NULL, '/admin/workbench', 'ChatLineSquare', 2, TRUE, '客服接待工作界面'),
('projects', '项目管理', 'menu', NULL, '/admin/projects', 'Folder', 3, TRUE, '管理客服项目'),
('knowledge', '知识库管理', 'menu', NULL, '/admin/knowledge', 'Document', 4, TRUE, '管理知识库分类和文章'),
('client', '客户端', 'menu', NULL, NULL, 'UserFilled', 5, TRUE, '客户端用户管理'),
('system', '系统管理', 'menu', NULL, NULL, 'Setting', 6, TRUE, '系统管理功能'),
('settings', '系统设置', 'menu', NULL, '/admin/settings', 'Tools', 7, TRUE, '系统配置')
ON CONFLICT (code) DO NOTHING;

-- 插入客户端子菜单（二级菜单）
INSERT INTO sys_menus (code, name, type, parent_id, path, icon, sort_order, is_enabled, description) VALUES
('customers', '用户管理', 'menu', (SELECT id FROM sys_menus WHERE code = 'client'), '/admin/customers', 'User', 1, TRUE, '管理客户端用户'),
('customer-tags', '标签管理', 'menu', (SELECT id FROM sys_menus WHERE code = 'client'), '/admin/customer-tags', 'PriceTag', 2, TRUE, '管理客户标签')
ON CONFLICT (code) DO NOTHING;

-- 插入系统管理子菜单（二级菜单）
INSERT INTO sys_menus (code, name, type, parent_id, path, icon, sort_order, is_enabled, description) VALUES
('users', '用户管理', 'menu', (SELECT id FROM sys_menus WHERE code = 'system'), '/admin/users', 'User', 1, TRUE, '管理系统用户'),
('agents', '客服管理', 'menu', (SELECT id FROM sys_menus WHERE code = 'system'), '/admin/agents', 'Headset', 2, TRUE, '管理客服人员和项目分配'),
('roles', '角色管理', 'menu', (SELECT id FROM sys_menus WHERE code = 'system'), '/admin/roles', 'Key', 3, TRUE, '管理用户角色和权限'),
('menus', '菜单管理', 'menu', (SELECT id FROM sys_menus WHERE code = 'system'), '/admin/menus', 'Menu', 4, TRUE, '管理系统菜单和权限')
ON CONFLICT (code) DO NOTHING;

-- 插入操作权限（按钮级别）
INSERT INTO sys_menus (code, name, type, parent_id, path, icon, sort_order, is_enabled, description) VALUES
('dashboard:view', '查看数据', 'button', (SELECT id FROM sys_menus WHERE code = 'dashboard'), NULL, NULL, 1, TRUE, '查看数据概览'),
('project:manage', '项目管理', 'button', (SELECT id FROM sys_menus WHERE code = 'projects'), NULL, NULL, 1, TRUE, '创建、编辑、删除项目'),
('kb:manage', '知识库管理', 'button', (SELECT id FROM sys_menus WHERE code = 'knowledge'), NULL, NULL, 1, TRUE, '创建、编辑、删除知识库分类和文章'),
('user:manage', '用户管理', 'button', (SELECT id FROM sys_menus WHERE code = 'users'), NULL, NULL, 1, TRUE, '创建、编辑、删除用户'),
('agent:manage', '客服管理', 'button', (SELECT id FROM sys_menus WHERE code = 'agents'), NULL, NULL, 1, TRUE, '配置客服人员和项目分配'),
('role:manage', '角色管理', 'button', (SELECT id FROM sys_menus WHERE code = 'roles'), NULL, NULL, 1, TRUE, '创建、编辑、删除角色'),
('menu:manage', '菜单管理', 'button', (SELECT id FROM sys_menus WHERE code = 'menus'), NULL, NULL, 1, TRUE, '创建、编辑、删除菜单'),
('settings:manage', '设置管理', 'button', (SELECT id FROM sys_menus WHERE code = 'settings'), NULL, NULL, 1, TRUE, '修改系统设置'),
('conversation:handle', '处理会话', 'button', (SELECT id FROM sys_menus WHERE code = 'workbench'), NULL, NULL, 1, TRUE, '接待和处理用户会话'),
('kb:view', '查看知识库', 'button', (SELECT id FROM sys_menus WHERE code = 'workbench'), NULL, NULL, 2, TRUE, '查看知识库内容'),
('customer:manage', '客户管理', 'button', (SELECT id FROM sys_menus WHERE code = 'customers'), NULL, NULL, 1, TRUE, '管理客户信息和标签'),
('customer:view', '查看客户', 'button', (SELECT id FROM sys_menus WHERE code = 'customers'), NULL, NULL, 2, TRUE, '查看客户信息')
ON CONFLICT (code) DO NOTHING;

-- 插入系统用户（密码: admin123 的 BCrypt 哈希值）
-- 哈希值: $2a$10$66xl1rpOC9cUISk5FvnUs.NaX12oacR73tv4P6GtxGTgdBsFJeJNW
-- 注意：sys_users 表不包含 project_id，用户是全局的
INSERT INTO sys_users (username, password_hash, email, role_id, status) VALUES
(
    'admin',
    '$2a$10$66xl1rpOC9cUISk5FvnUs.NaX12oacR73tv4P6GtxGTgdBsFJeJNW',
    'admin@example.com',
    (SELECT id FROM sys_roles WHERE code = 'admin'),
    'active'
),
(
    'agent1',
    '$2a$10$66xl1rpOC9cUISk5FvnUs.NaX12oacR73tv4P6GtxGTgdBsFJeJNW',
    'agent1@example.com',
    (SELECT id FROM sys_roles WHERE code = 'agent'),
    'active'
),
(
    'agent2',
    '$2a$10$66xl1rpOC9cUISk5FvnUs.NaX12oacR73tv4P6GtxGTgdBsFJeJNW',
    'agent2@example.com',
    (SELECT id FROM sys_roles WHERE code = 'agent'),
    'active'
)
ON CONFLICT DO NOTHING;

-- 插入客服信息（关联到sys_users）
INSERT INTO agents (user_id, nickname, work_status, max_load, current_load) VALUES
(
    (SELECT id FROM sys_users WHERE username = 'agent1'),
    '客服1',
    'online',
    5,
    2
),
(
    (SELECT id FROM sys_users WHERE username = 'agent2'),
    '客服2',
    'offline',
    5,
    0
)
ON CONFLICT DO NOTHING;

-- 插入客服与项目的关联关系
INSERT INTO user_projects (user_id, project_id) VALUES
(
    (SELECT id FROM sys_users WHERE username = 'agent1'),
    1
),
(
    (SELECT id FROM sys_users WHERE username = 'agent2'),
    1
)
ON CONFLICT DO NOTHING;

-- 插入示例用户（访客）
INSERT INTO users (project_id, uid, nickname, phone, device_type, city)
VALUES (
    1,
    'user_001',
    '李先生',
    '13800000001',
    'mobile',
    '北京'
),
(
    1,
    'user_002',
    '王女士',
    '13800000002',
    'pc',
    '上海'
)
ON CONFLICT DO NOTHING;

-- 插入示例会话
INSERT INTO conversations (project_id, user_id, agent_id, status, priority, last_message_content, last_message_time)
VALUES (
    1,
    1,
    1,
    'active',
    0,
    '好的，我马上为您处理',
    CURRENT_TIMESTAMP
),
(
    1,
    2,
    NULL,
    'queued',
    0,
    '请问有什么可以帮助您的？',
    CURRENT_TIMESTAMP
),
(
    1,
    1,
    1,
    'active',
    0,
    '您好，请问有什么可以帮助您？',
    CURRENT_TIMESTAMP
)
ON CONFLICT DO NOTHING;

-- 插入示例消息
INSERT INTO messages (project_id, conversation_id, msg_id, sender_type, sender_id, msg_type, content) VALUES
(1, 1, 'msg_001', 'user', 1, 'text', '{"text": "你好，我想咨询一下订单问题"}'),
(1, 1, 'msg_002', 'agent', 1, 'text', '{"text": "您好！请问您的订单号是多少？"}'),
(1, 1, 'msg_003', 'user', 1, 'text', '{"text": "订单号是 ORD20260122001"}'),
(1, 1, 'msg_004', 'agent', 1, 'text', '{"text": "好的，我帮您查询一下，请稍等"}'),
(1, 1, 'msg_005', 'agent', 1, 'text', '{"text": "您的订单目前正在配送中，预计明天送达"}'),
(1, 1, 'msg_006', 'user', 1, 'text', '{"text": "好的，谢谢！"}'),
(1, 2, 'msg_007', 'user', 2, 'text', '{"text": "请问有什么可以帮助您的？"}'),
(1, 3, 'msg_008', 'user', 1, 'text', '{"text": "我还有个问题想问"}'),
(1, 3, 'msg_009', 'agent', 1, 'text', '{"text": "请说"}'),
(1, 3, 'msg_010', 'user', 1, 'text', '{"text": "如何申请退款？"}')
ON CONFLICT DO NOTHING;

-- 插入示例知识库分类
INSERT INTO kb_categories (project_id, name, sort_order)
VALUES (
    1,
    '账户相关',
    1
),
(
    1,
    '支付相关',
    2
),
(
    1,
    '订单与物流',
    3
)
ON CONFLICT DO NOTHING;

-- 插入示例知识库文章
INSERT INTO kb_articles (project_id, category_id, title, content, is_published)
VALUES (
    1,
    1,
    '如何注册账号？',
    '<p>点击右上角注册按钮，填写邮箱和密码即可完成注册...</p>',
    TRUE
),
(
    1,
    2,
    '支持哪些支付方式？',
    '<p>我们支持支付宝、微信支付、银行卡等多种支付方式...</p>',
    TRUE
),
(
    1,
    3,
    '如何查询订单？',
    '<p>登录后在"我的订单"页面可以查看所有订单状态...</p>',
    TRUE
)
ON CONFLICT DO NOTHING;