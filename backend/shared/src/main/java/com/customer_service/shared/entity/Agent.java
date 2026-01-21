package com.customer_service.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "agents", indexes = {
    @Index(name = "idx_agents_project_username", columnList = "project_id,username"),
    @Index(name = "idx_agents_status", columnList = "project_id,status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Agent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(length = 50)
    private String nickname;

    @Column(length = 500)
    private String avatar;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String role; // agent, supervisor, admin

    @Column(length = 20)
    private String status; // offline, online, busy

    @Column(nullable = false)
    private Integer maxLoad; // 最大接待量

    @Column(nullable = false)
    private Integer currentLoad; // 当前接待量

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (maxLoad == null) {
            maxLoad = 5;
        }
        if (currentLoad == null) {
            currentLoad = 0;
        }
        if (status == null) {
            status = "offline";
        }
        if (role == null) {
            role = "agent";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
