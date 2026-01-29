package com.customer_service.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 工单事件实体
 */
@Entity
@Table(name = "ticket_events", indexes = {
        @Index(name = "idx_ticket_events_ticket", columnList = "ticket_id,created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(name = "operator_type", length = 10)
    private String operatorType; // agent, system, user

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(nullable = false, length = 50)
    private String action; // create, reply, close, transfer, assign

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
