package com.customer_service.admin.controller;

import com.customer_service.shared.annotation.RequirePermission;
import com.customer_service.shared.constant.RoleCode;
import com.customer_service.admin.service.SysMenuService;
import com.customer_service.shared.entity.SysMenu;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/menus")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService menuService;

    /**
     * 获取菜单树（用于管理界面）
     */
    @GetMapping("/tree")
    @RequirePermission(value = "menu:manage", roles = { RoleCode.ADMIN })
    public ResponseEntity<Map<String, Object>> getMenuTree() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", "success");
        response.put("data", menuService.getMenuTree());
        return ResponseEntity.ok(response);
    }

    /**
     * 获取启用的菜单树（用于角色授权选择）
     */
    @GetMapping("/enabled-tree")
    @RequirePermission(value = "role:manage", roles = { RoleCode.ADMIN })
    public ResponseEntity<Map<String, Object>> getEnabledMenuTree() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", "success");
        response.put("data", menuService.getEnabledMenuTree());
        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有菜单（平铺列表）
     */
    @GetMapping
    @RequirePermission(value = "menu:manage", roles = { RoleCode.ADMIN })
    public ResponseEntity<Map<String, Object>> getAllMenus() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("message", "success");
        response.put("data", menuService.getAllMenus());
        return ResponseEntity.ok(response);
    }

    /**
     * 获取菜单详情
     */
    @GetMapping("/{id}")
    @RequirePermission(value = "menu:manage", roles = { RoleCode.ADMIN })
    public ResponseEntity<Map<String, Object>> getMenuById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        return menuService.getMenuById(id)
                .map(menu -> {
                    response.put("code", 0);
                    response.put("message", "success");
                    response.put("data", menu);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    response.put("code", 404);
                    response.put("message", "菜单不存在");
                    return ResponseEntity.ok(response);
                });
    }

    /**
     * 创建菜单
     */
    @PostMapping
    @RequirePermission(value = "menu:manage", roles = { RoleCode.ADMIN })
    public ResponseEntity<Map<String, Object>> createMenu(@RequestBody SysMenu menu) {
        Map<String, Object> response = new HashMap<>();
        try {
            SysMenu created = menuService.createMenu(menu);
            response.put("code", 0);
            response.put("message", "创建成功");
            response.put("data", created);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("code", 400);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 更新菜单
     */
    @PutMapping("/{id}")
    @RequirePermission(value = "menu:manage", roles = { RoleCode.ADMIN })
    public ResponseEntity<Map<String, Object>> updateMenu(@PathVariable Long id, @RequestBody SysMenu menu) {
        Map<String, Object> response = new HashMap<>();
        try {
            SysMenu updated = menuService.updateMenu(id, menu);
            response.put("code", 0);
            response.put("message", "更新成功");
            response.put("data", updated);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("code", 400);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    @RequirePermission(value = "menu:manage", roles = { RoleCode.ADMIN })
    public ResponseEntity<Map<String, Object>> deleteMenu(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            menuService.deleteMenu(id);
            response.put("code", 0);
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("code", 400);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 切换菜单启用状态
     */
    @PostMapping("/{id}/toggle")
    @RequirePermission(value = "menu:manage", roles = { RoleCode.ADMIN })
    public ResponseEntity<Map<String, Object>> toggleMenuStatus(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            SysMenu menu = menuService.toggleMenuStatus(id);
            response.put("code", 0);
            response.put("message", menu.getIsEnabled() ? "已启用" : "已禁用");
            response.put("data", menu);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("code", 400);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}
