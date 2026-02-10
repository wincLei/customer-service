package com.customer_service.admin.controller;

import com.customer_service.admin.service.CustomerTagService;
import com.customer_service.shared.annotation.RequirePermission;
import com.customer_service.shared.constant.RoleCode;
import com.customer_service.shared.context.UserContextHolder;
import com.customer_service.shared.dto.ApiResponse;
import com.customer_service.shared.entity.CustomerTag;
import com.customer_service.shared.repository.UserProjectRepository;
import com.customer_service.shared.util.I18nUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 客户标签管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/customer-tags")
@RequiredArgsConstructor
@RequirePermission(value = "workbench", roles = { RoleCode.ADMIN, RoleCode.AGENT })
public class CustomerTagController {

    private final CustomerTagService customerTagService;
    private final UserProjectRepository userProjectRepository;

    /**
     * 获取项目下的所有标签
     */
    @GetMapping
    public ApiResponse<?> getTags(@RequestParam Long projectId) {
        if (!hasProjectAccess(projectId)) {
            return ApiResponse.error(403, I18nUtil.getMessage("tag.no.project.permission"));
        }

        List<CustomerTag> tags = customerTagService.getTagsByProject(projectId);
        List<Map<String, Object>> result = tags.stream()
                .map(this::toTagVO)
                .collect(Collectors.toList());
        return ApiResponse.success(result);
    }

    /**
     * 获取标签详情
     */
    @GetMapping("/{id}")
    public ApiResponse<?> getTag(@PathVariable Long id) {
        CustomerTag tag = customerTagService.getTagById(id);
        if (tag == null) {
            return ApiResponse.error(404, I18nUtil.getMessage("tag.not.found"));
        }

        if (!hasProjectAccess(tag.getProjectId())) {
            return ApiResponse.error(403, I18nUtil.getMessage("tag.no.permission"));
        }

        Map<String, Object> result = toTagVO(tag);
        result.put("userCount", customerTagService.getTagUserCount(id));
        return ApiResponse.success(result);
    }

    /**
     * 创建标签
     */
    @PostMapping
    public ApiResponse<?> createTag(@RequestBody CreateTagRequest request) {
        if (!hasProjectAccess(request.getProjectId())) {
            return ApiResponse.error(403, I18nUtil.getMessage("tag.no.operate.permission"));
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return ApiResponse.error(I18nUtil.getMessage("tag.name.required"));
        }

        try {
            Long operatorId = UserContextHolder.getUserId();
            CustomerTag tag = customerTagService.createTag(
                    request.getProjectId(),
                    request.getName().trim(),
                    request.getColor(),
                    request.getDescription(),
                    request.getSortOrder(),
                    operatorId);
            log.info("创建标签成功: {} (ID: {})", tag.getName(), tag.getId());
            return ApiResponse.success(toTagVO(tag));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新标签
     */
    @PutMapping("/{id}")
    public ApiResponse<?> updateTag(@PathVariable Long id, @RequestBody UpdateTagRequest request) {
        CustomerTag tag = customerTagService.getTagById(id);
        if (tag == null) {
            return ApiResponse.error(404, I18nUtil.getMessage("tag.not.found"));
        }

        if (!hasProjectAccess(tag.getProjectId())) {
            return ApiResponse.error(403, I18nUtil.getMessage("tag.no.update.permission"));
        }

        try {
            CustomerTag updated = customerTagService.updateTag(
                    id,
                    request.getName(),
                    request.getColor(),
                    request.getDescription(),
                    request.getSortOrder());
            log.info("更新标签成功: {} (ID: {})", updated.getName(), updated.getId());
            return ApiResponse.success(toTagVO(updated));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除标签
     */
    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteTag(@PathVariable Long id) {
        CustomerTag tag = customerTagService.getTagById(id);
        if (tag == null) {
            return ApiResponse.error(404, I18nUtil.getMessage("tag.not.found"));
        }

        if (!hasProjectAccess(tag.getProjectId())) {
            return ApiResponse.error(403, I18nUtil.getMessage("tag.no.delete.permission"));
        }

        try {
            customerTagService.deleteTag(id);
            return ApiResponse.success(I18nUtil.getMessage("common.delete.success"));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 检查当前用户是否有权限访问指定项目
     */
    private boolean hasProjectAccess(Long projectId) {
        String roleCode = UserContextHolder.getRoleCode();
        if ("admin".equals(roleCode) || "super_admin".equals(roleCode)) {
            return true;
        }

        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            return false;
        }

        List<Long> accessibleProjects = userProjectRepository.findProjectIdsByUserId(userId);
        return accessibleProjects.contains(projectId);
    }

    /**
     * 转换为 VO
     */
    private Map<String, Object> toTagVO(CustomerTag tag) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", tag.getId());
        vo.put("projectId", tag.getProjectId());
        vo.put("name", tag.getName());
        vo.put("color", tag.getColor());
        vo.put("description", tag.getDescription());
        vo.put("sortOrder", tag.getSortOrder());
        vo.put("createdAt", tag.getCreatedAt());
        vo.put("updatedAt", tag.getUpdatedAt());
        return vo;
    }

    @Data
    public static class CreateTagRequest {
        private Long projectId;
        private String name;
        private String color;
        private String description;
        private Integer sortOrder;
    }

    @Data
    public static class UpdateTagRequest {
        private String name;
        private String color;
        private String description;
        private Integer sortOrder;
    }
}
