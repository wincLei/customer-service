package com.customer_service.shared.repository;

import com.customer_service.shared.entity.Agent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {

    /**
     * 根据关联的用户ID查找客服
     */
    Optional<Agent> findByUserId(Long userId);

    /**
     * 检查用户是否已有客服记录
     */
    boolean existsByUserId(Long userId);

    /**
     * 根据工作状态查找客服列表
     */
    List<Agent> findByWorkStatusIn(List<String> workStatuses);

    /**
     * 根据工作状态查找客服
     */
    List<Agent> findByWorkStatus(String workStatus);

    /**
     * 通过关联的SysUser的用户名查找客服
     */
    @Query("SELECT a FROM Agent a JOIN a.sysUser u WHERE u.username = :username")
    Optional<Agent> findByUsername(@Param("username") String username);

    /**
     * 分页查询所有客服（带用户信息）
     */
    @Query("SELECT a FROM Agent a JOIN FETCH a.sysUser")
    List<Agent> findAllWithUser();

    /**
     * 根据用户ID列表查找客服
     */
    @Query("SELECT a FROM Agent a WHERE a.userId IN :userIds")
    List<Agent> findByUserIdIn(@Param("userIds") List<Long> userIds);
}
