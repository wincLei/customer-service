package com.customer_service.shared.repository;

import com.customer_service.shared.entity.SysMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysMenuRepository extends JpaRepository<SysMenu, Long> {

    Optional<SysMenu> findByCode(String code);

    boolean existsByCode(String code);

    List<SysMenu> findByParentIdIsNullOrderBySortOrderAsc();

    List<SysMenu> findByParentIdOrderBySortOrderAsc(Long parentId);

    List<SysMenu> findByIsEnabledTrueOrderBySortOrderAsc();

    @Query("SELECT m FROM SysMenu m WHERE m.isEnabled = true ORDER BY m.parentId NULLS FIRST, m.sortOrder ASC")
    List<SysMenu> findAllEnabledOrdered();

    @Query("SELECT m FROM SysMenu m ORDER BY m.parentId NULLS FIRST, m.sortOrder ASC")
    List<SysMenu> findAllOrdered();

    List<SysMenu> findByCodeIn(List<String> codes);

    @Query("SELECT m FROM SysMenu m WHERE m.type = :type AND m.isEnabled = true ORDER BY m.sortOrder ASC")
    List<SysMenu> findByTypeAndEnabled(String type);
}
