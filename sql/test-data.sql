-- 插入测试项目
INSERT INTO projects (id, name, app_key, app_secret, created_at)
VALUES (1, '测试项目', 'test_app_key', 'test_app_secret', NOW())
ON CONFLICT (id) DO NOTHING;

-- 插入测试客服 (使用BCrypt加密的密码hash: admin123)
INSERT INTO agents (id, project_id, username, password_hash, nickname, status, created_at)
VALUES 
    (1, 1, 'agent1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHCNCMsLR7S5j7UY7v2zvG', '客服小李', 'online', NOW()),
    (2, 1, 'agent2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHCNCMsLR7S5j7UY7v2zvG', '客服小王', 'online', NOW())
ON CONFLICT (id) DO NOTHING;

-- 插入测试用户 (不包含is_blocked字段)
INSERT INTO users (id, project_id, uid, nickname, avatar, phone, email, created_at)
VALUES 
    (1, 1, 'user_001', '张三', 'https://api.dicebear.com/7.x/avataaars/svg?seed=1', '13800138001', 'zhang@test.com', NOW()),
    (2, 1, 'user_002', '李四', 'https://api.dicebear.com/7.x/avataaars/svg?seed=2', '13800138002', 'li@test.com', NOW()),
    (3, 1, 'user_003', '王五', 'https://api.dicebear.com/7.x/avataaars/svg?seed=3', '13800138003', 'wang@test.com', NOW())
ON CONFLICT (id) DO NOTHING;

-- 插入测试会话（排队中）
INSERT INTO conversations (id, project_id, user_id, agent_id, status, priority, last_message_time, created_at)
VALUES 
    (1, 1, 1, NULL, 'queued', 1, NOW() - INTERVAL '2 minutes', NOW() - INTERVAL '5 minutes'),
    (2, 1, 2, NULL, 'queued', 1, NOW() - INTERVAL '1 minute', NOW() - INTERVAL '3 minutes')
ON CONFLICT (id) DO NOTHING;

-- 插入测试会话（进行中）
INSERT INTO conversations (id, project_id, user_id, agent_id, status, priority, last_message_time, last_message_content, created_at)
VALUES 
    (3, 1, 3, 1, 'active', 1, NOW(), '我给您详细介绍一下...', NOW() - INTERVAL '10 minutes')
ON CONFLICT (id) DO NOTHING;

-- 插入测试消息 (使用msg_type和jsonb content)
INSERT INTO messages (id, project_id, conversation_id, msg_id, sender_type, sender_id, msg_type, content, created_at)
VALUES 
    -- 会话1的消息
    (1, 1, 1, 'msg_001', 'user', 1, 'text', '{"text": "你好，我想咨询一下产品问题", "type": "text"}'::jsonb, NOW() - INTERVAL '5 minutes'),
    (2, 1, 1, 'msg_002', 'user', 1, 'text', '{"text": "能帮我看看吗？", "type": "text"}'::jsonb, NOW() - INTERVAL '2 minutes'),
    
    -- 会话2的消息
    (3, 1, 2, 'msg_003', 'user', 2, 'text', '{"text": "我的订单有问题", "type": "text"}'::jsonb, NOW() - INTERVAL '3 minutes'),
    (4, 1, 2, 'msg_004', 'user', 2, 'text', '{"text": "为什么还没发货？", "type": "text"}'::jsonb, NOW() - INTERVAL '1 minute'),
    
    -- 会话3的消息（有客服回复）
    (5, 1, 3, 'msg_005', 'user', 3, 'text', '{"text": "你好", "type": "text"}'::jsonb, NOW() - INTERVAL '10 minutes'),
    (6, 1, 3, 'msg_006', 'agent', 1, 'text', '{"text": "您好，请问有什么可以帮您？", "type": "text"}'::jsonb, NOW() - INTERVAL '9 minutes'),
    (7, 1, 3, 'msg_007', 'user', 3, 'text', '{"text": "我想了解一下贵公司的产品", "type": "text"}'::jsonb, NOW() - INTERVAL '8 minutes'),
    (8, 1, 3, 'msg_008', 'agent', 1, 'text', '{"text": "当然可以，我们有多款产品供您选择，您具体想了解哪方面呢？", "type": "text"}'::jsonb, NOW() - INTERVAL '7 minutes'),
    (9, 1, 3, 'msg_009', 'user', 3, 'text', '{"text": "价格和功能", "type": "text"}'::jsonb, NOW() - INTERVAL '5 minutes'),
    (10, 1, 3, 'msg_010', 'agent', 1, 'text', '{"text": "好的，我给您详细介绍一下...", "type": "text"}'::jsonb, NOW() - INTERVAL '4 minutes')
ON CONFLICT (id) DO NOTHING;

-- 插入知识库分类
INSERT INTO kb_categories (id, project_id, parent_id, name, sort_order, created_at)
VALUES 
    (1, 1, NULL, '常见问题', 1, NOW()),
    (2, 1, NULL, '产品介绍', 2, NOW()),
    (3, 1, 1, '订单问题', 1, NOW()),
    (4, 1, 1, '售后服务', 2, NOW())
ON CONFLICT (id) DO NOTHING;

-- 插入知识库文章
INSERT INTO kb_articles (id, project_id, category_id, title, content, view_count, hit_count, is_published, created_at)
VALUES 
    (1, 1, 3, '如何查询订单状态？', 
     '您可以通过以下方式查询订单状态：\n1. 登录账号后进入"我的订单"\n2. 输入订单号进行查询\n3. 联系客服人工查询', 
     150, 20, true, NOW()),
    (2, 1, 3, '订单可以取消吗？', 
     '订单在未发货前可以取消。已发货的订单需要等待收货后申请退货。', 
     200, 35, true, NOW()),
    (3, 1, 4, '如何申请退货退款？', 
     '申请退货退款流程：\n1. 进入订单详情页\n2. 点击"申请退货"\n3. 选择退货原因\n4. 等待审核\n5. 审核通过后寄回商品', 
     300, 50, true, NOW())
ON CONFLICT (id) DO NOTHING;

-- 插入快捷回复
INSERT INTO quick_replies (id, project_id, category, content, creator_id, created_at)
VALUES 
    (1, 1, '问候语', '您好，我是客服小李，请问有什么可以帮您？', NULL, NOW()),
    (2, 1, '问候语', '您好，很高兴为您服务！', NULL, NOW()),
    (3, 1, '订单查询', '请提供您的订单号，我帮您查询一下', 1, NOW()),
    (4, 1, '结束语', '感谢您的咨询，祝您生活愉快！', NULL, NOW())
ON CONFLICT (id) DO NOTHING;

-- 插入用户标签
INSERT INTO user_tags (id, project_id, user_id, tag_name, tagged_by, created_at)
VALUES 
    (1, 1, 1, 'VIP客户', 1, NOW()),
    (2, 1, 1, '优质客户', 1, NOW()),
    (3, 1, 2, '投诉客户', 1, NOW())
ON CONFLICT (id) DO NOTHING;

-- 更新序列（PostgreSQL）
SELECT setval('projects_id_seq', COALESCE((SELECT MAX(id) FROM projects), 1));
SELECT setval('agents_id_seq', COALESCE((SELECT MAX(id) FROM agents), 1));
SELECT setval('users_id_seq', COALESCE((SELECT MAX(id) FROM users), 1));
SELECT setval('conversations_id_seq', COALESCE((SELECT MAX(id) FROM conversations), 1));
SELECT setval('messages_id_seq', COALESCE((SELECT MAX(id) FROM messages), 1));
SELECT setval('kb_categories_id_seq', COALESCE((SELECT MAX(id) FROM kb_categories), 1));
SELECT setval('kb_articles_id_seq', COALESCE((SELECT MAX(id) FROM kb_articles), 1));
SELECT setval('quick_replies_id_seq', COALESCE((SELECT MAX(id) FROM quick_replies), 1));
SELECT setval('user_tags_id_seq', COALESCE((SELECT MAX(id) FROM user_tags), 1));

