package com.customer_service.portal.repository;

import com.customer_service.shared.entity.KbCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortalKbCategoryRepository extends JpaRepository<KbCategory, Long> {

    /**
     * 获取项目下的所有分类，按排序字段排序
     */
    List<KbCategory> findByProjectIdOrderBySortOrderAsc(Long projectId);
}
