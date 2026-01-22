package com.customer_service.admin.service;

import com.customer_service.admin.repository.ConversationRepository;
import com.customer_service.admin.repository.MessageRepository;
import com.customer_service.shared.entity.Conversation;
import com.customer_service.shared.entity.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ObjectMapper objectMapper;

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
        conversationRepository.findById(conversationId).ifPresent(conv -> {
            conv.setLastMessageTime(LocalDateTime.now());
            conv.setLastMessageContent(content);
            conversationRepository.save(conv);
        });

        return message;
    }
}
