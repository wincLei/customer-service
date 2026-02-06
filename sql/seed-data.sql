-- ============================================================
-- 生产环境初始化数据脚本
-- 用于 JPA/Hibernate 已创建表结构后插入初始数据
-- 执行: docker exec -i mini-customer-service-postgres psql -U postgres -d customer_service < sql/seed-data.sql
-- ============================================================

-- 插入示例项目
INSERT INTO projects (id, name, description, app_key, app_secret, config, created_at, updated_at)
VALUES (
    1,
    '示例项目',
    '这是一个示例客服项目',
    'demo_app_key_001',
    'demo_app_secret_001',
    '{"welcomeMessage": "欢迎咨询，我们随时准备为您服务", "themeColor": "#1890FF"}',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO NOTHING;

-- 重置序列
SELECT setval('projects_id_seq', COALESCE((SELECT MAX(id) FROM projects), 1));

-- 插入系统角色
INSERT INTO sys_roles (id, code, name, description, permissions, is_system, created_at, updated_at) VALUES
(1, 'super_admin', '超级管理员', '拥有系统所有权限', '{"menus": ["dashboard", "workbench", "projects", "system", "users", "agents", "roles", "menus", "settings", "client", "customers", "customer-tags", "quick-replies", "tickets"], "actions": ["*"]}', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'admin', '管理员', '项目管理员，可以管理客服和查看统计', '{"menus": ["dashboard", "projects", "knowledge", "system", "users", "agents", "roles", "menus", "settings", "client", "customers", "customer-tags", "quick-replies", "tickets"], "actions": ["dashboard:view", "project:manage", "kb:manage", "user:manage", "agent:manage", "role:manage", "menu:manage", "settings:manage", "customer:manage", "ticket:manage"]}', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'agent', '客服', '普通客服，可以接待用户', '{"menus": ["workbench", "knowledge", "settings", "client", "customers", "customer-tags", "quick-replies", "tickets"], "actions": ["workbench", "conversation:handle", "kb:view", "kb:manage", "customer:view", "ticket:handle"]}', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'viewer', '观察员', '只能查看不能操作', '{"menus": ["dashboard"], "actions": ["dashboard:view"]}', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

SELECT setval('sys_roles_id_seq', COALESCE((SELECT MAX(id) FROM sys_roles), 1));

-- 插入系统菜单（一级菜单）
INSERT INTO sys_menus (id, code, name, type, parent_id, path, icon, sort_order, is_enabled, description, created_at, updated_at) VALUES
(1, 'dashboard', '数据概览', 'menu', NULL, '/admin/dashboard', 'DataAnalysis', 1, TRUE, '系统数据统计和概览', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'workbench', '客服工作台', 'menu', NULL, '/admin/workbench', 'ChatLineSquare', 2, TRUE, '客服接待工作界面', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'projects', '项目管理', 'menu', NULL, '/admin/projects', 'Folder', 3, TRUE, '管理客服项目', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'knowledge', '知识库管理', 'menu', NULL, '/admin/knowledge', 'Document', 4, TRUE, '管理知识库分类和文章', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'client', '客户端', 'menu', NULL, NULL, 'UserFilled', 5, TRUE, '客户端用户管理', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'tickets', '工单管理', 'menu', NULL, '/admin/tickets', 'Tickets', 6, TRUE, '管理用户提交的工单', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'system', '系统管理', 'menu', NULL, NULL, 'Setting', 7, TRUE, '系统管理功能', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 'settings', '系统设置', 'menu', NULL, '/admin/settings', 'Tools', 8, TRUE, '系统配置', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 插入客户端子菜单（二级菜单）
INSERT INTO sys_menus (id, code, name, type, parent_id, path, icon, sort_order, is_enabled, description, created_at, updated_at) VALUES
(9, 'customers', '用户管理', 'menu', 5, '/admin/customers', 'User', 1, TRUE, '管理客户端用户', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 'customer-tags', '标签管理', 'menu', 5, '/admin/customer-tags', 'PriceTag', 2, TRUE, '管理客户标签', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 'quick-replies', '快捷回复', 'menu', 5, '/admin/quick-replies', 'ChatLineRound', 3, TRUE, '管理快捷回复模板', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 插入系统管理子菜单（二级菜单）
INSERT INTO sys_menus (id, code, name, type, parent_id, path, icon, sort_order, is_enabled, description, created_at, updated_at) VALUES
(12, 'users', '用户管理', 'menu', 7, '/admin/users', 'User', 1, TRUE, '管理系统用户', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 'agents', '客服管理', 'menu', 7, '/admin/agents', 'Headset', 2, TRUE, '管理客服人员和项目分配', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 'roles', '角色管理', 'menu', 7, '/admin/roles', 'Key', 3, TRUE, '管理用户角色和权限', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 'menus', '菜单管理', 'menu', 7, '/admin/menus', 'Menu', 4, TRUE, '管理系统菜单和权限', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 插入操作权限（按钮级别）
INSERT INTO sys_menus (id, code, name, type, parent_id, path, icon, sort_order, is_enabled, description, created_at, updated_at) VALUES
(16, 'dashboard:view', '查看数据', 'button', 1, NULL, NULL, 1, TRUE, '查看数据概览', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(17, 'project:manage', '项目管理', 'button', 3, NULL, NULL, 1, TRUE, '创建、编辑、删除项目', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, 'kb:manage', '知识库管理', 'button', 4, NULL, NULL, 1, TRUE, '创建、编辑、删除知识库分类和文章', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(19, 'user:manage', '用户管理', 'button', 12, NULL, NULL, 1, TRUE, '创建、编辑、删除用户', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20, 'agent:manage', '客服管理', 'button', 13, NULL, NULL, 1, TRUE, '配置客服人员和项目分配', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21, 'role:manage', '角色管理', 'button', 14, NULL, NULL, 1, TRUE, '创建、编辑、删除角色', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(22, 'menu:manage', '菜单管理', 'button', 15, NULL, NULL, 1, TRUE, '创建、编辑、删除菜单', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(23, 'settings:manage', '设置管理', 'button', 8, NULL, NULL, 1, TRUE, '修改系统设置', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(24, 'conversation:handle', '处理会话', 'button', 2, NULL, NULL, 1, TRUE, '接待和处理用户会话', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(25, 'kb:view', '查看知识库', 'button', 2, NULL, NULL, 2, TRUE, '查看知识库内容', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(26, 'customer:manage', '客户管理', 'button', 9, NULL, NULL, 1, TRUE, '管理客户信息和标签', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(27, 'customer:view', '查看客户', 'button', 9, NULL, NULL, 2, TRUE, '查看客户信息', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

SELECT setval('sys_menus_id_seq', COALESCE((SELECT MAX(id) FROM sys_menus), 1));

-- 插入系统用户（密码: admin123 的 BCrypt 哈希值）
-- 哈希值: $2a$10$66xl1rpOC9cUISk5FvnUs.NaX12oacR73tv4P6GtxGTgdBsFJeJNW
INSERT INTO sys_users (id, username, password_hash, email, role_id, status, created_at, updated_at) VALUES
(1, 'admin', '$2a$10$66xl1rpOC9cUISk5FvnUs.NaX12oacR73tv4P6GtxGTgdBsFJeJNW', 'admin@example.com', 2, 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'agent1', '$2a$10$66xl1rpOC9cUISk5FvnUs.NaX12oacR73tv4P6GtxGTgdBsFJeJNW', 'agent1@example.com', 3, 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'agent2', '$2a$10$66xl1rpOC9cUISk5FvnUs.NaX12oacR73tv4P6GtxGTgdBsFJeJNW', 'agent2@example.com', 3, 'active', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

SELECT setval('sys_users_id_seq', COALESCE((SELECT MAX(id) FROM sys_users), 1));

-- 插入客服信息（关联到sys_users）
INSERT INTO agents (id, user_id, nickname, work_status, max_load, current_load, created_at, updated_at) VALUES
(1, 2, '客服1', 'online', 5, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 3, '客服2', 'offline', 5, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

SELECT setval('agents_id_seq', COALESCE((SELECT MAX(id) FROM agents), 1));

-- 插入客服与项目的关联关系
INSERT INTO user_projects (id, user_id, project_id, created_at) VALUES
(1, 2, 1, CURRENT_TIMESTAMP),
(2, 3, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

SELECT setval('user_projects_id_seq', COALESCE((SELECT MAX(id) FROM user_projects), 1));

-- 插入示例用户（访客）
INSERT INTO users (id, project_id, uid, nickname, phone, device_type, city, created_at, updated_at) VALUES
(1, 1, 'user_001', '李先生', '13800000001', 'mobile', '北京', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 'user_002', '王女士', '13800000002', 'pc', '上海', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

SELECT setval('users_id_seq', COALESCE((SELECT MAX(id) FROM users), 1));

-- 插入示例会话
INSERT INTO conversations (id, project_id, user_id, agent_id, status, priority, last_message_content, last_message_time, created_at, updated_at) VALUES
(1, 1, 1, 1, 'active', 0, '好的，我马上为您处理', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 2, NULL, 'queued', 0, '请问有什么可以帮助您的？', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, 1, 1, 'active', 0, '您好，请问有什么可以帮助您？', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

SELECT setval('conversations_id_seq', COALESCE((SELECT MAX(id) FROM conversations), 1));

-- 插入示例消息
INSERT INTO messages (id, project_id, conversation_id, msg_id, sender_type, sender_id, msg_type, content, created_at) VALUES
(1, 1, 1, 'msg_001', 'user', 1, 'text', '{"text": "你好，我想咨询一下订单问题"}', CURRENT_TIMESTAMP),
(2, 1, 1, 'msg_002', 'agent', 1, 'text', '{"text": "您好！请问您的订单号是多少？"}', CURRENT_TIMESTAMP),
(3, 1, 1, 'msg_003', 'user', 1, 'text', '{"text": "订单号是 ORD20260122001"}', CURRENT_TIMESTAMP),
(4, 1, 1, 'msg_004', 'agent', 1, 'text', '{"text": "好的，我帮您查询一下，请稍等"}', CURRENT_TIMESTAMP),
(5, 1, 1, 'msg_005', 'agent', 1, 'text', '{"text": "您的订单目前正在配送中，预计明天送达"}', CURRENT_TIMESTAMP),
(6, 1, 1, 'msg_006', 'user', 1, 'text', '{"text": "好的，谢谢！"}', CURRENT_TIMESTAMP),
(7, 1, 2, 'msg_007', 'user', 2, 'text', '{"text": "请问有什么可以帮助您的？"}', CURRENT_TIMESTAMP),
(8, 1, 3, 'msg_008', 'user', 1, 'text', '{"text": "我还有个问题想问"}', CURRENT_TIMESTAMP),
(9, 1, 3, 'msg_009', 'agent', 1, 'text', '{"text": "请说"}', CURRENT_TIMESTAMP),
(10, 1, 3, 'msg_010', 'user', 1, 'text', '{"text": "如何申请退款？"}', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

SELECT setval('messages_id_seq', COALESCE((SELECT MAX(id) FROM messages), 1));

-- 插入示例知识库分类
INSERT INTO kb_categories (id, project_id, name, sort_order, created_at, updated_at) VALUES
(1, 1, '账户相关', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, '支付相关', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, '订单与物流', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

SELECT setval('kb_categories_id_seq', COALESCE((SELECT MAX(id) FROM kb_categories), 1));

-- 插入示例知识库文章
INSERT INTO kb_articles (id, project_id, category_id, title, content, is_published, created_at, updated_at) VALUES
(1, 1, 1, '如何注册账号？', '<p>点击右上角注册按钮，填写邮箱和密码即可完成注册...</p>', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 2, '支持哪些支付方式？', '<p>我们支持支付宝、微信支付、银行卡等多种支付方式...</p>', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, 3, '如何查询订单？', '<p>登录后在"我的订单"页面可以查看所有订单状态...</p>', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

SELECT setval('kb_articles_id_seq', COALESCE((SELECT MAX(id) FROM kb_articles), 1));

-- 插入示例快捷回复
INSERT INTO quick_replies (id, project_id, title, content, category, created_at, updated_at) VALUES
(1, 1, '问候语', '您好，很高兴为您服务，请问有什么可以帮助您的？', '常用', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, '稍等', '好的，请您稍等，我马上为您查询。', '常用', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, '感谢', '感谢您的耐心等待，您的问题已经处理完毕。', '常用', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 1, '结束语', '感谢您的咨询，如有其他问题随时联系我们，祝您生活愉快！', '常用', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 1, '退款政策', '您好，关于退款问题，我们支持7天无理由退款，请您提供订单号以便我们为您处理。', '售后', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 1, '配送时间', '您好，我们的标准配送时间为3-5个工作日，偏远地区可能需要额外1-2天。', '物流', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

SELECT setval('quick_replies_id_seq', COALESCE((SELECT MAX(id) FROM quick_replies), 1));

-- ============================================================
-- 完成
-- ============================================================
DO $$
BEGIN
    RAISE NOTICE '初始化数据插入完成！';
    RAISE NOTICE '默认管理员账号: admin / admin123';
    RAISE NOTICE '默认客服账号: agent1 / admin123, agent2 / admin123';
END $$;
