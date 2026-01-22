package com.customer_service.shared.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_tags", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "tag_name" }), indexes = {
        @Index(name = "idx_user_tags_project_tag", columnList = "project_id,tag_name")
})
public class UserTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "tag_name", nullable = false, length = 50)
    private String tagName;

    @Column(name = "tagged_by")
    private Long taggedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
