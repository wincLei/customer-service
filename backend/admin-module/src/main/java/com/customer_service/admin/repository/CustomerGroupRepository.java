package com.customer_service.admin.repository;

import com.customer_service.shared.entity.CustomerGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 客户分组 Repository
 */
@Repository
public interface CustomerGroupRepository extends JpaRepository<CustomerGroup, Long> {

    /**
     * 根据项目ID获取所有分组
     */
    List<CustomerGroup> findByProjectIdOrderBySortOrderAsc(Long projectId);

    /**
     * 根据项目ID和分组名称查找
     */
    Optional<CustomerGroup> findByProjectIdAndName(Long projectId, String name);

    /**
     * 检查分组名称是否存在
     */
    boolean existsByProjectIdAndName(Long projectId, String name);

    /**
     * 统计项目下的分组数量
     */
    long countByProjectId(Long projectId);

    /**
     * 获取系统内置分组
     */
    List<CustomerGroup> findByProjectIdAndIsSystemTrueOrderBySortOrderAsc(Long projectId);
}
