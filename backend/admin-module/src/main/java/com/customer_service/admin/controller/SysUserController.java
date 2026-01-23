package com.customer_service.admin.controller;

import com.customer_service.admin.service.SysUserService;
import com.customer_service.shared.annotation.RequirePermission;
import com.customer_service.shared.dto.ApiResponse;
import com.customer_service.shared.entity.SysUser;
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
 * 用户管理控制器
 * 仅管理员可访问
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@RequirePermission(value = "user:manage", roles = { "admin" })
public class SysUserController {

    private final SysUserService sysUserService;

    /**
     * 获取用户列表（分页）
     */
    @GetMapping
    public ApiResponse<?> listUsers(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {

        Page<SysUser> userPage = sysUserService.getUsers(keyword, page - 1, size);

        Map<String, Object> result = new HashMap<>();
        result.put("list", userPage.getContent().stream().map(this::toUserVO).collect(Collectors.toList()));
        result.put("total", userPage.getTotalElements());
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", userPage.getTotalPages());

        return ApiResponse.success(result);
    }

    /**
     * 获取单个用户详情
     */
    @GetMapping("/{id}")
    public ApiResponse<?> getUser(@PathVariable Long id) {
        return sysUserService.getUserById(id)
                .map(user -> ApiResponse.success(toUserVO(user)))
                .orElse(ApiResponse.error("用户不存在"));
    }

    /**
     * 创建用户
     */
    @PostMapping
    public ApiResponse<?> createUser(@RequestBody CreateUserRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return ApiResponse.error("用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            return ApiResponse.error("密码长度不能少于6位");
        }

        try {
            SysUser user = sysUserService.createUser(
                    request.getUsername(),
                    request.getPassword(),
                    request.getEmail(),
                    request.getPhone(),
                    request.getRoleId());
            log.info("创建用户成功: {} (ID: {})", user.getUsername(), user.getId());
            return ApiResponse.success(toUserVO(user));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public ApiResponse<?> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        try {
            SysUser user = sysUserService.updateUser(
                    id,
                    request.getEmail(),
                    request.getPhone(),
                    request.getRoleId(),
                    request.getStatus());
            log.info("更新用户成功: {} (ID: {})", user.getUsername(), user.getId());
            return ApiResponse.success(toUserVO(user));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 修改用户密码
     */
    @PostMapping("/{id}/password")
    public ApiResponse<?> changePassword(@PathVariable Long id, @RequestBody ChangePasswordRequest request) {
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            return ApiResponse.error("密码长度不能少于6位");
        }

        try {
            sysUserService.changePassword(id, request.getNewPassword());
            log.info("修改用户密码成功: ID={}", id);
            return ApiResponse.success("密码修改成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteUser(@PathVariable Long id) {
        try {
            sysUserService.deleteUser(id);
            log.info("删除用户成功: ID={}", id);
            return ApiResponse.success("删除成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 启用/禁用用户
     */
    @PostMapping("/{id}/toggle-status")
    public ApiResponse<?> toggleStatus(@PathVariable Long id) {
        try {
            SysUser user = sysUserService.toggleStatus(id);
            log.info("切换用户状态成功: {} -> {}", user.getUsername(), user.getStatus());
            return ApiResponse.success(toUserVO(user));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 转换为VO对象
     */
    private Map<String, Object> toUserVO(SysUser user) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", user.getId());
        vo.put("username", user.getUsername());
        vo.put("email", user.getEmail());
        vo.put("phone", user.getPhone());
        vo.put("avatar", user.getAvatar());
        vo.put("status", user.getStatus());
        vo.put("lastLoginAt", user.getLastLoginAt());
        vo.put("createdAt", user.getCreatedAt());
        vo.put("updatedAt", user.getUpdatedAt());

        if (user.getRole() != null) {
            Map<String, Object> roleVO = new HashMap<>();
            roleVO.put("id", user.getRole().getId());
            roleVO.put("code", user.getRole().getCode());
            roleVO.put("name", user.getRole().getName());
            vo.put("role", roleVO);
        }

        return vo;
    }

    @Data
    public static class CreateUserRequest {
        private String username;
        private String password;
        private String email;
        private String phone;
        private Long roleId;
    }

    @Data
    public static class UpdateUserRequest {
        private String email;
        private String phone;
        private Long roleId;
        private String status;
    }

    @Data
    public static class ChangePasswordRequest {
        private String newPassword;
    }
}
