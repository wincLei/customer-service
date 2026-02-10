/**
 * 消息发送相关常量
 */

/** 单条消息最大字数 */
export const MAX_CHARS_PER_MESSAGE = 200

/** 每分钟最大总字数 */
export const MAX_CHARS_PER_MINUTE = 1000

/** 频率限制时间窗口（毫秒） - 1分钟 */
export const RATE_LIMIT_WINDOW = 60 * 1000

/** 欢迎语展示间隔（毫秒） - 5分钟 */
export const WELCOME_INTERVAL = 5 * 60 * 1000

/** 滚动加载触发阈值（像素） */
export const SCROLL_THRESHOLD = 50

/** API 请求超时时间（毫秒） */
export const API_TIMEOUT = 10000

/** 默认分页大小 */
export const DEFAULT_PAGE_SIZE = 10

/** 工作台会话分页大小 */
export const WORKBENCH_PAGE_SIZE = 20
