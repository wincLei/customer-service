package com.customer_service.shared.constant;

/**
 * 工单操作类型常量
 */
public final class TicketAction {

    private TicketAction() {
    }

    public static final String CREATE = "create";
    public static final String REPLY = "reply";
    public static final String CLOSE = "close";
    public static final String ASSIGN = "assign";
    public static final String STATUS_CHANGE = "status_change";
    public static final String AUTO_CLOSE = "auto_close";
    public static final String TRANSFER = "transfer";
}
