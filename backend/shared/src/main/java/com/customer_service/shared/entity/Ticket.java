package com.customer_service.shared.entity;

import com.customer_service.shared.constant.TicketPriority;
import com.customer_service.shared.constant.TicketStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 工单实体
 */
@Entity
@Table(name = "tickets", indexes = {
        @Index(name = "idx_tickets_project_status", columnList = "project_id,status"),
        @Index(name = "idx_tickets_user", columnList = "user_id"),
        @Index(name = "idx_tickets_assignee", columnList = "assignee_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "creator_type", nullable = false, length = 10)
    private String creatorType; // user, agent

    @Column(name = "assignee_id")
    private Long assigneeId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 10)
    @Builder.Default
    private String priority = TicketPriority.MEDIUM; // low, medium, high, urgent

    @Column(length = 20)
    @Builder.Default
    private String status = TicketStatus.OPEN; // open, processing, resolved, closed

    @Column(name = "contact_info", length = 100)
    private String contactInfo;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
