package com.customer_service.shared.constant;

/**
 * WuKongIM Webhook 事件类型常量
 */
public final class WebhookEvent {

    private WebhookEvent() {
    }

    public static final String MSG_NOTIFY = "msg.notify";
    public static final String MSG_OFFLINE = "msg.offline";
    public static final String USER_ONLINE_STATUS = "user.onlinestatus";
}
