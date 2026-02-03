package com.customer_service.admin.interceptor;

import com.customer_service.shared.annotation.Public;
import com.customer_service.shared.annotation.RequirePermission;
import com.customer_service.shared.context.UserContextHolder;
import com.customer_service.shared.dto.ApiResponse;
import com.customer_service.shared.dto.UserContext;
import com.customer_service.shared.entity.SysRole;
import com.customer_service.shared.entity.SysUser;
import com.customer_service.shared.repository.SysRoleRepository;
import com.customer_service.shared.repository.SysUserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 权限拦截器
 * 用于验证用户登录状态和权限
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;
    private final SysUserRepository sysUserRepository;
    private final SysRoleRepository sysRoleRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // 放行OPTIONS请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 如果不是方法处理器，放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 检查是否有@Public注解（类级别或方法级别）
        if (isPublicEndpoint(handlerMethod)) {
            return true;
        }

        // 获取token
        String token = extractToken(request);
        if (token == null || token.isEmpty()) {
            sendUnauthorized(response, "未登录或登录已过期");
            return false;
        }

        // 从Redis获取用户信息
        String userInfoJson = redisTemplate.opsForValue().get("token:" + token);
        if (userInfoJson == null) {
            sendUnauthorized(response, "登录已过期，请重新登录");
            return false;
        }

        try {
            // 解析用户信息
            Map<String, Object> userInfo = objectMapper.readValue(userInfoJson,
                    new TypeReference<Map<String, Object>>() {
                    });

            Long userId = Long.valueOf(userInfo.get("userId").toString());
            String username = (String) userInfo.get("username");
            String roleCode = (String) userInfo.get("roleCode");

            // 获取角色权限
            List<String> permissions = getPermissions(roleCode);

            // 获取项目ID列表
            List<Long> projectIds = null;
            if (userInfo.get("projectIds") != null) {
                @SuppressWarnings("unchecked")
                List<Number> rawProjectIds = (List<Number>) userInfo.get("projectIds");
                projectIds = rawProjectIds.stream().map(Number::longValue).toList();
            }

            // 构建用户上下文
            UserContext context = UserContext.builder()
                    .userId(userId)
                    .username(username)
                    .roleCode(roleCode)
                    .permissions(permissions)
                    .projectIds(projectIds)
                    .token(token)
                    .build();

            UserContextHolder.setContext(context);

            // 检查权限
            RequirePermission requirePermission = getRequirePermission(handlerMethod);
            if (requirePermission != null) {
                if (!checkPermission(context, requirePermission)) {
                    sendForbidden(response, "无权访问此资源");
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            log.error("解析用户信息失败", e);
            sendUnauthorized(response, "登录信息无效");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) {
        // 清理用户上下文
        UserContextHolder.clear();
    }

    /**
     * 检查是否是公开接口
     */
    private boolean isPublicEndpoint(HandlerMethod handlerMethod) {
        // 检查方法级别
        if (handlerMethod.hasMethodAnnotation(Public.class)) {
            return true;
        }
        // 检查类级别
        return handlerMethod.getBeanType().isAnnotationPresent(Public.class);
    }

    /**
     * 获取权限注解
     */
    private RequirePermission getRequirePermission(HandlerMethod handlerMethod) {
        // 优先方法级别
        RequirePermission methodAnnotation = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        // 然后类级别
        return handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
    }

    /**
     * 检查权限
     */
    private boolean checkPermission(UserContext context, RequirePermission annotation) {
        String[] roles = annotation.roles();
        String[] permissions = annotation.value();

        // 如果没有指定任何权限要求，只要登录即可
        if ((roles == null || roles.length == 0) &&
                (permissions == null || permissions.length == 0)) {
            return true;
        }

        // 检查角色
        if (roles != null && roles.length > 0) {
            if (context.hasAnyRole(roles)) {
                return true;
            }
        }

        // 检查权限
        if (permissions != null && permissions.length > 0) {
            if (context.hasAnyPermission(permissions)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 从请求中提取token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 获取角色的权限列表
     */
    private List<String> getPermissions(String roleCode) {
        if (roleCode == null) {
            return Collections.emptyList();
        }

        try {
            Optional<SysRole> roleOpt = sysRoleRepository.findByCode(roleCode);
            if (roleOpt.isPresent() && roleOpt.get().getPermissions() != null) {
                String permJson = roleOpt.get().getPermissions();
                // 解析权限JSON
                Map<String, Object> permMap = objectMapper.readValue(permJson,
                        new TypeReference<Map<String, Object>>() {
                        });

                // 提取menus和actions作为权限
                List<String> permissions = new java.util.ArrayList<>();
                if (permMap.get("menus") instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> menus = (List<String>) permMap.get("menus");
                    permissions.addAll(menus);
                }
                if (permMap.get("actions") instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> actions = (List<String>) permMap.get("actions");
                    permissions.addAll(actions);
                }
                return permissions;
            }
        } catch (Exception e) {
            log.error("解析角色权限失败: {}", roleCode, e);
        }

        return Collections.emptyList();
    }

    /**
     * 发送未授权响应
     */
    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        ApiResponse<?> apiResponse = ApiResponse.error(401, message);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }

    /**
     * 发送禁止访问响应
     */
    private void sendForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        ApiResponse<?> apiResponse = ApiResponse.error(403, message);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
