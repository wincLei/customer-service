package com.customer_service.shared.context;

import com.customer_service.shared.dto.UserContext;

/**
 * 用户上下文持有者
 * 使用ThreadLocal存储当前请求的用户信息
 */
public class UserContextHolder {

    private static final ThreadLocal<UserContext> CONTEXT = new ThreadLocal<>();

    /**
     * 设置当前用户上下文
     */
    public static void setContext(UserContext context) {
        CONTEXT.set(context);
    }

    /**
     * 获取当前用户上下文
     */
    public static UserContext getContext() {
        return CONTEXT.get();
    }

    /**
     * 清除当前用户上下文
     */
    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        UserContext context = getContext();
        return context != null ? context.getUserId() : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        UserContext context = getContext();
        return context != null ? context.getUsername() : null;
    }

    /**
     * 获取当前用户角色
     */
    public static String getRoleCode() {
        UserContext context = getContext();
        return context != null ? context.getRoleCode() : null;
    }
}
