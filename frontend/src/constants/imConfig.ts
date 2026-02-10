/**
 * IM 配置常量
 * WuKongIM 相关的消息类型、分页限制等
 */
export const IMPayloadType = {
  /** 文本消息 */
  TEXT: 1,
  /** 图片消息 */
  IMAGE: 2,
  /** 文件消息 */
  FILE: 3,
} as const

/** IM 消息发送成功状态码 */
export const IM_SEND_SUCCESS_CODE = 0

/** IM 首次加载消息数 */
export const IM_INITIAL_LOAD_LIMIT = 50

/** IM 加载更多消息数 */
export const IM_LOAD_MORE_LIMIT = 30

/** 客服 UID 前缀 */
export const AGENT_UID_PREFIX = 'agent_'
