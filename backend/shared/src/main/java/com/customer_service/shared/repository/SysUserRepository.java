package com.customer_service.shared.repository;

import com.customer_service.shared.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysUserRepository extends JpaRepository<SysUser, Long>, JpaSpecificationExecutor<SysUser> {

    Optional<SysUser> findByUsername(String username);

    @Query("SELECT u FROM SysUser u JOIN u.role r WHERE r.code = ?1")
    List<SysUser> findByRoleCode(String roleCode);

    boolean existsByUsername(String username);
}
