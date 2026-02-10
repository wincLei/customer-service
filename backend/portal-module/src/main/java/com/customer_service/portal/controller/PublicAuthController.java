package com.customer_service.portal.controller;

import com.customer_service.shared.dto.ApiResponse;
import com.customer_service.shared.dto.LoginRequest;
import com.customer_service.shared.dto.LoginResponse;
import com.customer_service.shared.util.I18nUtil;
import com.customer_service.shared.entity.SysUser;
import com.customer_service.shared.util.JwtTokenProvider;
import com.customer_service.portal.service.PortalAgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pub/auth")
@RequiredArgsConstructor
public class PublicAuthController {

    private final PortalAgentService agentService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            return ApiResponse.fail(400, I18nUtil.getMessage("portal.auth.username.required"));
        }

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return ApiResponse.fail(400, I18nUtil.getMessage("portal.auth.password.required"));
        }

        Optional<SysUser> userOptional = agentService.authenticate(request.getUsername(), request.getPassword());

        if (userOptional.isEmpty()) {
            return ApiResponse.fail(401, I18nUtil.getMessage("portal.auth.login.failed"));
        }

        SysUser user = userOptional.get();

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("role", user.getRoleCode());

        String token = jwtTokenProvider.generateToken(user.getUsername(), claims);

        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getUsername()) // SysUser 没有 nickname，使用 username
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .role(user.getRoleCode())
                .status(user.getStatus())
                .build();

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .user(userInfo)
                .build();

        return ApiResponse.success(response);
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refreshToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ApiResponse.fail(401, I18nUtil.getMessage("auth.token.invalid"));
        }

        String token = authHeader.substring(7);

        if (!jwtTokenProvider.validateToken(token)) {
            return ApiResponse.fail(401, I18nUtil.getMessage("auth.token.expired.portal"));
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);
        Optional<SysUser> userOptional = agentService.findByUsername(username);

        if (userOptional.isEmpty()) {
            return ApiResponse.fail(401, I18nUtil.getMessage("auth.user.not.found"));
        }

        SysUser user = userOptional.get();

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("role", user.getRoleCode());

        String newToken = jwtTokenProvider.generateToken(user.getUsername(), claims);

        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .role(user.getRoleCode())
                .status(user.getStatus())
                .build();

        LoginResponse response = LoginResponse.builder()
                .token(newToken)
                .user(userInfo)
                .build();

        return ApiResponse.success(response);
    }
}
