package com.customer_service.portal.controller;

import com.customer_service.shared.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/pub")
public class PublicConfigController {

    @GetMapping("/config")
    public ApiResponse<?> getProjectConfig(@RequestParam Long projectId) {
        log.info("Get project config: {}", projectId);
        // TODO: 从数据库获取项目配置
        ProjectConfigResponse config = ProjectConfigResponse.builder()
                .projectId(projectId)
                .welcomeMessage("欢迎咨询我们的客服系统")
                .themeColor("#1890FF")
                .workingHours("09:00-18:00")
                .build();
        return ApiResponse.success(config);
    }

    public static class ProjectConfigResponse {
        private Long projectId;
        private String welcomeMessage;
        private String themeColor;
        private String workingHours;

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Long projectId;
            private String welcomeMessage;
            private String themeColor;
            private String workingHours;

            public Builder projectId(Long projectId) {
                this.projectId = projectId;
                return this;
            }

            public Builder welcomeMessage(String welcomeMessage) {
                this.welcomeMessage = welcomeMessage;
                return this;
            }

            public Builder themeColor(String themeColor) {
                this.themeColor = themeColor;
                return this;
            }

            public Builder workingHours(String workingHours) {
                this.workingHours = workingHours;
                return this;
            }

            public ProjectConfigResponse build() {
                return new ProjectConfigResponse(projectId, welcomeMessage, themeColor, workingHours);
            }
        }

        private ProjectConfigResponse(Long projectId, String welcomeMessage, String themeColor, String workingHours) {
            this.projectId = projectId;
            this.welcomeMessage = welcomeMessage;
            this.themeColor = themeColor;
            this.workingHours = workingHours;
        }

        public Long getProjectId() {
            return projectId;
        }

        public String getWelcomeMessage() {
            return welcomeMessage;
        }

        public String getThemeColor() {
            return themeColor;
        }

        public String getWorkingHours() {
            return workingHours;
        }
    }
}
