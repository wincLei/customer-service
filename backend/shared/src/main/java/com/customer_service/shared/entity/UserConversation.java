package com.customer_service.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 用户会话实体 - 用于客服端工作台展示用户列表
 * 记录每个用户的最新消息信息，用于渲染"我的会话"列表
 */
@Entity
@Table(name = "user_conversations", indexes = {
        @Index(name = "idx_last_time_id_updated", columnList = "project_id,last_message_time,id"),
        @Index(name = "idx_unread_id_updated", columnList = "project_id,unread_count,id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserConversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "unread_count", nullable = false)
    @Builder.Default
    private Integer unreadCount = 0;

    @Column(name = "last_message_seq", nullable = false)
    @Builder.Default
    private Long lastMessageSeq = 0L;

    @Column(name = "last_message_content", nullable = false, columnDefinition = "TEXT")
    private String lastMessageContent;

    @Column(name = "last_message_time", nullable = false)
    private OffsetDateTime lastMessageTime;

    @Column(name = "created_at")
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    // 关联用户实体（用于获取用户详细信息）
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
