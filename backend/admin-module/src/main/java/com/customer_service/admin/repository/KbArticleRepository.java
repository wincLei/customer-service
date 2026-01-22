package com.customer_service.admin.repository;

import com.customer_service.shared.entity.KbArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KbArticleRepository extends JpaRepository<KbArticle, Long> {

    List<KbArticle> findByProjectIdAndIsPublishedOrderByCreatedAtDesc(Long projectId, Boolean isPublished);

    List<KbArticle> findByProjectIdAndCategoryIdAndIsPublished(Long projectId, Long categoryId, Boolean isPublished);

    List<KbArticle> findByProjectIdAndTitleContainingIgnoreCase(Long projectId, String keyword);
}
