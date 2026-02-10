package com.customer_service.shared.constant;

/**
 * IM 相关常量
 */
public final class IMConstants {

    private IMConstants() {
    }

    /** 客服 UID 前缀 */
    public static final String AGENT_UID_PREFIX = "agent_";

    /** 匿名用户 UID 前缀 */
    public static final String ANON_UID_PREFIX = "anon_";

    /** 消息同步默认数量 */
    public static final int DEFAULT_SYNC_LIMIT = 50;
}
