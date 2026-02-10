package com.customer_service.portal.service;

import com.customer_service.shared.constant.IMConstants;
import com.customer_service.shared.constant.WKChannelType;
import com.customer_service.shared.dto.WKMessageNotify;
import com.customer_service.shared.entity.User;
import com.customer_service.shared.entity.UserConversation;
import com.customer_service.shared.repository.UserConversationRepository;
import com.customer_service.shared.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * WuKongIM Webhook 处理服务
 * 处理来自 WuKongIM 的各类事件通知
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final UserConversationRepository userConversationRepository;

    /**
     * 处理消息通知事件 (msg.notify)
     * WuKongIM 会将所有消息批量推送到此接口
     * 
     * @param body JSON 格式: [{"from_uid":"2_5","channel_id":"2_5",...}, ...]
     *             注意: 直接是数组格式，不是 {"messages": [...]}
     */
    @Transactional
    public void handleMessageNotify(String body) {
        try {
            log.info("handleMessageNotify: {}", body);

            // WuKongIM 发送的格式是直接的数组 [{...}, {...}]
            List<WKMessageNotify> messages = objectMapper.readValue(
                    body, new TypeReference<List<WKMessageNotify>>() {
                    });

            log.info("Processing {} messages from webhook", messages.size());

            for (WKMessageNotify msg : messages) {
                processMessage(msg);
            }
        } catch (Exception e) {
            log.error("Failed to parse message notify body", e);
        }
    }

    /**
     * 处理单条消息
     * 
     * from_uid 和 channel_id 格式都是 {projectId}_{userId}
     * 例如: "2_5" 表示 projectId=2, userId=5
     */
    private void processMessage(WKMessageNotify msg) {
        try {
            String fromUid = msg.getFromUid();
            String channelId = msg.getChannelId();
            Integer channelType = msg.getChannelType();

            log.debug("Processing message: fromUid={}, channelId={}, channelType={}",
                    fromUid, channelId, channelType);

            // 只处理访客频道消息 (channel_type = 10)
            if (channelType == null || channelType != WKChannelType.VISITOR) {
                log.debug("Skipping non-visitor channel message: channelType={}", channelType);
                return;
            }

            // 判断消息发送者是否为用户（非客服）
            // 客服 UID 格式: agent_{sysUserId}
            // 用户 UID 格式: {projectId}_{userId}
            boolean isFromUser = !fromUid.startsWith(IMConstants.AGENT_UID_PREFIX);

            // 只有用户发送的消息才需要更新 user_conversations
            if (!isFromUser) {
                log.debug("Skipping agent message: fromUid={}", fromUid);
                return;
            }

            // 解析 fromUid 获取 projectId 和 userId
            // 格式: {projectId}_{userId}
            String[] parts = fromUid.split("_");
            if (parts.length != 2) {
                log.warn("Invalid fromUid format: {}, expected {projectId}_{userId}", fromUid);
                return;
            }

            Long projectId;
            Long userId;
            try {
                projectId = Long.parseLong(parts[0]);
                userId = Long.parseLong(parts[1]);
            } catch (NumberFormatException e) {
                log.warn("Invalid numeric format in fromUid: {}", fromUid);
                return;
            }

            // 解析消息内容
            String messageContent = decodePayload(msg.getPayload());
            Long messageSeq = msg.getMessageSeq();
            Long timestamp = msg.getTimestamp();

            // 根据 userId 查找用户
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                log.warn("User not found for userId: {}", userId);
                return;
            }

            User user = userOpt.get();

            // 验证 projectId 是否匹配
            if (!user.getProjectId().equals(projectId)) {
                log.warn("ProjectId mismatch: fromUid projectId={}, user projectId={}",
                        projectId, user.getProjectId());
                return;
            }

            // 更新或创建 UserConversation
            updateUserConversation(user, messageContent, messageSeq, timestamp);

        } catch (Exception e) {
            log.error("Failed to process message: {}", msg, e);
        }
    }

    /**
     * 更新或创建用户会话记录
     */
    private void updateUserConversation(User user, String content, Long seq, Long timestamp) {
        OffsetDateTime messageTime = timestamp != null
                ? OffsetDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault())
                : OffsetDateTime.now();

        Optional<UserConversation> existingOpt = userConversationRepository.findByUserId(user.getId());

        if (existingOpt.isPresent()) {
            // 更新现有记录
            UserConversation existing = existingOpt.get();
            existing.setLastMessageContent(content != null ? content : "");
            existing.setLastMessageTime(messageTime);
            Long lastSeq = existing.getLastMessageSeq();
            // 只有当 seq > lastMessageSeq 时才加1，否则不变
            if (seq != null && (lastSeq == null || seq > lastSeq)) {
                existing.setUnreadCount(existing.getUnreadCount() + 1);
                existing.setLastMessageSeq(seq);
            }
            userConversationRepository.save(existing);
            log.info("Updated UserConversation for userId={}, unreadCount={}",
                    user.getId(), existing.getUnreadCount());
        } else {
            // 创建新记录
            UserConversation newConv = UserConversation.builder()
                    .projectId(user.getProjectId())
                    .userId(user.getId())
                    .lastMessageContent(content != null ? content : "")
                    .lastMessageTime(messageTime)
                    .lastMessageSeq(seq != null ? seq : 0L)
                    .unreadCount(1)
                    .build();
            userConversationRepository.save(newConv);
            log.info("Created new UserConversation for userId={}", user.getId());
        }
    }

    /**
     * 解码 Base64 编码的消息内容
     */
    private String decodePayload(String payload) {
        if (payload == null || payload.isEmpty()) {
            return "";
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(payload);
            String jsonStr = new String(decoded, StandardCharsets.UTF_8);

            // 尝试解析 JSON 获取 content 字段
            JsonNode node = objectMapper.readTree(jsonStr);
            if (node.has("content")) {
                return node.get("content").asText();
            }

            // 如果没有 content 字段，返回原始 JSON
            return jsonStr;
        } catch (Exception e) {
            log.warn("Failed to decode payload: {}", e.getMessage());
            return payload; // 返回原始值
        }
    }

    /**
     * 处理离线消息通知 (msg.offline)
     * 用于推送离线消息给用户
     */
    public void handleOfflineMessage(String body) {
        log.info("Received offline message notification");
        // TODO: 实现离线消息推送逻辑（调用推送服务）
    }

    /**
     * 处理用户在线状态变更 (user.onlinestatus)
     * 更新用户的最后活跃时间
     * 
     * 请求体格式: ["uid1-1-0-1001-2-4", "uid2-0-0-1001-1-2"]
     * 格式说明:
     * UserUID-DeviceFlag-OnlineStatus-ConnectionID-DeviceOnlineCount-UserTotalOnlineCount
     */
    @Transactional
    public void handleUserOnlineStatus(String body) {
        log.info("Received user online status change");
        try {
            // 解析 JSON 数组
            List<String> statusList = objectMapper.readValue(body, new TypeReference<List<String>>() {
            });

            for (String status : statusList) {
                processUserOnlineStatus(status);
            }
        } catch (Exception e) {
            log.error("Failed to parse user online status: {}", e.getMessage());
        }
    }

    /**
     * 处理单个用户的在线状态
     * 格式:
     * UserUID-DeviceFlag-OnlineStatus-ConnectionID-DeviceOnlineCount-UserTotalOnlineCount
     * 
     * UserUID 格式: {projectId}_{userId} 或 agent_{sysUserId}
     */
    private void processUserOnlineStatus(String statusStr) {
        try {
            String[] parts = statusStr.split("-");
            if (parts.length < 3) {
                log.warn("Invalid online status format: {}", statusStr);
                return;
            }

            String userUid = parts[0];
            int onlineStatus = Integer.parseInt(parts[2]); // 0=离线, 1=在线

            log.debug("Processing online status: uid={}, status={}", userUid, onlineStatus);

            // 跳过客服状态更新（客服 UID 格式: agent_{sysUserId}）
            if (userUid.startsWith(IMConstants.AGENT_UID_PREFIX)) {
                log.debug("Skipping agent online status: {}", userUid);
                return;
            }

            // 解析 userUid: {projectId}_{userId}
            String[] uidParts = userUid.split("_");
            if (uidParts.length != 2) {
                log.warn("Invalid userUid format: {}, expected {projectId}_{userId}", userUid);
                return;
            }

            Long userId;
            try {
                userId = Long.parseLong(uidParts[1]);
            } catch (NumberFormatException e) {
                log.warn("Invalid userId in userUid: {}", userUid);
                return;
            }

            // 不管是上线还是下线，都更新最后活跃时间
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setLastActiveAt(java.time.LocalDateTime.now());
                userRepository.save(user);
                log.info("Updated last_active_at for user: id={}, onlineStatus={}", userId, onlineStatus);
            } else {
                log.debug("User not found for online status update: userId={}", userId);
            }
        } catch (Exception e) {
            log.error("Failed to process user online status: {}", statusStr, e);
        }
    }
}
