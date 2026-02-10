package com.customer_service.portal.security;

import com.customer_service.shared.constant.AppDefaults;
import com.customer_service.shared.dto.ApiResponse;
import com.customer_service.shared.util.I18nUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Portal Token 拦截器
 * 验证 Portal 端用户的访问令牌
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PortalTokenInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final PortalTokenProvider tokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // OPTIONS 请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = resolveToken(request);

        if (!StringUtils.hasText(token)) {
            log.warn("Missing authorization token for request: {} {}", request.getMethod(), request.getRequestURI());
            sendUnauthorizedResponse(response, I18nUtil.getMessage("portal.auth.token.missing"));
            return false;
        }

        if (!tokenProvider.validateToken(token)) {
            log.warn("Invalid authorization token for request: {} {}", request.getMethod(), request.getRequestURI());
            sendUnauthorizedResponse(response, I18nUtil.getMessage("portal.auth.token.invalid"));
            return false;
        }

        // 设置用户上下文
        Long userId = tokenProvider.getUserIdFromToken(token);
        Long projectId = tokenProvider.getProjectIdFromToken(token);
        String uid = tokenProvider.getUidFromToken(token);

        PortalUserContext context = new PortalUserContext(userId, projectId, uid);
        PortalUserContext.setContext(context);

        log.debug("Portal token validated for user: {} (uid: {}, projectId: {})", userId, uid, projectId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {
        // 清除用户上下文
        PortalUserContext.clear();
    }

    /**
     * 从请求头中获取 Token
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(AppDefaults.BEARER_PREFIX)) {
            return bearerToken.substring(AppDefaults.BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * 发送 401 未授权响应
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponse<?> apiResponse = ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), message);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
