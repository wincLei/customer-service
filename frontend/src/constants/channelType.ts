/**
 * WuKongIM 频道类型常量
 * 用于 IM 通信时区分不同的频道类型
 * 前后端必须保持一致
 */
export const WKChannelType = {
  /** 个人频道 - 用于一对一私聊 */
  PERSONAL: 1,
  /** 群组频道 - 用于群聊 */
  GROUP: 2,
  /** 客服频道 - 已废弃，使用 VISITOR 替代 */
  CUSTOMER_SERVICE: 3,
  /** 社区话题频道 */
  COMMUNITY_TOPIC: 4,
  /** 社区频道 */
  COMMUNITY: 5,
  /** 数据频道 - 用于系统数据推送 */
  DATA: 6,
  /** 访客频道 - 用于客服场景，支持一个访客订阅者 + 多个客服订阅者 */
  VISITOR: 10
} as const

export type WKChannelTypeValue = typeof WKChannelType[keyof typeof WKChannelType]

/**
 * 获取频道类型名称
 */
export function getChannelTypeName(channelType: number): string {
  switch (channelType) {
    case WKChannelType.PERSONAL:
      return '个人频道'
    case WKChannelType.GROUP:
      return '群组频道'
    case WKChannelType.CUSTOMER_SERVICE:
      return '客服频道(已废弃)'
    case WKChannelType.COMMUNITY_TOPIC:
      return '社区话题频道'
    case WKChannelType.COMMUNITY:
      return '社区频道'
    case WKChannelType.DATA:
      return '数据频道'
    case WKChannelType.VISITOR:
      return '访客频道'
    default:
      return '未知频道'
  }
}
