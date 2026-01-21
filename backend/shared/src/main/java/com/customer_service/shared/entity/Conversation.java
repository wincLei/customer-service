package com.customer_service.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversations", indexes = {
    @Index(name = "idx_conv_project_status", columnList = "project_id,status"),
    @Index(name = "idx_conv_agent_active", columnList = "agent_id,status"),
    @Index(name = "idx_conv_user_history", columnList = "user_id,created_at"),
    @Index(name = "idx_conv_updated", columnList = "project_id,last_message_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long userId;

    @Column
    private Long agentId; // 当前接待客服，为空则未分配

    @Column(nullable = false, length = 20)
    private String status; // queued, active, closed

    @Column(nullable = false)
    private Integer priority; // 优先级

    @Column
    private Integer score; // 评分 (1-5)

    @Column(columnDefinition = "TEXT")
    private String scoreRemark; // 评价内容

    @Column(columnDefinition = "TEXT")
    private String lastMessageContent; // 冗余字段，用于列表展示

    @Column(name = "last_message_time")
    private LocalDateTime lastMessageTime; // 用于列表排序

    @Column(name = "started_at")
    private LocalDateTime startedAt; // 客服接入时间

    @Column(name = "ended_at")
    private LocalDateTime endedAt; // 结束时间

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastMessageTime = LocalDateTime.now();
        if (status == null) {
            status = "queued";
        }
        if (priority == null) {
            priority = 0;
        }
    }
}
