package com.customer_service.admin.controller;

import com.customer_service.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AuthController {

    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody LoginRequest request) {
        // TODO: 实现登录逻辑
        log.info("Agent login attempt: {}", request.getUsername());
        return ApiResponse.success("Login successful");
    }

    @GetMapping("/logout")
    public ApiResponse<?> logout() {
        // TODO: 实现登出逻辑
        return ApiResponse.success("Logout successful");
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
