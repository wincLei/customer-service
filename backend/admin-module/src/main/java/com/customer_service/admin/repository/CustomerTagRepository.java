package com.customer_service.admin.repository;

import com.customer_service.shared.entity.CustomerTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 客户标签 Repository
 */
@Repository
public interface CustomerTagRepository extends JpaRepository<CustomerTag, Long> {

    /**
     * 根据项目ID获取所有标签
     */
    List<CustomerTag> findByProjectIdOrderBySortOrderAsc(Long projectId);

    /**
     * 根据项目ID和标签名称查找
     */
    Optional<CustomerTag> findByProjectIdAndName(Long projectId, String name);

    /**
     * 检查标签名称是否存在
     */
    boolean existsByProjectIdAndName(Long projectId, String name);

    /**
     * 统计项目下的标签数量
     */
    long countByProjectId(Long projectId);

    /**
     * 统计某标签关联的用户数量
     */
    @Query("SELECT COUNT(r) FROM UserTagRelation r WHERE r.tagId = :tagId")
    long countUsersByTagId(@Param("tagId") Long tagId);

    /**
     * 根据多个项目ID获取标签
     */
    List<CustomerTag> findByProjectIdInOrderBySortOrderAsc(List<Long> projectIds);
}
