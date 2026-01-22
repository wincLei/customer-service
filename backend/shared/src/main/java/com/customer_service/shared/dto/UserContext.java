package com.customer_service.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户上下文信息
 * 用于在请求中传递当前登录用户信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {

    private Long userId;
    private String username;
    private String roleCode;
    private String roleName;
    private List<String> permissions;
    private String token;

    /**
     * 检查是否有指定权限
     */
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }

    /**
     * 检查是否有任意一个指定权限
     */
    public boolean hasAnyPermission(String... perms) {
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }
        for (String perm : perms) {
            if (permissions.contains(perm)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否是指定角色
     */
    public boolean hasRole(String role) {
        return roleCode != null && roleCode.equals(role);
    }

    /**
     * 检查是否有任意一个指定角色
     */
    public boolean hasAnyRole(String... roles) {
        if (roleCode == null) {
            return false;
        }
        for (String role : roles) {
            if (roleCode.equals(role)) {
                return true;
            }
        }
        return false;
    }
}
