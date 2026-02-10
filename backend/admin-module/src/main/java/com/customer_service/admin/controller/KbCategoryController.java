package com.customer_service.admin.controller;

import com.customer_service.admin.service.KbCategoryService;
import com.customer_service.shared.annotation.RequirePermission;
import com.customer_service.shared.constant.RoleCode;
import com.customer_service.shared.dto.ApiResponse;
import com.customer_service.shared.entity.KbCategory;
import com.customer_service.shared.util.I18nUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识库分类管理控制器
 */
@RestController
@RequestMapping("/api/admin/kb/categories")
@RequiredArgsConstructor
public class KbCategoryController {

    private final KbCategoryService kbCategoryService;

    /**
     * 获取分类列表（扁平）
     */
    @GetMapping
    @RequirePermission(value = "workbench", roles = { RoleCode.ADMIN, RoleCode.AGENT })
    public ApiResponse<List<KbCategory>> getCategories(@RequestParam Long projectId) {
        return ApiResponse.success(kbCategoryService.getAllCategories(projectId));
    }

    /**
     * 获取分类树
     */
    @GetMapping("/tree")
    @RequirePermission(value = "workbench", roles = { RoleCode.ADMIN, RoleCode.AGENT })
    public ApiResponse<List<KbCategoryService.CategoryTreeNode>> getCategoryTree(@RequestParam Long projectId) {
        return ApiResponse.success(kbCategoryService.getCategoryTree(projectId));
    }

    /**
     * 获取分类详情
     */
    @GetMapping("/{id}")
    @RequirePermission(value = "workbench", roles = { RoleCode.ADMIN, RoleCode.AGENT })
    public ApiResponse<KbCategory> getCategory(@PathVariable Long id) {
        KbCategory category = kbCategoryService.getCategoryById(id);
        if (category == null) {
            return ApiResponse.error(404, I18nUtil.getMessage("kb.category.not.found"));
        }
        return ApiResponse.success(category);
    }

    /**
     * 创建分类
     */
    @PostMapping
    @RequirePermission(value = "kb:manage", roles = { RoleCode.ADMIN, RoleCode.AGENT })
    public ApiResponse<KbCategory> createCategory(@RequestBody KbCategory category) {
        try {
            KbCategory created = kbCategoryService.createCategory(category);
            return ApiResponse.success(created);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    @RequirePermission(value = "kb:manage", roles = { RoleCode.ADMIN, RoleCode.AGENT })
    public ApiResponse<KbCategory> updateCategory(@PathVariable Long id, @RequestBody KbCategory category) {
        try {
            KbCategory updated = kbCategoryService.updateCategory(id, category);
            return ApiResponse.success(updated);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    @RequirePermission(value = "kb:manage", roles = { RoleCode.ADMIN, RoleCode.AGENT })
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        try {
            kbCategoryService.deleteCategory(id);
            return ApiResponse.success(null);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
}
