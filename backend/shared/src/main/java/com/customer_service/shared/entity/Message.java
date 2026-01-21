package com.customer_service.shared.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_messages_conv_time", columnList = "conversation_id,created_at"),
    @Index(name = "idx_messages_msg_id", columnList = "msg_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long conversationId;

    @Column(nullable = false, length = 64)
    private String msgId; // WuKongIM 的消息ID

    @Column(nullable = false, length = 10)
    private String senderType; // user, agent, system

    @Column(nullable = false)
    private Long senderId; // user_id 或 agent_id

    @Column(nullable = false, length = 20)
    private String msgType; // text, image, file, voice, rich

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private JsonNode content; // 消息内容结构体

    @Column(nullable = false)
    private Boolean isRevoked; // 是否撤回

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isRevoked == null) {
            isRevoked = false;
        }
    }
}
