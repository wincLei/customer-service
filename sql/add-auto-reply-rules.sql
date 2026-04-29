-- 自动回复规则表
CREATE TABLE IF NOT EXISTS auto_reply_rules (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    rule_name VARCHAR(100) NOT NULL,
    keywords VARCHAR(500) NOT NULL,
    reply_content TEXT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    priority INT NOT NULL DEFAULT 100,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_auto_reply_rules_project ON auto_reply_rules(project_id);
CREATE INDEX IF NOT EXISTS idx_auto_reply_rules_enabled ON auto_reply_rules(project_id, enabled);

-- 示例规则
INSERT INTO auto_reply_rules (project_id, rule_name, keywords, reply_content, enabled, priority)
VALUES
(1, '提现问题', '提现,到账,转账,打款', '您好，提现通常在1-3个工作日内到账。如超过3个工作日未到账，请提供提现记录截图联系客服处理。', TRUE, 10),
(1, '注册登录问题', '注册,账号,登录,密码', '您好，注册时请确保手机号正确，验证码有效期为5分钟。如遇登录问题，可通过手机号找回密码。', TRUE, 20),
(1, '退款问题', '退款,退钱,退回', '您好，退款申请审核通过后将在3-5个工作日内原路退回。如有疑问请提供订单号，客服会尽快处理。', TRUE, 30)
ON CONFLICT DO NOTHING;
