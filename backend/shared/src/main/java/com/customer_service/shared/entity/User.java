package com.customer_service.shared.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * 客户端用户实体（访客/客户）
 * 区别于 SysUser（系统管理用户），User 是使用客服系统的终端用户
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_project_phone", columnList = "project_id,phone"),
        @Index(name = "idx_users_project_uid", columnList = "project_id,uid"),
        @Index(name = "idx_users_project_external_uid", columnList = "project_id,external_uid"),
        @Index(name = "idx_users_is_guest", columnList = "project_id,is_guest")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(nullable = false, length = 100)
    private String uid; // 系统内部唯一ID（游客ID或外部系统ID）

    @Column(name = "external_uid", length = 100)
    private String externalUid; // 外部系统的唯一ID（可为空，表示游客）

    @Column(name = "is_guest")
    @Builder.Default
    private Boolean isGuest = true; // 是否游客

    @Column(length = 100)
    private String nickname;

    @Column(length = 500)
    private String avatar;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "device_type", length = 20)
    private String deviceType; // pc, mobile, tablet

    @Column(name = "source_url", columnDefinition = "TEXT")
    private String sourceUrl; // 来源页面

    @Column(name = "open_id", length = 100)
    private String openId; // 微信/第三方 OpenID

    @Column(length = 50)
    private String city; // IP 解析城市

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extra_info", columnDefinition = "jsonb")
    private JsonNode extraInfo; // 存储其他自定义参数

    @Column(name = "merged_from_id")
    private Long mergedFromId; // 合并来源用户ID（用于标记已被合并的用户）

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastActiveAt = LocalDateTime.now();
        if (isGuest == null) {
            isGuest = true;
        }
    }
}
