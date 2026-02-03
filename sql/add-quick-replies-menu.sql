-- ============================================================
-- 添加"快捷回复"菜单到客户端子菜单
-- ============================================================

-- 插入快捷回复菜单（二级菜单，在客户端下）
INSERT INTO sys_menus (code, name, type, parent_id, path, icon, sort_order, is_enabled, description) VALUES
('quick-replies', '快捷回复', 'menu', (SELECT id FROM sys_menus WHERE code = 'client'), '/admin/quick-replies', 'ChatLineRound', 3, TRUE, '管理快捷回复模板')
ON CONFLICT (code) DO NOTHING;

-- 更新 admin 角色权限，添加 quick-replies 菜单
UPDATE sys_roles 
SET permissions = jsonb_set(
    permissions,
    '{menus}',
    (
        SELECT jsonb_agg(DISTINCT value)
        FROM (
            SELECT jsonb_array_elements_text(permissions->'menus') as value
            FROM sys_roles WHERE code = 'admin'
            UNION
            SELECT 'quick-replies'
        ) t
    )
)
WHERE code = 'admin';

-- 更新 agent 角色权限，添加 quick-replies 菜单
UPDATE sys_roles 
SET permissions = jsonb_set(
    permissions,
    '{menus}',
    (
        SELECT jsonb_agg(DISTINCT value)
        FROM (
            SELECT jsonb_array_elements_text(permissions->'menus') as value
            FROM sys_roles WHERE code = 'agent'
            UNION
            SELECT 'quick-replies'
        ) t
    )
)
WHERE code = 'agent';
