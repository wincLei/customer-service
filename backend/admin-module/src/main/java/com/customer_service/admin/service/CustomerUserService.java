package com.customer_service.admin.service;

import com.customer_service.admin.repository.CustomerTagRepository;
import com.customer_service.admin.repository.UserTagRelationRepository;
import com.customer_service.shared.entity.CustomerTag;
import com.customer_service.shared.entity.User;
import com.customer_service.shared.entity.UserTagRelation;
import com.customer_service.shared.repository.UserProjectRepository;
import com.customer_service.shared.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 客户端用户管理服务
 * 管理使用客服系统的终端用户（访客/客户）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerUserService {

    private final UserRepository userRepository;
    private final UserProjectRepository userProjectRepository;
    private final CustomerTagRepository customerTagRepository;
    private final UserTagRelationRepository userTagRelationRepository;

    /**
     * 获取用户有权限的项目ID列表
     */
    public List<Long> getAccessibleProjectIds(Long userId, String roleCode) {
        if ("admin".equals(roleCode) || "super_admin".equals(roleCode)) {
            // 管理员可以访问所有项目
            return null; // null 表示不限制
        }
        // 客服只能访问关联的项目
        return userProjectRepository.findProjectIdsByUserId(userId);
    }

    /**
     * 分页获取客户端用户列表
     */
    public Page<User> getUsers(Long projectId, String keyword, Boolean isGuest, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastActiveAt"));

        if (keyword != null && !keyword.trim().isEmpty()) {
            return userRepository.searchByProjectId(projectId, keyword.trim(), pageable);
        } else if (isGuest != null) {
            return userRepository.findByProjectIdAndIsGuest(projectId, isGuest, pageable);
        } else {
            return userRepository.findByProjectId(projectId, pageable);
        }
    }

    /**
     * 获取单个用户详情
     */
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * 获取用户详情（包含标签）
     */
    public Map<String, Object> getUserDetail(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("user", user);

        // 获取用户标签
        List<UserTagRelation> relations = userTagRelationRepository.findByUserId(id);
        List<Long> tagIds = relations.stream().map(UserTagRelation::getTagId).collect(Collectors.toList());
        if (!tagIds.isEmpty()) {
            List<CustomerTag> tags = customerTagRepository.findAllById(tagIds);
            result.put("tags", tags);
        } else {
            result.put("tags", List.of());
        }

        return result;
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public User updateUser(Long id, String nickname, String phone, String email) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (nickname != null) {
            user.setNickname(nickname);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (email != null) {
            user.setEmail(email);
        }
        user.setLastActiveAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * 给用户添加标签
     */
    @Transactional
    public void addTagToUser(Long userId, Long tagId, Long operatorId) {
        if (userTagRelationRepository.existsByUserIdAndTagId(userId, tagId)) {
            return; // 已存在，无需重复添加
        }

        UserTagRelation relation = UserTagRelation.builder()
                .userId(userId)
                .tagId(tagId)
                .taggedBy(operatorId)
                .build();
        userTagRelationRepository.save(relation);
    }

    /**
     * 移除用户的标签
     */
    @Transactional
    public void removeTagFromUser(Long userId, Long tagId) {
        userTagRelationRepository.deleteByUserIdAndTagId(userId, tagId);
    }

    /**
     * 批量给用户添加标签
     */
    @Transactional
    public void batchAddTagToUsers(List<Long> userIds, Long tagId, Long operatorId) {
        for (Long userId : userIds) {
            addTagToUser(userId, tagId, operatorId);
        }
    }

    /**
     * 获取用户的所有标签
     */
    public List<CustomerTag> getUserTags(Long userId) {
        List<Long> tagIds = userTagRelationRepository.findTagIdsByUserId(userId);
        if (tagIds.isEmpty()) {
            return List.of();
        }
        return customerTagRepository.findAllById(tagIds);
    }

    /**
     * 设置用户的标签（替换现有标签）
     */
    @Transactional
    public void setUserTags(Long userId, List<Long> tagIds, Long operatorId) {
        // 删除现有标签
        userTagRelationRepository.deleteByUserId(userId);

        // 添加新标签
        for (Long tagId : tagIds) {
            UserTagRelation relation = UserTagRelation.builder()
                    .userId(userId)
                    .tagId(tagId)
                    .taggedBy(operatorId)
                    .build();
            userTagRelationRepository.save(relation);
        }
    }

    /**
     * 合并游客用户到已认证用户
     * 
     * @param guestUserId  游客用户ID
     * @param targetUserId 目标用户ID（已认证用户）
     */
    @Transactional
    public User mergeGuestToUser(Long guestUserId, Long targetUserId) {
        User guestUser = userRepository.findById(guestUserId)
                .orElseThrow(() -> new RuntimeException("游客用户不存在"));
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("目标用户不存在"));

        if (!guestUser.getIsGuest()) {
            throw new RuntimeException("只能合并游客用户");
        }
        if (!guestUser.getProjectId().equals(targetUser.getProjectId())) {
            throw new RuntimeException("用户必须属于同一项目");
        }

        // 标记游客用户已被合并
        guestUser.setMergedFromId(targetUserId);
        userRepository.save(guestUser);

        // 迁移游客的标签到目标用户
        List<Long> guestTagIds = userTagRelationRepository.findTagIdsByUserId(guestUserId);
        for (Long tagId : guestTagIds) {
            if (!userTagRelationRepository.existsByUserIdAndTagId(targetUserId, tagId)) {
                UserTagRelation relation = UserTagRelation.builder()
                        .userId(targetUserId)
                        .tagId(tagId)
                        .build();
                userTagRelationRepository.save(relation);
            }
        }

        log.info("合并游客用户 {} 到用户 {}", guestUserId, targetUserId);
        return targetUser;
    }

    /**
     * 根据游客UID和外部UID自动合并
     * 用于前端传入外部系统ID时自动关联历史游客记录
     */
    @Transactional
    public User autoMergeByExternalUid(Long projectId, String guestUid, String externalUid) {
        // 查找游客记录
        User guestUser = userRepository.findByProjectIdAndUid(projectId, guestUid).orElse(null);
        if (guestUser == null || !guestUser.getIsGuest()) {
            return null;
        }

        // 查找或创建目标用户
        User targetUser = userRepository.findByProjectIdAndExternalUid(projectId, externalUid).orElse(null);
        if (targetUser == null) {
            // 将游客用户升级为认证用户
            guestUser.setExternalUid(externalUid);
            guestUser.setIsGuest(false);
            guestUser.setLastActiveAt(LocalDateTime.now());
            log.info("将游客用户 {} 升级为认证用户, externalUid={}", guestUser.getId(), externalUid);
            return userRepository.save(guestUser);
        }

        // 合并游客到已存在的认证用户
        return mergeGuestToUser(guestUser.getId(), targetUser.getId());
    }

    /**
     * 统计项目用户数据
     */
    public Map<String, Object> getUserStats(Long projectId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", userRepository.countByProjectId(projectId));
        stats.put("guests", userRepository.countByProjectIdAndIsGuest(projectId, true));
        stats.put("registered", userRepository.countByProjectIdAndIsGuest(projectId, false));
        return stats;
    }
}
