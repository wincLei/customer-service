package com.customer_service.shared.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户-项目关联实体
 * 用于控制客服账号能够访问哪些项目
 */
@Data
@Entity
@Table(name = "user_projects")
public class UserProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private SysUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public UserProject() {
    }

    public UserProject(SysUser user, Project project) {
        this.user = user;
        this.project = project;
    }
}
