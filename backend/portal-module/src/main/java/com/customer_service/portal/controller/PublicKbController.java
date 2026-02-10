package com.customer_service.portal.controller;

import com.customer_service.portal.repository.PortalKbArticleRepository;
import com.customer_service.portal.repository.PortalKbCategoryRepository;
import com.customer_service.shared.dto.ApiResponse;
import com.customer_service.shared.util.I18nUtil;
import com.customer_service.shared.entity.KbArticle;
import com.customer_service.shared.entity.KbCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 公开知识库接口 - 无需认证
 * 用于门户端帮助中心展示
 */
@Slf4j
@RestController
@RequestMapping("/api/pub/kb")
@RequiredArgsConstructor
public class PublicKbController {

    private final PortalKbArticleRepository articleRepository;
    private final PortalKbCategoryRepository categoryRepository;

    /**
     * 获取项目下已发布的知识库文章（按分类分组）
     */
    @GetMapping("/articles")
    public ApiResponse<?> getArticles(@RequestParam Long projectId) {
        log.info("Public KB: get articles for project {}", projectId);

        // 获取所有已发布文章
        List<KbArticle> articles = articleRepository
                .findByProjectIdAndIsPublishedOrderByViewCountDesc(projectId, true);

        // 获取所有分类
        List<KbCategory> categories = categoryRepository
                .findByProjectIdOrderBySortOrderAsc(projectId);

        // 构建分类映射
        Map<Long, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(KbCategory::getId, KbCategory::getName));

        // 热门文章（取浏览量前6）
        List<Map<String, Object>> hotArticles = articles.stream()
                .limit(6)
                .map(this::toArticleSummary)
                .collect(Collectors.toList());

        // 按分类分组
        List<Map<String, Object>> categoryGroups = new ArrayList<>();

        // 先处理有分类的文章
        Map<Long, List<KbArticle>> grouped = articles.stream()
                .filter(a -> a.getCategoryId() != null)
                .collect(Collectors.groupingBy(KbArticle::getCategoryId));

        for (KbCategory cat : categories) {
            List<KbArticle> catArticles = grouped.getOrDefault(cat.getId(), Collections.emptyList());
            if (!catArticles.isEmpty()) {
                Map<String, Object> group = new LinkedHashMap<>();
                group.put("id", cat.getId());
                group.put("name", cat.getName());
                group.put("articles", catArticles.stream()
                        .map(this::toArticleSummary)
                        .collect(Collectors.toList()));
                categoryGroups.add(group);
            }
        }

        // 处理未分类的文章
        List<KbArticle> uncategorized = articles.stream()
                .filter(a -> a.getCategoryId() == null)
                .collect(Collectors.toList());
        if (!uncategorized.isEmpty()) {
            Map<String, Object> group = new LinkedHashMap<>();
            group.put("id", 0);
            group.put("name", I18nUtil.getMessage("kb.category.default"));
            group.put("articles", uncategorized.stream()
                    .map(this::toArticleSummary)
                    .collect(Collectors.toList()));
            categoryGroups.add(group);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("hotArticles", hotArticles);
        result.put("categories", categoryGroups);

        return ApiResponse.success(result);
    }

    /**
     * 获取文章详情
     */
    @GetMapping("/articles/{id}")
    public ApiResponse<?> getArticleDetail(@PathVariable Long id) {
        log.info("Public KB: get article detail {}", id);

        return articleRepository.findById(id)
                .filter(KbArticle::getIsPublished)
                .map(article -> {
                    // 增加浏览次数
                    article.setViewCount(article.getViewCount() + 1);
                    articleRepository.save(article);

                    Map<String, Object> detail = new LinkedHashMap<>();
                    detail.put("id", article.getId());
                    detail.put("title", article.getTitle());
                    detail.put("content", article.getContent());
                    detail.put("viewCount", article.getViewCount());
                    detail.put("createdAt", article.getCreatedAt());
                    return ApiResponse.success(detail);
                })
                .orElse(ApiResponse.fail(404, I18nUtil.getMessage("kb.article.not.published")));
    }

    /**
     * 搜索知识库文章
     */
    @GetMapping("/articles/search")
    public ApiResponse<?> searchArticles(
            @RequestParam Long projectId,
            @RequestParam String keyword) {
        log.info("Public KB: search articles for project {} with keyword '{}'", projectId, keyword);

        List<KbArticle> articles = articleRepository
                .findByProjectIdAndIsPublishedAndTitleContainingIgnoreCaseOrderByViewCountDesc(
                        projectId, true, keyword);

        List<Map<String, Object>> result = articles.stream()
                .map(this::toArticleSummary)
                .collect(Collectors.toList());

        return ApiResponse.success(result);
    }

    private Map<String, Object> toArticleSummary(KbArticle article) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", article.getId());
        map.put("title", article.getTitle());
        map.put("excerpt", truncateContent(article.getContent(), 80));
        map.put("viewCount", article.getViewCount());
        map.put("createdAt", article.getCreatedAt());
        return map;
    }

    private String truncateContent(String content, int maxLength) {
        if (content == null)
            return "";
        // 去除 HTML 标签
        String text = content.replaceAll("<[^>]+>", "").trim();
        if (text.length() <= maxLength)
            return text;
        return text.substring(0, maxLength) + "...";
    }
}
