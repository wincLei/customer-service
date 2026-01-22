package com.customer_service.shared.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统菜单实体
 * 支持多级菜单结构
 */
@Data
@Entity
@Table(name = "sys_menus")
public class SysMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 菜单编码（唯一标识，用于前端权限判断）
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * 菜单名称
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 菜单类型：menu-菜单, button-按钮/操作
     */
    @Column(length = 20)
    private String type = "menu";

    /**
     * 父级菜单ID，null表示顶级菜单
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 菜单路径（前端路由）
     */
    @Column(length = 200)
    private String path;

    /**
     * 图标
     */
    @Column(length = 100)
    private String icon;

    /**
     * 排序号（越小越靠前）
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * 是否启用
     */
    @Column(name = "is_enabled")
    private Boolean isEnabled = true;

    /**
     * 描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
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
