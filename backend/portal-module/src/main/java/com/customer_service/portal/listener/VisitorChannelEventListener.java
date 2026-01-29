package com.customer_service.portal.listener;

import com.customer_service.shared.constant.WKChannelType;
import com.customer_service.shared.event.VisitorChannelCreateEvent;
import com.customer_service.shared.repository.UserProjectRepository;
import com.customer_service.shared.service.WuKongIMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 访客频道创建事件监听器
 * 异步处理访客频道的创建和客服订阅
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VisitorChannelEventListener {

    private final WuKongIMService wuKongIMService;
    private final UserProjectRepository userProjectRepository;

    /**
     * 异步处理访客频道创建事件
     * 
     * Visitor Channel 设计:
     * - channelId = {projectId}_{userId} 格式（使用数据库自增 ID 保证唯一）
     * - channelType = 10 (访客频道)
     * - 用户的 WuKongIM UID 格式也是 {projectId}_{userId}
     */
    @Async
    @EventListener
    public void handleVisitorChannelCreate(VisitorChannelCreateEvent event) {
        Long projectId = event.getProjectId();
        Long userId = event.getUserId();

        // 拼接 channelId 和用户的 WuKongIM UID
        String channelId = projectId + "_" + userId;
        String userImUid = projectId + "_" + userId;

        log.info("Processing visitor channel creation: projectId={}, userId={}, channelId={}",
                projectId, userId, channelId);

        try {
            // 1. 创建访客频道 (channel_id = {projectId}_{userId}, channel_type = 10)
            // subscribers 传用户的 WuKongIM UID
            boolean channelCreated = wuKongIMService.createChannel(
                    channelId,
                    WKChannelType.VISITOR,
                    new String[] { userImUid }); // 用户订阅自己的访客频道

            if (!channelCreated) {
                log.warn("Failed to create visitor channel: channelId={}", channelId);
                return;
            }

            log.info("Visitor channel created: channelId={}, channelType={}, subscriber={}",
                    channelId, WKChannelType.VISITOR, userImUid);

            // 2. 让项目关联的客服订阅该频道
            subscribeAgentsToChannel(projectId, channelId);

        } catch (Exception e) {
            log.error("Error creating visitor channel: channelId={}", channelId, e);
        }
    }

    /**
     * 让项目关联的客服订阅访客频道
     * 
     * @param projectId 项目ID
     * @param channelId 访客频道ID（格式: {projectId}_{userId}）
     */
    private void subscribeAgentsToChannel(Long projectId, String channelId) {
        try {
            // 通过 UserProject 关联表查找项目关联的客服（系统用户ID）
            List<Long> agentSysUserIds = userProjectRepository.findUserIdsByProjectId(projectId);

            if (agentSysUserIds.isEmpty()) {
                log.warn("No agents found for project: {}", projectId);
                return;
            }

            String[] agentUids = agentSysUserIds.stream()
                    .map(sysUserId -> "agent_" + sysUserId)
                    .toArray(String[]::new);

            boolean subscribed = wuKongIMService.addChannelSubscribers(
                    channelId,
                    WKChannelType.VISITOR,
                    agentUids);

            if (subscribed) {
                log.info("Agents subscribed to visitor channel: channelId={}, agents={}",
                        channelId, agentUids.length);
            } else {
                log.warn("Failed to subscribe agents to visitor channel: channelId={}", channelId);
            }
        } catch (Exception e) {
            log.error("Error subscribing agents to visitor channel: channelId={}", channelId, e);
        }
    }
}
