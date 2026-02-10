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
  [TicketStatus.OPEN]: 'ticket.status.open',
  [TicketStatus.PENDING]: 'ticket.status.pending',
  [TicketStatus.PROCESSING]: 'ticket.status.processing',
  [TicketStatus.RESOLVED]: 'ticket.status.resolved',
  [TicketStatus.CLOSED]: 'ticket.status.closed',
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
  [TicketPriority.LOW]: 'ticket.priority.low',
  [TicketPriority.MEDIUM]: 'ticket.priority.medium',
  [TicketPriority.HIGH]: 'ticket.priority.high',
  [TicketPriority.URGENT]: 'ticket.priority.urgent',
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
