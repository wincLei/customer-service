package com.customer_service.admin.repository;

import com.customer_service.shared.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByProjectIdAndStatus(Long projectId, String status);
    
    List<Conversation> findByAgentIdAndStatus(Long agentId, String status);
    
    List<Conversation> findByUserIdOrderByCreatedAtDesc(Long userId);
}
