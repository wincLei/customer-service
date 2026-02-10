package com.customer_service.admin.service;

import com.customer_service.admin.dto.ConversationDTO;
import com.customer_service.shared.constant.ConversationStatus;
import com.customer_service.shared.repository.MessageRepository;
import com.customer_service.shared.entity.Conversation;
import com.customer_service.shared.entity.User;
import com.customer_service.shared.repository.AgentRepository;
import com.customer_service.shared.repository.ConversationRepository;
import com.customer_service.shared.repository.UserRepository;
import com.customer_service.shared.service.WuKongIMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final AgentRepository agentRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final WuKongIMService wuKongIMService;

    /**
     * 获取排队中的会话列表（包含用户 UID）
     */
    public List<ConversationDTO> getQueuedConversations(Long projectId) {
        List<Conversation> conversations = conversationRepository
                .findByProjectIdAndStatusOrderByLastMessageTimeDesc(projectId, ConversationStatus.QUEUED);
        return enrichConversationsWithUserInfo(conversations);
    }

    /**
     * 获取指定客服的会话列表（包含用户 UID）
     */
    public List<ConversationDTO> getAgentConversations(Long agentId) {
        List<Conversation> conversations = conversationRepository
                .findByAgentIdAndStatusOrderByLastMessageTimeDesc(agentId, ConversationStatus.ACTIVE);
        return enrichConversationsWithUserInfo(conversations);
    }

    /**
     * 为会话列表添加用户信息
     */
    private List<ConversationDTO> enrichConversationsWithUserInfo(List<Conversation> conversations) {
        // 获取所有相关用户的 ID
        List<Long> userIds = conversations.stream()
                .map(Conversation::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询用户信息
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 转换为 DTO
        return conversations.stream()
                .map(conv -> {
                    User user = userMap.get(conv.getUserId());
                    String userUid = user != null ? user.getUid() : null;
                    String userName = user != null ? user.getNickname() : null;
                    return ConversationDTO.from(conv, userUid, userName);
                })
                .collect(Collectors.toList());
    }

    /**
     * 客服接入会话（从排队状态）
     * 使用 Personal Channel 模式，不需要订阅群组频道
     */
    @Transactional
    public Conversation acceptConversation(Long conversationId, Long agentId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("会话不存在"));

        if (!ConversationStatus.QUEUED.equals(conversation.getStatus())) {
            throw new RuntimeException("会话不在排队状态");
        }

        conversation.setAgentId(agentId);
        conversation.setStatus(ConversationStatus.ACTIVE);

        Conversation savedConversation = conversationRepository.save(conversation);

        log.info("Conversation {} accepted by agent {}", conversationId, agentId);

        return savedConversation;
    }

    /**
     * 结束会话
     */
    @Transactional
    public Conversation closeConversation(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("会话不存在"));

        conversation.setStatus(ConversationStatus.CLOSED);
        Conversation savedConversation = conversationRepository.save(conversation);

        log.info("Conversation {} closed", conversationId);

        return savedConversation;
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
