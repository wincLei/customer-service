package com.customer_service.admin.service;

import com.customer_service.admin.repository.KbArticleRepository;
import com.customer_service.shared.entity.KbArticle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KbArticleService {

    private final KbArticleRepository kbArticleRepository;

    /**
     * 获取已发布的知识库文章列表
     */
    public List<KbArticle> getPublishedArticles(Long projectId) {
        return kbArticleRepository.findByProjectIdAndIsPublishedOrderByCreatedAtDesc(projectId, true);
    }

    /**
     * 根据分类获取文章
     */
    public List<KbArticle> getArticlesByCategory(Long projectId, Long categoryId) {
        return kbArticleRepository.findByProjectIdAndCategoryIdAndIsPublished(projectId, categoryId, true);
    }

    /**
     * 搜索文章
     */
    public List<KbArticle> searchArticles(Long projectId, String keyword) {
        return kbArticleRepository.findByProjectIdAndTitleContainingIgnoreCase(projectId, keyword);
    }

    /**
     * 创建文章
     */
    @Transactional
    public KbArticle createArticle(KbArticle article) {
        article.setViewCount(0);
        article.setHitCount(0);
        article.setCreatedAt(LocalDateTime.now());
        article.setUpdatedAt(LocalDateTime.now());
        return kbArticleRepository.save(article);
    }

    /**
     * 更新文章
     */
    @Transactional
    public KbArticle updateArticle(Long articleId, KbArticle updates) {
        KbArticle article = kbArticleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("文章不存在"));

        article.setTitle(updates.getTitle());
        article.setContent(updates.getContent());
        article.setCategoryId(updates.getCategoryId());
        article.setIsPublished(updates.getIsPublished());
        article.setUpdatedAt(LocalDateTime.now());

        return kbArticleRepository.save(article);
    }

    /**
     * 增加文章命中次数（用于统计）
     */
    @Transactional
    public void incrementHitCount(Long articleId) {
        kbArticleRepository.findById(articleId).ifPresent(article -> {
            article.setHitCount(article.getHitCount() + 1);
            kbArticleRepository.save(article);
        });
    }
}
