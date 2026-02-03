package com.customer_service.admin.service;

import com.customer_service.admin.dto.UserConversationDTO;
import com.customer_service.shared.entity.User;
import com.customer_service.shared.entity.UserConversation;
import com.customer_service.shared.repository.UserConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     * 分页获取多个项目下的用户会话列表（我的会话）
     * 按最后消息时间倒序排列
     * 
     * @param projectIds 项目ID列表
     * @param page       页码（从0开始）
     * @param pageSize   每页大小
     * @return 包含列表和分页信息的Map
     */
    public Map<String, Object> getUserConversationsPaged(List<Long> projectIds, int page, int pageSize) {
        return getUserConversationsPaged(projectIds, page, pageSize, null);
    }

    /**
     * 分页获取多个项目下的用户会话列表（我的会话），支持搜索
     * 按最后消息时间倒序排列
     * 
     * @param projectIds 项目ID列表
     * @param page       页码（从0开始）
     * @param pageSize   每页大小
     * @param keyword    搜索关键词（全匹配 uid 或 external_uid）
     * @return 包含列表和分页信息的Map
     */
    public Map<String, Object> getUserConversationsPaged(List<Long> projectIds, int page, int pageSize,
            String keyword) {
        log.info("Getting user conversations paged for projects: {}, page: {}, pageSize: {}, keyword: {}",
                projectIds, page, pageSize, keyword);
        Map<String, Object> result = new HashMap<>();

        if (projectIds == null || projectIds.isEmpty()) {
            result.put("list", List.of());
            result.put("total", 0L);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("hasMore", false);
            return result;
        }

        Pageable pageable = PageRequest.of(page, pageSize);
        Page<UserConversation> pageResult;

        // 如果有搜索关键词，使用带搜索条件的查询
        if (keyword != null && !keyword.trim().isEmpty()) {
            String trimmedKeyword = keyword.trim();
            pageResult = userConversationRepository.findByProjectIdsAndKeywordPaged(projectIds, trimmedKeyword,
                    pageable);
        } else {
            pageResult = userConversationRepository.findByProjectIdsWithUserPaged(projectIds, pageable);
        }

        List<UserConversationDTO> list = pageResult.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        result.put("list", list);
        result.put("total", pageResult.getTotalElements());
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("hasMore", pageResult.hasNext());

        return result;
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
     * 
     * @param userId     用户ID
     * @param messageSeq 消息序号（可选），如果提供则记录已读位置
     */
    @Transactional
    public void markAsRead(Long userId, Long messageSeq) {
        userConversationRepository.findByUserId(userId).ifPresent(conv -> {
            conv.setUnreadCount(0);
            if (messageSeq != null && messageSeq > 0) {
                conv.setLastMessageSeq(messageSeq);
            }
            userConversationRepository.save(conv);
            log.info("Marked user conversation as read: userId={}, messageSeq={}", userId, messageSeq);
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
                .externalUid(user != null ? user.getExternalUid() : null)
                .email(user != null ? user.getEmail() : null)
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
