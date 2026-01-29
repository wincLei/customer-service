package com.customer_service.shared.repository;

import com.customer_service.shared.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByProjectIdAndStatusOrderByLastMessageTimeDesc(Long projectId, String status);

    List<Conversation> findByAgentIdAndStatusOrderByLastMessageTimeDesc(Long agentId, String status);

    List<Conversation> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 查找用户的活跃会话（排队中或进行中）
     */
    @Query("SELECT c FROM Conversation c WHERE c.projectId = ?1 AND c.userId = ?2 AND c.status IN ('queued', 'active') ORDER BY c.createdAt DESC")
    Optional<Conversation> findActiveByProjectIdAndUserId(Long projectId, Long userId);

    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.projectId = ?1 AND c.status = 'queued'")
    Long countQueuedByProjectId(Long projectId);

    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.projectId = ?1 AND c.status = 'active'")
    Long countActiveByProjectId(Long projectId);

    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.projectId = ?1 AND c.createdAt >= ?2")
    Long countByProjectIdAndCreatedAtAfter(Long projectId, LocalDateTime startTime);

    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.agentId = ?1 AND c.status = 'active'")
    Long countActiveByAgentId(Long agentId);
}
