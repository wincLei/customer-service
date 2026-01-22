package com.customer_service.shared.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 客服/坐席实体
 * 只包含客服业务相关信息，登录认证信息在SysUser中
 */
@Entity
@Table(name = "agents", indexes = {
        @Index(name = "idx_agents_project", columnList = "project_id"),
        @Index(name = "idx_agents_user", columnList = "user_id"),
        @Index(name = "idx_agents_status", columnList = "project_id,work_status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Agent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private SysUser sysUser;

    @Column(length = 50)
    private String nickname;

    @Column(name = "work_status", length = 20)
    private String workStatus; // offline, online, busy

    @Column(name = "max_load", nullable = false)
    private Integer maxLoad; // 最大接待量

    @Column(name = "current_load", nullable = false)
    private Integer currentLoad; // 当前接待量

    @Column(name = "skill_groups", columnDefinition = "JSONB DEFAULT '[]'")
    private String skillGroups;

    @Column(name = "welcome_message", columnDefinition = "TEXT")
    private String welcomeMessage;

    @Column(name = "auto_reply_enabled")
    private Boolean autoReplyEnabled;

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
        if (workStatus == null) {
            workStatus = "offline";
        }
        if (autoReplyEnabled == null) {
            autoReplyEnabled = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 便捷方法：获取用户名（从关联的SysUser）
    public String getUsername() {
        return sysUser != null ? sysUser.getUsername() : null;
    }

    // 便捷方法：获取头像（从关联的SysUser）
    public String getAvatar() {
        return sysUser != null ? sysUser.getAvatar() : null;
    }

    // 便捷方法：获取邮箱（从关联的SysUser）
    public String getEmail() {
        return sysUser != null ? sysUser.getEmail() : null;
    }

    // 兼容旧代码：获取status（映射到workStatus）
    public String getStatus() {
        return workStatus;
    }

    // 兼容旧代码：设置status（映射到workStatus）
    public void setStatus(String status) {
        this.workStatus = status;
    }
}
