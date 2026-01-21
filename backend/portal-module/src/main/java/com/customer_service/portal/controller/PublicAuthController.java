package com.customer_service.portal.controller;

import com.customer_service.shared.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/pub/auth")
public class PublicAuthController {

    @PostMapping("/visitor")
    public ApiResponse<?> visitorLogin(@RequestBody VisitorLoginRequest request) {
        log.info("Visitor login attempt");
        // TODO: 实现访客登录逻辑
        return ApiResponse.success(new VisitorLoginResponse("mock-token-123"));
    }

    public static class VisitorLoginRequest {
        private String projectId;
        private String deviceType;
        private String nickName;
        private String phone;

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public String getDeviceType() {
            return deviceType;
        }

        public void setDeviceType(String deviceType) {
            this.deviceType = deviceType;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }

    public static class VisitorLoginResponse {
        private String token;

        public VisitorLoginResponse(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
