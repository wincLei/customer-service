package com.customer_service.shared.repository;

import com.customer_service.shared.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /**
     * 按用户ID获取工单列表
     */
    List<Ticket> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 按项目ID分页获取工单
     */
    Page<Ticket> findByProjectIdOrderByCreatedAtDesc(Long projectId, Pageable pageable);

    /**
     * 按项目ID和状态分页获取工单
     */
    Page<Ticket> findByProjectIdAndStatusOrderByCreatedAtDesc(Long projectId, String status, Pageable pageable);

    /**
     * 按多个项目ID分页获取工单
     */
    Page<Ticket> findByProjectIdInOrderByCreatedAtDesc(List<Long> projectIds, Pageable pageable);

    /**
     * 按多个项目ID分页获取工单（Set版本）
     */
    Page<Ticket> findByProjectIdIn(Set<Long> projectIds, Pageable pageable);

    /**
     * 按指派人获取工单
     */
    Page<Ticket> findByAssigneeIdOrderByCreatedAtDesc(Long assigneeId, Pageable pageable);

    /**
     * 按状态分页获取工单
     */
    Page<Ticket> findByStatus(String status, Pageable pageable);

    /**
     * 搜索工单
     */
    @Query("SELECT t FROM Ticket t WHERE t.projectId = :projectId AND " +
            "(t.title LIKE %:keyword% OR t.description LIKE %:keyword%)")
    Page<Ticket> searchByProjectId(@Param("projectId") Long projectId,
            @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * 全局搜索工单
     */
    @Query("SELECT t FROM Ticket t WHERE t.title LIKE %:keyword% OR t.description LIKE %:keyword%")
    Page<Ticket> searchTickets(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 统计项目工单数量
     */
    Long countByProjectId(Long projectId);

    /**
     * 统计项目工单状态数量
     */
    Long countByProjectIdAndStatus(Long projectId, String status);

    /**
     * 按状态统计数量
     */
    Long countByStatus(String status);

    /**
     * 统计多项目工单数量
     */
    Long countByProjectIdIn(Set<Long> projectIds);

    /**
     * 统计多项目工单状态数量
     */
    Long countByProjectIdInAndStatus(Set<Long> projectIds, String status);
}
