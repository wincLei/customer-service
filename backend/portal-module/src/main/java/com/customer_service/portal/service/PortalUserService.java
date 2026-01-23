package com.customer_service.portal.service;

import com.customer_service.shared.entity.User;
import com.customer_service.shared.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Portal 用户服务
 * 处理客户端用户的初始化、登录和合并逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortalUserService {

    private final UserRepository userRepository;

    /**
     * 用户初始化结果
     */
    public record UserInitResult(
            User user,
            boolean isNewUser,
            boolean merged,
            Long mergedFromUserId) {
    }

    /**
     * 初始化或获取用户
     * 支持游客和已认证用户的自动合并
     *
     * @param projectId   项目ID
     * @param guestUid    游客UID（由前端生成并存储在localStorage）
     * @param externalUid 外部系统UID（可选，如果提供则尝试合并）
     * @param nickname    昵称
     * @param avatar      头像
     * @param phone       手机号
     * @return 用户初始化结果
     */
    @Transactional
    public UserInitResult initOrMergeUser(
            Long projectId,
            String guestUid,
            String externalUid,
            String nickname,
            String avatar,
            String phone) {
        log.info("Init user: projectId={}, guestUid={}, externalUid={}", projectId, guestUid, externalUid);

        // 情况1: 有外部UID，优先查找已认证用户
        if (externalUid != null && !externalUid.isBlank()) {
            Optional<User> existingAuth = userRepository.findByProjectIdAndExternalUid(projectId, externalUid);

            if (existingAuth.isPresent()) {
                // 已认证用户存在，更新信息并返回
                User authUser = existingAuth.get();
                updateUserInfo(authUser, nickname, avatar, phone);
                authUser.setLastActiveAt(LocalDateTime.now());
                userRepository.save(authUser);

                // 检查是否需要合并游客记录
                if (guestUid != null && !guestUid.isBlank() && !guestUid.equals(authUser.getUid())) {
                    Long mergedUserId = mergeGuestToAuthUser(projectId, guestUid, authUser);
                    if (mergedUserId != null) {
                        return new UserInitResult(authUser, false, true, mergedUserId);
                    }
                }

                return new UserInitResult(authUser, false, false, null);
            }

            // 已认证用户不存在，检查是否有游客记录需要升级
            Optional<User> existingGuest = userRepository.findByProjectIdAndUid(projectId, guestUid);

            if (existingGuest.isPresent()) {
                // 将游客升级为认证用户
                User guestUser = existingGuest.get();
                guestUser.setExternalUid(externalUid);
                guestUser.setIsGuest(false);
                updateUserInfo(guestUser, nickname, avatar, phone);
                guestUser.setLastActiveAt(LocalDateTime.now());
                userRepository.save(guestUser);
                log.info("Upgraded guest to auth user: uid={}, externalUid={}", guestUser.getUid(), externalUid);
                return new UserInitResult(guestUser, false, false, null);
            }

            // 创建新的认证用户
            User newAuthUser = createNewUser(projectId, externalUid, externalUid, false, nickname, avatar, phone);
            return new UserInitResult(newAuthUser, true, false, null);
        }

        // 情况2: 没有外部UID，作为游客处理
        if (guestUid != null && !guestUid.isBlank()) {
            Optional<User> existingGuest = userRepository.findByProjectIdAndUid(projectId, guestUid);

            if (existingGuest.isPresent()) {
                // 游客存在，更新最后活跃时间
                User guestUser = existingGuest.get();
                updateUserInfo(guestUser, nickname, avatar, phone);
                guestUser.setLastActiveAt(LocalDateTime.now());
                userRepository.save(guestUser);
                return new UserInitResult(guestUser, false, false, null);
            }

            // 创建新游客
            User newGuest = createNewUser(projectId, guestUid, null, true, nickname, avatar, phone);
            return new UserInitResult(newGuest, true, false, null);
        }

        // 情况3: 既没有外部UID也没有游客UID，创建匿名游客
        String anonymousUid = "anon_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 10000);
        User anonymousUser = createNewUser(projectId, anonymousUid, null, true, nickname, avatar, phone);
        return new UserInitResult(anonymousUser, true, false, null);
    }

    /**
     * 合并游客记录到认证用户
     */
    @Transactional
    public Long mergeGuestToAuthUser(Long projectId, String guestUid, User authUser) {
        Optional<User> guestOpt = userRepository.findByProjectIdAndUid(projectId, guestUid);

        if (guestOpt.isEmpty()) {
            return null;
        }

        User guestUser = guestOpt.get();

        // 标记游客已被合并
        guestUser.setMergedFromId(authUser.getId());
        userRepository.save(guestUser);

        log.info("Merged guest user {} to auth user {}", guestUser.getId(), authUser.getId());

        // TODO: 这里可以添加合并会话历史的逻辑
        // conversationService.mergeConversations(guestUser.getId(), authUser.getId());

        return guestUser.getId();
    }

    /**
     * 创建新用户
     */
    private User createNewUser(
            Long projectId,
            String uid,
            String externalUid,
            boolean isGuest,
            String nickname,
            String avatar,
            String phone) {
        User user = User.builder()
                .projectId(projectId)
                .uid(uid)
                .externalUid(externalUid)
                .isGuest(isGuest)
                .nickname(nickname)
                .avatar(avatar)
                .phone(phone)
                .build();

        userRepository.save(user);
        log.info("Created new user: id={}, uid={}, isGuest={}", user.getId(), uid, isGuest);
        return user;
    }

    /**
     * 更新用户信息（只更新非空字段）
     */
    private void updateUserInfo(User user, String nickname, String avatar, String phone) {
        if (nickname != null && !nickname.isBlank()) {
            user.setNickname(nickname);
        }
        if (avatar != null && !avatar.isBlank()) {
            user.setAvatar(avatar);
        }
        if (phone != null && !phone.isBlank()) {
            user.setPhone(phone);
        }
    }

    /**
     * 根据UID获取用户
     */
    public Optional<User> getUserByUid(Long projectId, String uid) {
        return userRepository.findByProjectIdAndUid(projectId, uid);
    }

    /**
     * 根据外部UID获取用户
     */
    public Optional<User> getUserByExternalUid(Long projectId, String externalUid) {
        return userRepository.findByProjectIdAndExternalUid(projectId, externalUid);
    }
}
