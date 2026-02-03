package com.customer_service.portal.security;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Portal 用户上下文
 * 用于在请求处理过程中传递用户信息
 */
@Data
@AllArgsConstructor
public class PortalUserContext {
    private Long userId;
    private Long projectId;
    private String uid;

    private static final ThreadLocal<PortalUserContext> contextHolder = new ThreadLocal<>();

    /**
     * 设置当前用户上下文
     */
    public static void setContext(PortalUserContext context) {
        contextHolder.set(context);
    }

    /**
     * 获取当前用户上下文
     */
    public static PortalUserContext getContext() {
        return contextHolder.get();
    }

    /**
     * 清除当前用户上下文
     */
    public static void clear() {
        contextHolder.remove();
    }

    /**
     * 获取当前用户 ID
     */
    public static Long getCurrentUserId() {
        PortalUserContext context = getContext();
        return context != null ? context.getUserId() : null;
    }

    /**
     * 获取当前项目 ID
     */
    public static Long getCurrentProjectId() {
        PortalUserContext context = getContext();
        return context != null ? context.getProjectId() : null;
    }

    /**
     * 获取当前用户 UID
     */
    public static String getCurrentUid() {
        PortalUserContext context = getContext();
        return context != null ? context.getUid() : null;
    }
}
