package com.customer_service.admin.service;

import com.customer_service.admin.repository.ConversationRepository;
import com.customer_service.admin.repository.MessageRepository;
import com.customer_service.shared.entity.Conversation;
import com.customer_service.shared.repository.AgentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final AgentRepository agentRepository;
    private final MessageRepository messageRepository;

    /**
     * 获取排队中的会话列表
     */
    public List<Conversation> getQueuedConversations(Long projectId) {
        return conversationRepository.findByProjectIdAndStatusOrderByLastMessageTimeDesc(projectId, "queued");
    }

    /**
     * 获取指定客服的会话列表
     */
    public List<Conversation> getAgentConversations(Long agentId) {
        return conversationRepository.findByAgentIdAndStatusOrderByLastMessageTimeDesc(agentId, "active");
    }

    /**
     * 客服接入会话（从排队状态）
     */
    @Transactional
    public Conversation acceptConversation(Long conversationId, Long agentId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("会话不存在"));

        if (!"queued".equals(conversation.getStatus())) {
            throw new RuntimeException("会话不在排队状态");
        }

        conversation.setAgentId(agentId);
        conversation.setStatus("active");

        return conversationRepository.save(conversation);
    }

    /**
     * 结束会话
     */
    @Transactional
    public Conversation closeConversation(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("会话不存在"));

        conversation.setStatus("closed");

        return conversationRepository.save(conversation);
    }

    /**
     * 获取排队数量
     */
    public Long getQueueCount(Long projectId) {
        return conversationRepository.countQueuedByProjectId(projectId);
    }

    /**
     * 获取统计数据
     */
    public Map<String, Object> getStatistics(Long projectId) {
        Map<String, Object> stats = new HashMap<>();

        // 排队数量
        Long queueCount = conversationRepository.countQueuedByProjectId(projectId);
        stats.put("queueCount", queueCount);

        // 活跃会话数
        Long activeCount = conversationRepository.countActiveByProjectId(projectId);
        stats.put("activeConversations", activeCount);

        // 今日会话数（从今天0点开始）
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        Long todayConversations = conversationRepository.countByProjectIdAndCreatedAtAfter(projectId, todayStart);
        stats.put("todayConversations", todayConversations);

        // 今日消息数
        Long todayMessages = messageRepository.countByProjectIdAndCreatedAtAfter(projectId, todayStart);
        stats.put("todayMessages", todayMessages);

        return stats;
    }
}
