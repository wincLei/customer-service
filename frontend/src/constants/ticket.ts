/**
 * 工单状态常量
 */
export const TicketStatus = {
  OPEN: 'open',
  PENDING: 'pending',
  PROCESSING: 'processing',
  RESOLVED: 'resolved',
  CLOSED: 'closed',
} as const

/**
 * 工单优先级常量
 */
export const TicketPriority = {
  LOW: 'low',
  MEDIUM: 'medium',
  HIGH: 'high',
  URGENT: 'urgent',
} as const

/**
 * 工单状态标签映射
 */
export const TicketStatusLabel: Record<string, string> = {
  [TicketStatus.OPEN]: '待处理',
  [TicketStatus.PENDING]: '待处理',
  [TicketStatus.PROCESSING]: '处理中',
  [TicketStatus.RESOLVED]: '已解决',
  [TicketStatus.CLOSED]: '已关闭',
}

/**
 * 工单状态对应 Element Plus tag 类型
 */
export const TicketStatusType: Record<string, string> = {
  [TicketStatus.OPEN]: 'warning',
  [TicketStatus.PENDING]: 'warning',
  [TicketStatus.PROCESSING]: 'primary',
  [TicketStatus.RESOLVED]: 'success',
  [TicketStatus.CLOSED]: 'info',
}

/**
 * 工单优先级标签映射
 */
export const TicketPriorityLabel: Record<string, string> = {
  [TicketPriority.LOW]: '低',
  [TicketPriority.MEDIUM]: '中',
  [TicketPriority.HIGH]: '高',
  [TicketPriority.URGENT]: '紧急',
}

/**
 * 工单优先级对应 Element Plus tag 类型
 */
export const TicketPriorityType: Record<string, string> = {
  [TicketPriority.LOW]: 'info',
  [TicketPriority.MEDIUM]: '',
  [TicketPriority.HIGH]: 'warning',
  [TicketPriority.URGENT]: 'danger',
}
