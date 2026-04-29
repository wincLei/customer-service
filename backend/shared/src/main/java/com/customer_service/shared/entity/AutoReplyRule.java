package com.customer_service.shared.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 自动回复规则
 * 当用户消息包含指定关键词时，系统自动回复配置内容
 */
@Data
@Entity
@Table(name = "auto_reply_rules", indexes = {
        @Index(name = "idx_auto_reply_rules_project", columnList = "project_id"),
        @Index(name = "idx_auto_reply_rules_enabled", columnList = "project_id,enabled")
})
public class AutoReplyRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    /**
     * 规则名称（便于管理识别）
     */
    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    /**
     * 关键词列表，多个关键词用英文逗号分隔，任一匹配即触发
     * 例如: "提现,到账,转账"
     */
    @Column(nullable = false, length = 500)
    private String keywords;

    /**
     * 自动回复内容
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String replyContent;

    /**
     * 是否启用
     */
    @Column(nullable = false)
    private Boolean enabled = true;

    /**
     * 优先级，数字越小优先级越高（多规则匹配时取优先级最高的）
     */
    @Column(nullable = false)
    private Integer priority = 100;

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
