package com.customer_service.portal.repository;

import com.customer_service.shared.entity.KbArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortalKbArticleRepository extends JpaRepository<KbArticle, Long> {

    /**
     * 获取项目下已发布的文章，按浏览量降序
     */
    List<KbArticle> findByProjectIdAndIsPublishedOrderByViewCountDesc(Long projectId, Boolean isPublished);

    /**
     * 获取项目下某分类的已发布文章
     */
    List<KbArticle> findByProjectIdAndCategoryIdAndIsPublishedOrderByCreatedAtDesc(Long projectId, Long categoryId,
            Boolean isPublished);

    /**
     * 搜索项目下已发布文章（标题模糊匹配）
     */
    List<KbArticle> findByProjectIdAndIsPublishedAndTitleContainingIgnoreCaseOrderByViewCountDesc(Long projectId,
            Boolean isPublished, String keyword);
}
