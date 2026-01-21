package com.customer_service.portal.controller;

import com.customer_service.shared.dto.ApiResponse;
import com.customer_service.shared.dto.LoginRequest;
import com.customer_service.shared.dto.LoginResponse;
import com.customer_service.shared.entity.Agent;
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
            return ApiResponse.fail(400, "用户名不能为空");
        }
        
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return ApiResponse.fail(400, "密码不能为空");
        }

        Optional<Agent> agent = agentService.authenticate(request.getUsername(), request.getPassword());
        
        if (agent.isEmpty()) {
            return ApiResponse.fail(401, "用户名或密码错误");
        }

        Agent a = agent.get();
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", a.getId());
        claims.put("role", a.getRole());

        String token = jwtTokenProvider.generateToken(a.getUsername(), claims);

        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(a.getId())
                .username(a.getUsername())
                .nickname(a.getNickname())
                .email(a.getEmail())
                .avatar(a.getAvatar())
                .role(a.getRole())
                .status(a.getStatus())
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
            return ApiResponse.fail(401, "无效的令牌");
        }

        String token = authHeader.substring(7);

        if (!jwtTokenProvider.validateToken(token)) {
            return ApiResponse.fail(401, "令牌已过期");
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);
        Optional<Agent> agent = agentService.findByUsername(username);

        if (agent.isEmpty()) {
            return ApiResponse.fail(401, "用户不存在");
        }

        Agent a = agent.get();

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", a.getId());
        claims.put("role", a.getRole());

        String newToken = jwtTokenProvider.generateToken(a.getUsername(), claims);

        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(a.getId())
                .username(a.getUsername())
                .nickname(a.getNickname())
                .email(a.getEmail())
                .avatar(a.getAvatar())
                .role(a.getRole())
                .status(a.getStatus())
                .build();

        LoginResponse response = LoginResponse.builder()
                .token(newToken)
                .user(userInfo)
                .build();

        return ApiResponse.success(response);
    }

    @GetMapping("/visitor")
    public ApiResponse<LoginResponse> visitorLogin() {
        // TODO: Implement visitor login
        return ApiResponse.fail(500, "Not implemented");
    }
}
