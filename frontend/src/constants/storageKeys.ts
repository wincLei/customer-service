/**
 * LocalStorage 键名常量
 * 统一管理所有 localStorage 的 key，避免散落在各处
 */
export const StorageKeys = {
  /** 管理端 auth token */
  AUTH_TOKEN: 'auth_token',
  /** 管理端用户信息 */
  USER_INFO: 'user_info',
  /** Portal 端 token */
  PORTAL_TOKEN: 'portal_token',
  /** H5 游客 UID 前缀（拼接 projectId） */
  GUEST_UID_PREFIX: 'mini_cs_guest_uid',
  /** H5 工单已读状态前缀 */
  TICKET_READ_PREFIX: 'mini_cs_ticket_read_',
  /** 客服端工单已读状态前缀 */
  AGENT_TICKET_READ_PREFIX: 'mini_cs_agent_ticket_read_',
  /** H5 最后访问时间前缀（拼接 projectId 和 userId） */
  H5_LAST_VISIT_PREFIX: 'h5chat_last_visit_',
} as const
