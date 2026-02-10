package com.customer_service.shared.constant;

/**
 * 应用默认值常量
 */
public final class AppDefaults {

    private AppDefaults() {
    }

    /** 工单自动关闭超时时间（小时） */
    public static final int AUTO_CLOSE_TIMEOUT_HOURS = 48;

    /** 客服默认最大接待量 */
    public static final int DEFAULT_MAX_LOAD = 5;

    /** Token 过期时间（小时） */
    public static final int TOKEN_EXPIRE_HOURS = 24;

    /** 验证码过期时间（分钟） */
    public static final int CAPTCHA_EXPIRE_MINUTES = 5;

    /** 默认优先级 */
    public static final int DEFAULT_PRIORITY = 0;

    /** Redis key 前缀 - Token */
    public static final String REDIS_TOKEN_PREFIX = "token:";

    /** Redis key 前缀 - 验证码 */
    public static final String REDIS_CAPTCHA_PREFIX = "captcha:";

    /** 默认欢迎语 */
    public static final String DEFAULT_WELCOME_MESSAGE = "您好，请问有什么需要帮助的？";

    /** Authorization Header 前缀 */
    public static final String BEARER_PREFIX = "Bearer ";
}
