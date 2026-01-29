/**
 * WuKongIM 设备类型常量
 * 用于 IM 连接时区分不同的客户端类型
 * 前端获取 token 和连接 SDK 时必须使用相同的 deviceFlag
 */
export const DeviceType = {
  /** Web 端（客服工作台） */
  WEB: 1,
  /** H5 移动端（用户聊天） */
  H5: 2
} as const

export type DeviceTypeValue = typeof DeviceType[keyof typeof DeviceType]
