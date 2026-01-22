package com.customer_service.shared.repository;

import com.customer_service.shared.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByAppKey(String appKey);

    /**
     * 根据项目名称或AppKey搜索（分页）
     */
    @Query("SELECT p FROM Project p WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.appKey) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Project> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
