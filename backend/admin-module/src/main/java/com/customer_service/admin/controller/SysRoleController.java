package com.customer_service.admin.controller;

import com.customer_service.admin.service.SysRoleService;
import com.customer_service.shared.annotation.RequirePermission;
import com.customer_service.shared.constant.RoleCode;
import com.customer_service.shared.dto.ApiResponse;
import com.customer_service.shared.entity.SysRole;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * 角色管理控制器
 * 仅管理员可访问
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@RequirePermission(value = "role:manage", roles = { RoleCode.ADMIN })
public class SysRoleController {

    private final SysRoleService sysRoleService;
    private final ObjectMapper objectMapper;

    /**
     * 获取所有角色（用于下拉选择）
     */
    @GetMapping("/all")
    public ApiResponse<?> getAllRoles() {
        List<SysRole> roles = sysRoleService.getAllRoles();
        return ApiResponse.success(roles.stream().map(this::toRoleVO).collect(Collectors.toList()));
    }

    /**
     * 获取角色列表（分页）
     */
    @GetMapping
    public ApiResponse<?> listRoles(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {

        Page<SysRole> rolePage = sysRoleService.getRoles(keyword, page - 1, size);

        Map<String, Object> result = new HashMap<>();
        result.put("list", rolePage.getContent().stream().map(this::toRoleVO).collect(Collectors.toList()));
        result.put("total", rolePage.getTotalElements());
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", rolePage.getTotalPages());

        return ApiResponse.success(result);
    }

    /**
     * 获取单个角色详情
     */
    @GetMapping("/{id}")
    public ApiResponse<?> getRole(@PathVariable Long id) {
        return sysRoleService.getRoleById(id)
                .map(role -> ApiResponse.success(toRoleVO(role)))
                .orElse(ApiResponse.error("角色不存在"));
    }

    /**
     * 创建角色
     */
    @PostMapping
    public ApiResponse<?> createRole(@RequestBody CreateRoleRequest request) {
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            return ApiResponse.error("角色代码不能为空");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return ApiResponse.error("角色名称不能为空");
        }

        try {
            // 将 permissions 对象转换为 JSON 字符串
            String permissionsJson = null;
            if (request.getPermissions() != null) {
                permissionsJson = objectMapper.writeValueAsString(request.getPermissions());
            }

            SysRole role = sysRoleService.createRole(
                    request.getCode(),
                    request.getName(),
                    request.getDescription(),
                    permissionsJson);
            log.info("创建角色成功: {} (ID: {})", role.getName(), role.getId());
            return ApiResponse.success(toRoleVO(role));
        } catch (JsonProcessingException e) {
            log.error("权限数据格式错误", e);
            return ApiResponse.error("权限数据格式错误");
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    public ApiResponse<?> updateRole(@PathVariable Long id, @RequestBody UpdateRoleRequest request) {
        try {
            // 将 permissions 对象转换为 JSON 字符串
            String permissionsJson = null;
            if (request.getPermissions() != null) {
                permissionsJson = objectMapper.writeValueAsString(request.getPermissions());
            }

            SysRole role = sysRoleService.updateRole(
                    id,
                    request.getName(),
                    request.getDescription(),
                    permissionsJson);
            log.info("更新角色成功: {} (ID: {})", role.getName(), role.getId());
            return ApiResponse.success(toRoleVO(role));
        } catch (JsonProcessingException e) {
            log.error("权限数据格式错误", e);
            return ApiResponse.error("权限数据格式错误");
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteRole(@PathVariable Long id) {
        try {
            sysRoleService.deleteRole(id);
            log.info("删除角色成功: ID={}", id);
            return ApiResponse.success("删除成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 转换为VO对象
     */
    private Map<String, Object> toRoleVO(SysRole role) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", role.getId());
        vo.put("code", role.getCode());
        vo.put("name", role.getName());
        vo.put("description", role.getDescription());
        vo.put("permissions", role.getPermissions());
        vo.put("isSystem", role.getIsSystem());
        vo.put("createdAt", role.getCreatedAt());
        vo.put("updatedAt", role.getUpdatedAt());
        return vo;
    }

    @Data
    public static class CreateRoleRequest {
        private String code;
        private String name;
        private String description;
        private Object permissions; // 支持对象或字符串
    }

    @Data
    public static class UpdateRoleRequest {
        private String name;
        private String description;
        private Object permissions; // 支持对象或字符串
    }
}
