package com.customer_service.portal.controller;

import com.customer_service.shared.dto.ApiResponse;
import com.customer_service.shared.entity.Project;
import com.customer_service.shared.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/pub")
@RequiredArgsConstructor
public class PublicConfigController {

    private final ProjectRepository projectRepository;

    @GetMapping("/config")
    public ApiResponse<?> getProjectConfig(@RequestParam Long projectId) {
        log.info("Get project config: {}", projectId);

        // 从数据库获取项目配置
        Optional<Project> projectOpt = projectRepository.findById(projectId);

        if (projectOpt.isEmpty()) {
            return ApiResponse.fail(404, "项目不存在");
        }

        Project project = projectOpt.get();

        // 解析配置
        String welcomeMessage = "欢迎咨询，我们随时准备为您服务";
        String themeColor = "#1890FF";
        String workingHours = "09:00-18:00";

        if (project.getConfig() != null) {
            try {
                var config = project.getConfig();
                if (config.has("welcomeMessage") && !config.get("welcomeMessage").asText().isEmpty()) {
                    welcomeMessage = config.get("welcomeMessage").asText();
                }
                if (config.has("themeColor") && !config.get("themeColor").asText().isEmpty()) {
                    themeColor = config.get("themeColor").asText();
                }
                if (config.has("workingHours") && !config.get("workingHours").asText().isEmpty()) {
                    workingHours = config.get("workingHours").asText();
                }
            } catch (Exception e) {
                log.warn("Failed to parse project config: {}", e.getMessage());
            }
        }

        ProjectConfigResponse configResponse = ProjectConfigResponse.builder()
                .projectId(projectId)
                .projectName(project.getName())
                .welcomeMessage(welcomeMessage)
                .themeColor(themeColor)
                .workingHours(workingHours)
                .build();
        return ApiResponse.success(configResponse);
    }

    public static class ProjectConfigResponse {
        private Long projectId;
        private String projectName;
        private String welcomeMessage;
        private String themeColor;
        private String workingHours;

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Long projectId;
            private String projectName;
            private String welcomeMessage;
            private String themeColor;
            private String workingHours;

            public Builder projectId(Long projectId) {
                this.projectId = projectId;
                return this;
            }

            public Builder projectName(String projectName) {
                this.projectName = projectName;
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
                return new ProjectConfigResponse(projectId, projectName, welcomeMessage, themeColor, workingHours);
            }
        }

        private ProjectConfigResponse(Long projectId, String projectName, String welcomeMessage, String themeColor,
                String workingHours) {
            this.projectId = projectId;
            this.projectName = projectName;
            this.welcomeMessage = welcomeMessage;
            this.themeColor = themeColor;
            this.workingHours = workingHours;
        }

        public Long getProjectId() {
            return projectId;
        }

        public String getProjectName() {
            return projectName;
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
