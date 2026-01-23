package com.customer_service.admin.controller;

import com.customer_service.admin.service.KbArticleService;
import com.customer_service.shared.annotation.RequirePermission;
import com.customer_service.shared.dto.ApiResponse;
import com.customer_service.shared.entity.KbArticle;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识库文章管理控制器
 * 客服和管理员可访问
 */
@RestController
@RequestMapping("/api/admin/kb/articles")
@RequiredArgsConstructor
@RequirePermission(value = "workbench", roles = { "admin", "agent" })
public class KbArticleController {

    private final KbArticleService kbArticleService;

    /**
     * 获取已发布的文章列表
     */
    @GetMapping
    public ApiResponse<List<KbArticle>> getArticles(@RequestParam Long projectId) {
        return ApiResponse.success(kbArticleService.getPublishedArticles(projectId));
    }

    /**
     * 获取所有文章列表（包含未发布）
     */
    @GetMapping("/all")
    public ApiResponse<List<KbArticle>> getAllArticles(@RequestParam Long projectId) {
        return ApiResponse.success(kbArticleService.getAllArticles(projectId));
    }

    /**
     * 分页获取文章列表
     */
    @GetMapping("/page")
    public ApiResponse<Page<KbArticle>> getArticlesPage(
            @RequestParam Long projectId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(kbArticleService.getArticlesPage(projectId, categoryId, keyword, page, size));
    }

    /**
     * 获取单篇文章详情
     */
    @GetMapping("/{id}")
    public ApiResponse<KbArticle> getArticle(@PathVariable Long id) {
        KbArticle article = kbArticleService.getArticleById(id);
        if (article == null) {
            return ApiResponse.error("文章不存在");
        }
        return ApiResponse.success(article);
    }

    /**
     * 根据分类获取文章
     */
    @GetMapping("/category/{categoryId}")
    public ApiResponse<List<KbArticle>> getArticlesByCategory(
            @PathVariable Long categoryId,
            @RequestParam Long projectId) {
        return ApiResponse.success(kbArticleService.getArticlesByCategory(projectId, categoryId));
    }

    /**
     * 搜索文章
     */
    @GetMapping("/search")
    public ApiResponse<List<KbArticle>> searchArticles(
            @RequestParam Long projectId,
            @RequestParam String keyword) {
        return ApiResponse.success(kbArticleService.searchArticles(projectId, keyword));
    }

    /**
     * 创建文章
     */
    @PostMapping
    public ApiResponse<KbArticle> createArticle(@RequestBody KbArticle article) {
        return ApiResponse.success(kbArticleService.createArticle(article));
    }

    /**
     * 更新文章
     */
    @PutMapping("/{id}")
    public ApiResponse<KbArticle> updateArticle(@PathVariable Long id, @RequestBody KbArticle article) {
        return ApiResponse.success(kbArticleService.updateArticle(id, article));
    }

    /**
     * 删除文章
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteArticle(@PathVariable Long id) {
        kbArticleService.deleteArticle(id);
        return ApiResponse.success(null);
    }

    /**
     * 切换文章发布状态
     */
    @PostMapping("/{id}/toggle-publish")
    public ApiResponse<KbArticle> togglePublish(@PathVariable Long id) {
        return ApiResponse.success(kbArticleService.togglePublish(id));
    }

    /**
     * 增加文章浏览次数
     */
    @PostMapping("/{id}/view")
    public ApiResponse<Void> incrementViewCount(@PathVariable Long id) {
        kbArticleService.incrementViewCount(id);
        return ApiResponse.success(null);
    }

    /**
     * 增加文章命中次数
     */
    @PostMapping("/{id}/hit")
    public ApiResponse<Void> incrementHitCount(@PathVariable Long id) {
        kbArticleService.incrementHitCount(id);
        return ApiResponse.success(null);
    }
}
