package com.customer_service.admin.dto;

import com.customer_service.shared.entity.Conversation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话 DTO - 包含用户 UID 等额外信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
    private Long id;
    private Long projectId;
    private Long userId;
    private String userUid; // 用户的 WuKongIM UID
    private String userName; // 用户昵称
    private Long agentId;
    private String status;
    private Integer priority;
    private String lastMessageContent;
    private LocalDateTime lastMessageTime;
    private LocalDateTime createdAt;

    /**
     * 从 Conversation 实体创建 DTO
     */
    public static ConversationDTO from(Conversation conversation, String userUid, String userName) {
        return ConversationDTO.builder()
                .id(conversation.getId())
                .projectId(conversation.getProjectId())
                .userId(conversation.getUserId())
                .userUid(userUid)
                .userName(userName)
                .agentId(conversation.getAgentId())
                .status(conversation.getStatus())
                .priority(conversation.getPriority())
                .lastMessageContent(conversation.getLastMessageContent())
                .lastMessageTime(conversation.getLastMessageTime())
                .createdAt(conversation.getCreatedAt())
                .build();
    }
}
