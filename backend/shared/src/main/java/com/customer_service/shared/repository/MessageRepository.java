package com.customer_service.shared.repository;

import com.customer_service.shared.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * 按会话ID获取消息，按时间升序
     */
    List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    /**
     * 按会话ID获取消息，按时间降序
     */
    List<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId);

    /**
     * 分页获取会话消息
     */
    Page<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);

    /**
     * 按消息ID查找
     */
    Optional<Message> findByMsgId(String msgId);

    /**
     * 统计会话消息数量
     */
    Long countByConversationId(Long conversationId);

    /**
     * 检查会话中是否存在指定发送者类型的消息
     */
    boolean existsByConversationIdAndSenderType(Long conversationId, String senderType);

    /**
     * 统计项目的消息数量（从指定时间开始）
     */
    @Query("SELECT COUNT(m) FROM Message m JOIN Conversation c ON m.conversationId = c.id WHERE c.projectId = ?1 AND m.createdAt >= ?2")
    Long countByProjectIdAndCreatedAtAfter(Long projectId, LocalDateTime startTime);
}
