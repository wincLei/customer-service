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
import i18n from '@/locales'

export function getChannelTypeName(channelType: number): string {
  const { t } = i18n.global
  switch (channelType) {
    case WKChannelType.PERSONAL:
      return t('channel.personal')
    case WKChannelType.GROUP:
      return t('channel.group')
    case WKChannelType.CUSTOMER_SERVICE:
      return t('channel.service')
    case WKChannelType.COMMUNITY_TOPIC:
      return t('channel.communityTopic')
    case WKChannelType.COMMUNITY:
      return t('channel.community')
    case WKChannelType.DATA:
      return t('channel.data')
    case WKChannelType.VISITOR:
      return t('channel.visitor')
    default:
      return t('channel.unknown')
  }
}
