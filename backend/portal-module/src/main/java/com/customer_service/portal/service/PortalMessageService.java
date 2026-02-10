package com.customer_service.portal.service;

import com.customer_service.shared.constant.MessageType;
import com.customer_service.shared.constant.OperatorType;
import com.customer_service.shared.constant.WKChannelType;
import com.customer_service.shared.entity.Message;
import com.customer_service.shared.repository.MessageRepository;
import com.customer_service.shared.service.WuKongIMService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Portal 消息服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortalMessageService {

    private final MessageRepository messageRepository;
    private final PortalConversationService conversationService;
    private final WuKongIMService wuKongIMService;
    private final ObjectMapper objectMapper;

    /**
     * 消息 DTO
     */
    public record MessageDTO(
            Long id,
            String msgId,
            String senderType,
            Long senderId,
            String msgType,
            String content,
            String fileName,
            java.time.LocalDateTime createdAt) {
        public static MessageDTO from(Message msg) {
            String content = "";
            String fileName = null;

            if (msg.getContent() != null) {
                if (msg.getContent().has("text")) {
                    content = msg.getContent().get("text").asText();
                } else if (msg.getContent().has("url")) {
                    content = msg.getContent().get("url").asText();
                    if (msg.getContent().has("fileName")) {
                        fileName = msg.getContent().get("fileName").asText();
                    }
                }
            }

            return new MessageDTO(
                    msg.getId(),
                    msg.getMsgId(),
                    msg.getSenderType(),
                    msg.getSenderId(),
                    msg.getMsgType(),
                    content,
                    fileName,
                    msg.getCreatedAt());
        }
    }

    /**
     * 获取会话消息历史
     */
    public List<MessageDTO> getConversationMessages(Long conversationId, int limit) {
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);

        // 限制返回数量
        if (messages.size() > limit) {
            messages = messages.subList(messages.size() - limit, messages.size());
        }

        return messages.stream().map(MessageDTO::from).toList();
    }

    /**
     * 发送消息
     * 
     * 使用 WuKongIM Visitor Channel (channel_type=10) 实现客服场景通信：
     * - 频道 ID = 用户 UID (格式: projectId_guestUid)
     * - 用户和客服都向用户的访客频道发送消息
     */
    @Transactional
    public MessageSendResult sendMessage(Long conversationId, Long userId, String userUid, String msgType,
            String content) {
        var convOpt = conversationService.getConversation(conversationId);
        if (convOpt.isEmpty()) {
            throw new IllegalArgumentException("会话不存在");
        }

        var conv = convOpt.get();

        // 构建消息内容 JSON
        ObjectNode contentNode = objectMapper.createObjectNode();
        if (MessageType.TEXT.equals(msgType)) {
            contentNode.put("text", content);
        } else if (MessageType.IMAGE.equals(msgType)) {
            contentNode.put("url", content);
            contentNode.put("type", MessageType.IMAGE);
        } else if (MessageType.FILE.equals(msgType)) {
            contentNode.put("url", content);
            contentNode.put("type", MessageType.FILE);
        }

        // 创建消息记录
        Message message = Message.builder()
                .projectId(conv.getProjectId())
                .conversationId(conversationId)
                .msgId(UUID.randomUUID().toString())
                .senderType(OperatorType.USER)
                .senderId(userId)
                .msgType(msgType)
                .content(contentNode)
                .isRevoked(false)
                .build();

        messageRepository.save(message);

        // 更新会话最后消息
        String lastContent = MessageType.TEXT.equals(msgType) ? content : "[" + msgType + "]";
        conversationService.updateLastMessage(conversationId, lastContent);

        log.info("Message saved: convId={}, userId={}, type={}", conversationId, userId, msgType);

        // 通过 WuKongIM 发送实时消息
        // 使用 Visitor Channel (channel_type=10)：
        // - from_uid = 用户 UID
        // - channel_id = 用户 UID (用户的访客频道)
        // - channel_type = 10
        String senderUid = userUid != null ? userUid : conversationService.getUserUid(userId);

        // 访客频道 ID = 用户 UID
        String visitorChannelId = senderUid;

        boolean messageSent = false;
        if (MessageType.TEXT.equals(msgType)) {
            messageSent = wuKongIMService.sendTextMessage(senderUid, visitorChannelId, WKChannelType.VISITOR, content);
        } else if (MessageType.IMAGE.equals(msgType)) {
            messageSent = wuKongIMService.sendImageMessage(senderUid, visitorChannelId, WKChannelType.VISITOR, content);
        }

        if (messageSent) {
            log.info("IM message sent to visitor channel: from={} channelId={} (Visitor Channel)", senderUid,
                    visitorChannelId);
        } else {
            log.warn("Failed to send IM message to visitor channel: from={} channelId={}", senderUid, visitorChannelId);
        }

        return new MessageSendResult(MessageDTO.from(message), messageSent);
    }

    /**
     * 向项目队列频道发送消息通知
     * /**
     * 发送消息结果
     */
    public record MessageSendResult(MessageDTO message, boolean channelCreated) {
    }
}
