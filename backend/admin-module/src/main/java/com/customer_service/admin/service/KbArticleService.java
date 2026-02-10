package com.customer_service.admin.service;

import com.customer_service.admin.repository.KbArticleRepository;
import com.customer_service.shared.entity.KbArticle;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import com.customer_service.shared.util.I18nUtil;

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
     * 获取多个项目的已发布知识库文章列表
     */
    public List<KbArticle> getPublishedArticles(List<Long> projectIds) {
        return kbArticleRepository.findByProjectIdInAndIsPublishedOrderByCreatedAtDesc(projectIds, true);
    }

    /**
     * 获取所有文章（包含未发布）- 管理员用
     */
    public List<KbArticle> getAllArticles(Long projectId) {
        return kbArticleRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
    }

    /**
     * 分页获取文章列表
     */
    public Page<KbArticle> getArticlesPage(Long projectId, Long categoryId, String keyword, int page, int size) {
        // 前端页码从1开始，Spring Data 从0开始
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (categoryId != null && keyword != null && !keyword.isEmpty()) {
            return kbArticleRepository.findByProjectIdAndCategoryIdAndTitleContainingIgnoreCase(
                    projectId, categoryId, keyword, pageable);
        } else if (categoryId != null) {
            return kbArticleRepository.findByProjectIdAndCategoryId(projectId, categoryId, pageable);
        } else if (keyword != null && !keyword.isEmpty()) {
            return kbArticleRepository.findByProjectIdAndTitleContainingIgnoreCase(projectId, keyword, pageable);
        } else {
            return kbArticleRepository.findByProjectId(projectId, pageable);
        }
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
     * 根据ID获取文章
     */
    public KbArticle getArticleById(Long id) {
        return kbArticleRepository.findById(id).orElse(null);
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
                .orElseThrow(() -> new RuntimeException(I18nUtil.getMessage("kb.article.not.found")));

        article.setTitle(updates.getTitle());
        article.setContent(updates.getContent());
        article.setCategoryId(updates.getCategoryId());
        article.setIsPublished(updates.getIsPublished());
        article.setUpdatedAt(LocalDateTime.now());

        return kbArticleRepository.save(article);
    }

    /**
     * 删除文章
     */
    @Transactional
    public void deleteArticle(Long articleId) {
        if (!kbArticleRepository.existsById(articleId)) {
            throw new RuntimeException(I18nUtil.getMessage("kb.article.not.found"));
        }
        kbArticleRepository.deleteById(articleId);
    }

    /**
     * 切换文章发布状态
     */
    @Transactional
    public KbArticle togglePublish(Long articleId) {
        KbArticle article = kbArticleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException(I18nUtil.getMessage("kb.article.not.found")));
        article.setIsPublished(!article.getIsPublished());
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

    /**
     * 增加文章浏览次数
     */
    @Transactional
    public void incrementViewCount(Long articleId) {
        kbArticleRepository.findById(articleId).ifPresent(article -> {
            article.setViewCount(article.getViewCount() + 1);
            kbArticleRepository.save(article);
        });
    }
}
