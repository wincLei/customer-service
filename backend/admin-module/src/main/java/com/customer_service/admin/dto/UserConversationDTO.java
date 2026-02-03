package com.customer_service.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 用户会话 DTO - 用于客服工作台的用户列表
 * 对应截图中的列表项信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserConversationDTO {

    /**
     * 用户会话 ID
     */
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 用户 UID（用于 IM 通信）
     */
    private String userUid;

    /**
     * 用户昵称/显示名称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 设备类型 (PC端/H5 等)
     */
    private String deviceType;

    /**
     * 最后消息内容
     */
    private String lastMessage;

    /**
     * 最后消息时间
     */
    private OffsetDateTime lastMessageTime;

    /**
     * 未读消息数量
     */
    private Integer unreadCount;

    /**
     * 项目 ID
     */
    private Long projectId;

    /**
     * 是否游客
     */
    private Boolean isGuest;

    /**
     * 外部用户ID
     */
    private String externalUid;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;
}
