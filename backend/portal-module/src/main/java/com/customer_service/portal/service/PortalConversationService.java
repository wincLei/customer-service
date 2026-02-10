package com.customer_service.portal.service;

import com.customer_service.shared.constant.AppDefaults;
import com.customer_service.shared.constant.ConversationStatus;
import com.customer_service.shared.constant.WKChannelType;
import com.customer_service.shared.entity.Conversation;
import com.customer_service.shared.entity.Project;
import com.customer_service.shared.repository.ConversationRepository;
import com.customer_service.shared.repository.ProjectRepository;
import com.customer_service.shared.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Portal 会话服务
 * 使用 WuKongIM Visitor Channel (channel_type=10) 实现客服场景
 * 访客频道在用户初始化时异步创建，此服务仅处理会话逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortalConversationService {

    private final ConversationRepository conversationRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    /**
     * 会话初始化结果
     */
    public record ConversationInitResult(
            Conversation conversation,
            boolean isNew,
            String welcomeMessage) {
    }

    /**
     * 初始化或获取用户的活跃会话
     * 注意：访客频道在用户初始化时已异步创建，此处仅处理会话逻辑
     */
    @Transactional
    public ConversationInitResult initOrGetConversation(Long projectId, Long userId) {
        // 获取项目欢迎语（无论会话是否已存在，都需要返回）
        String welcomeMessage = AppDefaults.DEFAULT_WELCOME_MESSAGE;
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isPresent() && projectOpt.get().getConfig() != null) {
            try {
                var config = projectOpt.get().getConfig();
                if (config.has("welcomeMessage")) {
                    welcomeMessage = config.get("welcomeMessage").asText();
                }
            } catch (Exception e) {
                log.warn("Failed to parse project config", e);
            }
        }

        // 查找用户现有的活跃会话
        Optional<Conversation> existingConv = conversationRepository
                .findActiveByProjectIdAndUserId(projectId, userId);

        if (existingConv.isPresent()) {
            Conversation conv = existingConv.get();
            log.info("Found existing conversation: id={}", conv.getId());
            return new ConversationInitResult(conv, false, welcomeMessage);
        }

        // 创建新会话
        Conversation newConv = Conversation.builder()
                .projectId(projectId)
                .userId(userId)
                .status(ConversationStatus.QUEUED)
                .priority(AppDefaults.DEFAULT_PRIORITY)
                .build();

        conversationRepository.save(newConv);
        log.info("Created new conversation: id={}, projectId={}, userId={}",
                newConv.getId(), projectId, userId);

        // 访客频道已在用户初始化时异步创建，无需在此重复创建

        return new ConversationInitResult(newConv, true, welcomeMessage);
    }

    /**
     * 获取会话
     */
    public Optional<Conversation> getConversation(Long conversationId) {
        return conversationRepository.findById(conversationId);
    }

    /**
     * 获取用户的 UID（用于 IM 通信）
     */
    public String getUserUid(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getUid() != null ? user.getUid() : "user_" + userId)
                .orElse("user_" + userId);
    }

    /**
     * 更新会话最后消息
     */
    @Transactional
    public void updateLastMessage(Long conversationId, String content) {
        conversationRepository.findById(conversationId).ifPresent(conv -> {
            conv.setLastMessageContent(content);
            conv.setLastMessageTime(LocalDateTime.now());
            conversationRepository.save(conv);
        });
    }

    /**
     * 结束会话
     */
    @Transactional
    public void endConversation(Long conversationId) {
        conversationRepository.findById(conversationId).ifPresent(conv -> {
            conv.setStatus(ConversationStatus.CLOSED);
            conv.setEndedAt(LocalDateTime.now());
            conversationRepository.save(conv);
            log.info("Conversation ended: id={}", conversationId);
        });
    }
}
