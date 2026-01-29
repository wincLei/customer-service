package com.customer_service.admin.service;

import com.customer_service.shared.constant.WKChannelType;
import com.customer_service.shared.repository.ConversationRepository;
import com.customer_service.shared.repository.MessageRepository;
import com.customer_service.shared.repository.UserRepository;
import com.customer_service.shared.entity.Conversation;
import com.customer_service.shared.entity.Message;
import com.customer_service.shared.entity.User;
import com.customer_service.shared.service.WuKongIMService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final WuKongIMService wuKongIMService;

    /**
     * 获取会话的所有消息
     */
    public List<Message> getConversationMessages(Long conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    /**
     * 分页获取会话消息（倒序，用于加载更多）
     */
    public Page<Message> getConversationMessagesPage(Long conversationId, int page, int size) {
        return messageRepository.findByConversationIdOrderByCreatedAtDesc(
                conversationId,
                PageRequest.of(page, size));
    }

    /**
     * 发送消息
     * 
     * 使用 WuKongIM Personal Channel (channel_type=1) 实现一对一通信：
     * - 客服发送消息给用户：channel_id = 用户的 UID
     */
    @Transactional
    public Message sendMessage(Long projectId, Long conversationId, Long senderId, String senderType,
            String contentType, String content) {
        // 创建消息内容JsonNode
        ObjectNode contentNode = objectMapper.createObjectNode();
        contentNode.put("text", content);
        contentNode.put("type", contentType);

        // 创建消息
        Message message = new Message();
        message.setProjectId(projectId);
        message.setConversationId(conversationId);
        message.setMsgId(UUID.randomUUID().toString());
        message.setSenderId(senderId);
        message.setSenderType(senderType);
        message.setMsgType(contentType);
        message.setContent(contentNode);
        message.setCreatedAt(LocalDateTime.now());

        message = messageRepository.save(message);

        // 更新会话的最后消息时间
        Optional<Conversation> convOpt = conversationRepository.findById(conversationId);
        String targetUserUid = null;

        if (convOpt.isPresent()) {
            Conversation conv = convOpt.get();
            conv.setLastMessageTime(LocalDateTime.now());
            conv.setLastMessageContent(content);
            conversationRepository.save(conv);

            // 获取会话对应用户的 UID
            Optional<User> userOpt = userRepository.findById(conv.getUserId());
            if (userOpt.isPresent()) {
                targetUserUid = userOpt.get().getUid();
            }
        }

        // 通过 WuKongIM 使用 Visitor Channel (channel_type=10) 推送消息给用户
        // 访客频道: channel_id = 用户UID，客服和用户都向此频道发送消息
        if (targetUserUid != null) {
            try {
                String fromUid = "agent_" + senderId;

                if ("text".equals(contentType)) {
                    boolean sent = wuKongIMService.sendTextMessage(fromUid, targetUserUid, WKChannelType.VISITOR,
                            content);
                    if (sent) {
                        log.info("IM message sent to visitor channel: from={} channelId={}", fromUid, targetUserUid);
                    } else {
                        log.warn("Failed to send IM message to visitor channel: from={} channelId={}", fromUid,
                                targetUserUid);
                    }
                } else if ("image".equals(contentType)) {
                    boolean sent = wuKongIMService.sendImageMessage(fromUid, targetUserUid, WKChannelType.VISITOR,
                            content);
                    if (sent) {
                        log.info("IM image sent to visitor channel: from={} channelId={}", fromUid, targetUserUid);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to send IM message: {}", e.getMessage());
                // IM 发送失败不影响消息保存
            }
        } else {
            log.warn("Cannot send IM message: user UID not found for conversation {}", conversationId);
        }

        return message;
    }
}
