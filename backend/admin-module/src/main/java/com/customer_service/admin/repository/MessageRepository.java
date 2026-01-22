package com.customer_service.admin.repository;

import com.customer_service.shared.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    Page<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM Message m JOIN Conversation c ON m.conversationId = c.id WHERE c.projectId = ?1 AND m.createdAt >= ?2")
    Long countByProjectIdAndCreatedAtAfter(Long projectId, LocalDateTime startTime);
}
