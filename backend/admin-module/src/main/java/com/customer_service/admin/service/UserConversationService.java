package com.customer_service.admin.service;

import com.customer_service.admin.dto.UserConversationDTO;
import com.customer_service.shared.entity.User;
import com.customer_service.shared.entity.UserConversation;
import com.customer_service.shared.repository.UserConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户会话服务 - 用于客服工作台
 * 提供用户列表查询功能
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserConversationService {

    private final UserConversationRepository userConversationRepository;

    /**
     * 获取多个项目下的所有用户会话列表（我的会话）
     * 按最后消息时间倒序排列
     */
    public List<UserConversationDTO> getUserConversations(List<Long> projectIds) {
        log.info("Getting user conversations for projects: {}", projectIds);
        if (projectIds == null || projectIds.isEmpty()) {
            return List.of();
        }
        List<UserConversation> conversations = userConversationRepository.findByProjectIdsWithUser(projectIds);
        return conversations.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取多个项目下有未读消息的用户会话（排队中）
     * 这些是新用户发来消息但客服还未处理的
     */
    public List<UserConversationDTO> getPendingUserConversations(List<Long> projectIds) {
        log.info("Getting pending user conversations for projects: {}", projectIds);
        if (projectIds == null || projectIds.isEmpty()) {
            return List.of();
        }
        List<UserConversation> conversations = userConversationRepository.findByProjectIdsWithUserAndUnread(projectIds);
        return conversations.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取排队中的用户数量
     */
    public long getPendingCount(List<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return 0;
        }
        return userConversationRepository.countByProjectIdsAndUnreadCountGreaterThanZero(projectIds);
    }

    /**
     * 标记用户会话为已读
     */
    @Transactional
    public void markAsRead(Long userId) {
        userConversationRepository.findByUserId(userId).ifPresent(conv -> {
            conv.setUnreadCount(0);
            userConversationRepository.save(conv);
            log.info("Marked user conversation as read: userId={}", userId);
        });
    }

    /**
     * 转换为 DTO
     * 注意：userUid 使用 {projectId}_{userId} 格式，与 WuKongIM channelId 一致
     */
    private UserConversationDTO toDTO(UserConversation conv) {
        User user = conv.getUser();

        String userName = user != null ? (user.getNickname() != null && !user.getNickname().isEmpty()
                ? user.getNickname()
                : user.getUid())
                : "Unknown";

        String deviceType = user != null && user.getDeviceType() != null
                ? formatDeviceType(user.getDeviceType())
                : "PC端";

        // userUid 使用 {projectId}_{userId} 格式，与 WuKongIM 的 channelId 保持一致
        String userUid = conv.getProjectId() + "_" + conv.getUserId();

        return UserConversationDTO.builder()
                .id(conv.getId())
                .userId(conv.getUserId())
                .userUid(userUid) // 使用 {projectId}_{userId} 格式
                .userName(userName)
                .avatar(user != null ? user.getAvatar() : null)
                .deviceType(deviceType)
                .lastMessage(conv.getLastMessageContent())
                .lastMessageTime(conv.getLastMessageTime())
                .unreadCount(conv.getUnreadCount())
                .projectId(conv.getProjectId())
                .isGuest(user != null ? user.getIsGuest() : true)
                .phone(user != null ? user.getPhone() : null)
                .build();
    }

    /**
     * 格式化设备类型显示
     */
    private String formatDeviceType(String deviceType) {
        if (deviceType == null)
            return "PC端";
        switch (deviceType.toLowerCase()) {
            case "mobile":
            case "h5":
                return "H5";
            case "pc":
            case "web":
                return "PC端";
            case "tablet":
                return "平板";
            default:
                return deviceType;
        }
    }
}
