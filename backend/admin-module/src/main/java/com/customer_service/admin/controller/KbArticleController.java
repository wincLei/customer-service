package com.customer_service.admin.controller;

import com.customer_service.admin.service.KbArticleService;
import com.customer_service.shared.entity.KbArticle;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/kb/articles")
@RequiredArgsConstructor
public class KbArticleController {

    private final KbArticleService kbArticleService;

    /**
     * 获取已发布的文章列表
     */
    @GetMapping
    public List<KbArticle> getArticles(@RequestParam Long projectId) {
        return kbArticleService.getPublishedArticles(projectId);
    }

    /**
     * 根据分类获取文章
     */
    @GetMapping("/category/{categoryId}")
    public List<KbArticle> getArticlesByCategory(
            @PathVariable Long categoryId,
            @RequestParam Long projectId) {
        return kbArticleService.getArticlesByCategory(projectId, categoryId);
    }

    /**
     * 搜索文章
     */
    @GetMapping("/search")
    public List<KbArticle> searchArticles(
            @RequestParam Long projectId,
            @RequestParam String keyword) {
        return kbArticleService.searchArticles(projectId, keyword);
    }

    /**
     * 创建文章
     */
    @PostMapping
    public KbArticle createArticle(@RequestBody KbArticle article) {
        return kbArticleService.createArticle(article);
    }

    /**
     * 更新文章
     */
    @PutMapping("/{id}")
    public KbArticle updateArticle(@PathVariable Long id, @RequestBody KbArticle article) {
        return kbArticleService.updateArticle(id, article);
    }

    /**
     * 增加文章命中次数
     */
    @PostMapping("/{id}/hit")
    public void incrementHitCount(@PathVariable Long id) {
        kbArticleService.incrementHitCount(id);
    }
}
