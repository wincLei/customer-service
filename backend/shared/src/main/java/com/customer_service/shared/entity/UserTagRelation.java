package com.customer_service.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户标签关联实体
 * 多对多关系：一个用户可以有多个标签，一个标签可以关联多个用户
 */
@Entity
@Table(name = "user_tag_relations", indexes = {
        @Index(name = "idx_user_tag_relations_user", columnList = "user_id"),
        @Index(name = "idx_user_tag_relations_tag", columnList = "tag_id")
}, uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "tag_id" })
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTagRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "tag_id", nullable = false)
    private Long tagId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", insertable = false, updatable = false)
    private CustomerTag tag;

    @Column(name = "tagged_by")
    private Long taggedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tagged_by", insertable = false, updatable = false)
    private SysUser tagger;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
