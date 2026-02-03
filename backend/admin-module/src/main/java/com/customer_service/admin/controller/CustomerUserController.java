package com.customer_service.admin.controller;

import com.customer_service.admin.dto.CustomerTagDTO;
import com.customer_service.admin.service.CustomerUserService;
import com.customer_service.shared.annotation.RequirePermission;
import com.customer_service.shared.context.UserContextHolder;
import com.customer_service.shared.dto.ApiResponse;
import com.customer_service.shared.entity.CustomerTag;
import com.customer_service.shared.entity.User;
import com.customer_service.shared.repository.UserProjectRepository;
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
 * 客户端用户管理控制器
 * 管理使用客服系统的终端用户（访客/客户）
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
@RequirePermission(value = "workbench", roles = { "admin", "agent" })
public class CustomerUserController {

    private final CustomerUserService customerUserService;
    private final UserProjectRepository userProjectRepository;

    /**
     * 分页获取客户列表
     */
    @GetMapping
    public ApiResponse<?> getCustomers(
            @RequestParam Long projectId,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false) Boolean isGuest,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {

        // 权限检查：客服只能查看自己关联的项目
        if (!hasProjectAccess(projectId)) {
            return ApiResponse.error(403, "您没有权限访问该项目的用户");
        }

        Page<User> userPage = customerUserService.getUsers(projectId, keyword, isGuest, page - 1, size);

        Map<String, Object> result = new HashMap<>();
        result.put("list", userPage.getContent().stream().map(this::toUserVO).collect(Collectors.toList()));
        result.put("total", userPage.getTotalElements());
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", userPage.getTotalPages());

        return ApiResponse.success(result);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public ApiResponse<?> getCustomerDetail(@PathVariable Long id) {
        Map<String, Object> detail = customerUserService.getUserDetail(id);
        if (detail == null) {
            return ApiResponse.error(404, "用户不存在");
        }

        User user = (User) detail.get("user");
        // 权限检查
        if (!hasProjectAccess(user.getProjectId())) {
            return ApiResponse.error(403, "您没有权限访问该用户");
        }

        Map<String, Object> result = toUserVO(user);
        result.put("tags", detail.get("tags"));
        return ApiResponse.success(result);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public ApiResponse<?> updateCustomer(@PathVariable Long id, @RequestBody UpdateCustomerRequest request) {
        User user = customerUserService.getUserById(id);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }

        // 权限检查
        if (!hasProjectAccess(user.getProjectId())) {
            return ApiResponse.error(403, "您没有权限修改该用户");
        }

        try {
            User updated = customerUserService.updateUser(id, request.getNickname(), request.getPhone(),
                    request.getEmail());
            return ApiResponse.success(toUserVO(updated));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 给用户添加标签
     */
    @PostMapping("/{id}/tags")
    public ApiResponse<?> addTagToCustomer(@PathVariable Long id, @RequestBody AddTagRequest request) {
        User user = customerUserService.getUserById(id);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }

        if (!hasProjectAccess(user.getProjectId())) {
            return ApiResponse.error(403, "您没有权限操作该用户");
        }

        try {
            Long operatorId = UserContextHolder.getUserId();
            customerUserService.addTagToUser(id, request.getTagId(), operatorId);
            return ApiResponse.success("标签添加成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 移除用户的标签
     */
    @DeleteMapping("/{id}/tags/{tagId}")
    public ApiResponse<?> removeTagFromCustomer(@PathVariable Long id, @PathVariable Long tagId) {
        User user = customerUserService.getUserById(id);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }

        if (!hasProjectAccess(user.getProjectId())) {
            return ApiResponse.error(403, "您没有权限操作该用户");
        }

        try {
            customerUserService.removeTagFromUser(id, tagId);
            return ApiResponse.success("标签移除成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 设置用户的标签（替换）
     */
    @PutMapping("/{id}/tags")
    public ApiResponse<?> setCustomerTags(@PathVariable Long id, @RequestBody SetTagsRequest request) {
        User user = customerUserService.getUserById(id);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }

        if (!hasProjectAccess(user.getProjectId())) {
            return ApiResponse.error(403, "您没有权限操作该用户");
        }

        try {
            Long operatorId = UserContextHolder.getUserId();
            customerUserService.setUserTags(id, request.getTagIds(), operatorId);
            return ApiResponse.success("标签设置成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取用户的标签
     */
    @GetMapping("/{id}/tags")
    public ApiResponse<?> getCustomerTags(@PathVariable Long id) {
        User user = customerUserService.getUserById(id);
        if (user == null) {
            return ApiResponse.error(404, "用户不存在");
        }

        if (!hasProjectAccess(user.getProjectId())) {
            return ApiResponse.error(403, "您没有权限查看该用户");
        }

        List<CustomerTag> tags = customerUserService.getUserTags(id);
        // 转换为 DTO，避免 Hibernate 懒加载代理序列化问题
        List<CustomerTagDTO> tagDTOs = tags.stream()
                .map(CustomerTagDTO::fromEntity)
                .collect(Collectors.toList());
        return ApiResponse.success(tagDTOs);
    }

    /**
     * 批量添加标签
     */
    @PostMapping("/batch/tags")
    public ApiResponse<?> batchAddTags(@RequestBody BatchAddTagRequest request) {
        try {
            Long operatorId = UserContextHolder.getUserId();
            customerUserService.batchAddTagToUsers(request.getUserIds(), request.getTagId(), operatorId);
            return ApiResponse.success("批量添加标签成功");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取项目用户统计
     */
    @GetMapping("/stats")
    public ApiResponse<?> getStats(@RequestParam Long projectId) {
        if (!hasProjectAccess(projectId)) {
            return ApiResponse.error(403, "您没有权限访问该项目");
        }

        Map<String, Object> stats = customerUserService.getUserStats(projectId);
        return ApiResponse.success(stats);
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
    private Map<String, Object> toUserVO(User user) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", user.getId());
        vo.put("projectId", user.getProjectId());
        vo.put("uid", user.getUid());
        vo.put("externalUid", user.getExternalUid());
        vo.put("isGuest", user.getIsGuest());
        vo.put("nickname", user.getNickname());
        vo.put("avatar", user.getAvatar());
        vo.put("email", user.getEmail());
        vo.put("phone", user.getPhone());
        vo.put("deviceType", user.getDeviceType());
        vo.put("sourceUrl", user.getSourceUrl());
        vo.put("city", user.getCity());
        vo.put("lastActiveAt", user.getLastActiveAt());
        vo.put("createdAt", user.getCreatedAt());
        return vo;
    }

    @Data
    public static class UpdateCustomerRequest {
        private String nickname;
        private String phone;
        private String email;
    }

    @Data
    public static class AddTagRequest {
        private Long tagId;
    }

    @Data
    public static class SetTagsRequest {
        private List<Long> tagIds;
    }

    @Data
    public static class BatchAddTagRequest {
        private List<Long> userIds;
        private Long tagId;
    }
}
