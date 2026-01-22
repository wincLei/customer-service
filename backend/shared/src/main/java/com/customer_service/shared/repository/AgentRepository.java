package com.customer_service.shared.repository;

import com.customer_service.shared.entity.Agent;
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
     * 根据项目ID和工作状态查找客服列表
     */
    List<Agent> findByProjectIdAndWorkStatusIn(Long projectId, List<String> workStatuses);

    /**
     * 根据项目ID查找所有客服
     */
    List<Agent> findByProjectId(Long projectId);

    /**
     * 根据项目ID和工作状态查找客服
     */
    List<Agent> findByProjectIdAndWorkStatus(Long projectId, String workStatus);

    /**
     * 通过关联的SysUser的用户名查找客服
     */
    @Query("SELECT a FROM Agent a JOIN a.sysUser u WHERE u.username = :username")
    Optional<Agent> findByUsername(@Param("username") String username);
}
