package com.customer_service.admin.service;

import com.customer_service.admin.repository.ConversationRepository;
import com.customer_service.admin.repository.UserTagRepository;
import com.customer_service.shared.entity.Agent;
import com.customer_service.shared.entity.User;
import com.customer_service.shared.entity.UserTag;
import com.customer_service.shared.repository.AgentRepository;
import com.customer_service.shared.repository.UserProjectRepository;
import com.customer_service.shared.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserTagRepository userTagRepository;
    private final AgentRepository agentRepository;
    private final ConversationRepository conversationRepository;
    private final UserProjectRepository userProjectRepository;

    /**
     * 根据UID获取用户信息
     */
    public User getUserByUid(Long projectId, String uid) {
        return userRepository.findByProjectIdAndUid(projectId, uid)
                .orElse(null);
    }

    /**
     * 创建或更新用户信息
     */
    @Transactional
    public User createOrUpdateUser(User user) {
        return userRepository.findByProjectIdAndUid(user.getProjectId(), user.getUid())
                .map(existing -> {
                    existing.setNickname(user.getNickname());
                    existing.setAvatar(user.getAvatar());
                    existing.setPhone(user.getPhone());
                    existing.setEmail(user.getEmail());
                    return userRepository.save(existing);
                })
                .orElseGet(() -> userRepository.save(user));
    }

    /**
     * 为用户添加标签
     */
    @Transactional
    public void addUserTag(Long projectId, Long userId, String tagName, Long taggedBy) {
        UserTag tag = new UserTag();
        tag.setProjectId(projectId);
        tag.setUserId(userId);
        tag.setTagName(tagName);
        tag.setTaggedBy(taggedBy);
        tag.setCreatedAt(LocalDateTime.now());

        userTagRepository.save(tag);
    }

    /**
     * 删除用户标签
     */
    @Transactional
    public void removeUserTag(Long userId, String tagName) {
        userTagRepository.deleteByUserIdAndTagName(userId, tagName);
    }

    /**
     * 获取用户的所有标签
     */
    public List<String> getUserTags(Long userId) {
        return userTagRepository.findByUserId(userId)
                .stream()
                .map(UserTag::getTagName)
                .collect(Collectors.toList());
    }

    /**
     * 获取项目的客服列表（包含活跃会话数）
     * 通过 user_projects 表关联查询该项目下的客服
     */
    public List<Map<String, Object>> getAgentList(Long projectId) {
        // 先查询关联到该项目的用户ID列表
        List<Long> userIds = userProjectRepository.findUserIdsByProjectId(projectId);
        if (userIds.isEmpty()) {
            return List.of();
        }

        // 查询这些用户对应的客服记录
        List<Agent> agents = agentRepository.findByUserIdIn(userIds);

        return agents.stream().map(agent -> {
            Map<String, Object> agentInfo = new HashMap<>();
            agentInfo.put("id", agent.getId());

            // 从关联的 SysUser 获取用户名，如果没有则使用 agent 的昵称
            String username = agent.getSysUser() != null ? agent.getSysUser().getUsername() : "unknown";
            agentInfo.put("username", username);
            agentInfo.put("nickname", agent.getNickname());
            agentInfo.put("status", agent.getWorkStatus());

            // 获取该客服的活跃会话数
            Long activeCount = conversationRepository.countActiveByAgentId(agent.getId());
            agentInfo.put("activeCount", activeCount);

            return agentInfo;
        }).collect(Collectors.toList());
    }
}
