package com.customer_service.admin.repository;

import com.customer_service.shared.entity.KbCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KbCategoryRepository extends JpaRepository<KbCategory, Long> {

    /**
     * 获取项目下的所有分类，按排序字段排序
     */
    List<KbCategory> findByProjectIdOrderBySortOrderAsc(Long projectId);

    /**
     * 获取项目下的顶级分类（parentId 为 null）
     */
    List<KbCategory> findByProjectIdAndParentIdIsNullOrderBySortOrderAsc(Long projectId);

    /**
     * 获取某分类的子分类
     */
    List<KbCategory> findByParentIdOrderBySortOrderAsc(Long parentId);

    /**
     * 检查分类名称是否在同一项目下已存在
     */
    boolean existsByProjectIdAndNameAndIdNot(Long projectId, String name, Long id);

    /**
     * 检查分类名称是否在同一项目下已存在（新建时）
     */
    boolean existsByProjectIdAndName(Long projectId, String name);

    /**
     * 统计分类下的文章数量
     */
    @Query("SELECT COUNT(a) FROM KbArticle a WHERE a.categoryId = :categoryId")
    long countArticlesByCategory(Long categoryId);
}
