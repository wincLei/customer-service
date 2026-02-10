package com.customer_service.admin.controller;

import com.customer_service.admin.service.ProjectService;
import com.customer_service.shared.annotation.RequirePermission;
import com.customer_service.shared.constant.RoleCode;
import com.customer_service.shared.context.UserContextHolder;
import com.customer_service.shared.dto.ApiResponse;
import com.customer_service.shared.entity.Project;
import com.customer_service.shared.util.I18nUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 项目管理控制器
 * 仅管理员可访问
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 获取所有项目列表（简单列表，用于下拉选择）
     * 客服只能看到关联的项目，管理员可以看到所有项目
     */
    @GetMapping("/all")
    @RequirePermission(value = "workbench", roles = { RoleCode.ADMIN, RoleCode.AGENT })
    public ApiResponse<List<Map<String, Object>>> getAllProjects() {
        String roleCode = UserContextHolder.getRoleCode();
        Long userId = UserContextHolder.getUserId();

        List<Project> projects;
        if ("agent".equals(roleCode) && userId != null) {
            // 客服只能看到关联的项目
            projects = projectService.getProjectsByUserId(userId);
        } else {
            // 管理员可以看到所有项目
            projects = projectService.getAllProjects();
        }

        List<Map<String, Object>> result = projects.stream()
                .map(this::toSimpleProjectVO)
                .collect(Collectors.toList());
        return ApiResponse.success(result);
    }

    /**
     * 获取项目列表（支持分页和搜索）
     */
    @GetMapping
    @RequirePermission(value = "project:manage", roles = { RoleCode.ADMIN })
    public ApiResponse<?> listProjects(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {

        // 页码从0开始，前端传的是从1开始
        Page<Project> projectPage = projectService.searchProjects(keyword, page - 1, size);

        Map<String, Object> result = new HashMap<>();
        result.put("list", projectPage.getContent().stream().map(this::toProjectVO).collect(Collectors.toList()));
        result.put("total", projectPage.getTotalElements());
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", projectPage.getTotalPages());

        return ApiResponse.success(result);
    }

    /**
     * 获取单个项目详情
     */
    @GetMapping("/{id}")
    @RequirePermission(value = "project:manage", roles = { RoleCode.ADMIN, RoleCode.AGENT })
    public ApiResponse<?> getProject(@PathVariable Long id) {
        return projectService.getProjectById(id)
                .map(project -> ApiResponse.success(toProjectVO(project)))
                .orElse(ApiResponse.error(I18nUtil.getMessage("project.not.found")));
    }

    /**
     * 创建项目
     */
    @PostMapping
    @RequirePermission(value = "project:manage", roles = { RoleCode.ADMIN })
    public ApiResponse<?> createProject(@RequestBody CreateProjectRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return ApiResponse.error(I18nUtil.getMessage("project.name.required"));
        }

        try {
            Project project = projectService.createProject(request.getName(), request.getDescription(),
                    request.getWelcomeMessage());
            log.info("创建项目成功: {} (ID: {})", project.getName(), project.getId());
            return ApiResponse.success(toProjectVO(project));
        } catch (Exception e) {
            log.error("创建项目失败: {}", e.getMessage(), e);
            return ApiResponse.error(I18nUtil.getMessage("project.create.error") + ": " + e.getMessage());
        }
    }

    /**
     * 更新项目
     */
    @PutMapping("/{id}")
    @RequirePermission(value = "project:manage", roles = { RoleCode.ADMIN })
    public ApiResponse<?> updateProject(@PathVariable Long id, @RequestBody UpdateProjectRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return ApiResponse.error(I18nUtil.getMessage("project.name.required"));
        }

        try {
            Project project = projectService.updateProject(id, request.getName(), request.getDescription(),
                    request.getWelcomeMessage());
            log.info("更新项目成功: {} (ID: {})", project.getName(), project.getId());
            return ApiResponse.success(toProjectVO(project));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除项目
     */
    @DeleteMapping("/{id}")
    @RequirePermission(value = "project:manage", roles = { RoleCode.ADMIN })
    public ApiResponse<?> deleteProject(@PathVariable Long id) {
        try {
            projectService.deleteProject(id);
            log.info("删除项目成功: ID={}", id);
            return ApiResponse.success(I18nUtil.getMessage("common.delete.success"));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 重新生成AppSecret
     */
    @PostMapping("/{id}/regenerate-secret")
    @RequirePermission(value = "project:manage", roles = { RoleCode.ADMIN })
    public ApiResponse<?> regenerateSecret(@PathVariable Long id) {
        try {
            Project project = projectService.regenerateAppSecret(id);
            log.info("重新生成AppSecret成功: {} (ID: {})", project.getName(), project.getId());
            return ApiResponse.success(toProjectVO(project));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 转换为VO对象（完整信息）
     */
    private Map<String, Object> toProjectVO(Project project) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", project.getId());
        vo.put("name", project.getName());
        vo.put("description", project.getDescription());
        vo.put("appKey", project.getAppKey());
        vo.put("appSecret", project.getAppSecret());
        vo.put("createdAt", project.getCreatedAt());
        vo.put("updatedAt", project.getUpdatedAt());

        // 提取欢迎语配置
        String welcomeMessage = "";
        if (project.getConfig() != null && project.getConfig().has("welcomeMessage")) {
            welcomeMessage = project.getConfig().get("welcomeMessage").asText("");
        }
        vo.put("welcomeMessage", welcomeMessage);

        return vo;
    }

    /**
     * 转换为简单VO对象（用于下拉选择）
     */
    private Map<String, Object> toSimpleProjectVO(Project project) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", project.getId());
        vo.put("name", project.getName());
        return vo;
    }

    @Data
    public static class CreateProjectRequest {
        private String name;
        private String description;
        private String welcomeMessage;
    }

    @Data
    public static class UpdateProjectRequest {
        private String name;
        private String description;
        private String welcomeMessage;
    }
}
