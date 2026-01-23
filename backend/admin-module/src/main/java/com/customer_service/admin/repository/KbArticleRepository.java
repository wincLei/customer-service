package com.customer_service.admin.repository;

import com.customer_service.shared.entity.KbArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KbArticleRepository extends JpaRepository<KbArticle, Long> {

    List<KbArticle> findByProjectIdAndIsPublishedOrderByCreatedAtDesc(Long projectId, Boolean isPublished);

    List<KbArticle> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    List<KbArticle> findByProjectIdAndCategoryIdAndIsPublished(Long projectId, Long categoryId, Boolean isPublished);

    List<KbArticle> findByProjectIdAndTitleContainingIgnoreCase(Long projectId, String keyword);

    // 分页查询方法
    Page<KbArticle> findByProjectId(Long projectId, Pageable pageable);

    Page<KbArticle> findByProjectIdAndCategoryId(Long projectId, Long categoryId, Pageable pageable);

    Page<KbArticle> findByProjectIdAndTitleContainingIgnoreCase(Long projectId, String keyword, Pageable pageable);

    Page<KbArticle> findByProjectIdAndCategoryIdAndTitleContainingIgnoreCase(
            Long projectId, Long categoryId, String keyword, Pageable pageable);
}
