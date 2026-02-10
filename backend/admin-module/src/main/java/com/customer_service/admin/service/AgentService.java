package com.customer_service.admin.service;

import com.customer_service.shared.constant.AppDefaults;
import com.customer_service.shared.constant.RoleCode;
import com.customer_service.shared.constant.WorkStatus;
import com.customer_service.shared.entity.Agent;
import com.customer_service.shared.entity.Project;
import com.customer_service.shared.entity.SysUser;
import com.customer_service.shared.entity.UserProject;
import com.customer_service.shared.repository.AgentRepository;
import com.customer_service.shared.repository.ProjectRepository;
import com.customer_service.shared.repository.SysUserRepository;
import com.customer_service.shared.repository.UserProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.customer_service.shared.util.I18nUtil;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentRepository agentRepository;
    private final SysUserRepository sysUserRepository;
    private final UserProjectRepository userProjectRepository;
    private final ProjectRepository projectRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * 用户认证 - 使用 SysUser 表
     */
    public Optional<SysUser> authenticate(String username, String password) {
        Optional<SysUser> userOptional = sysUserRepository.findByUsername(username);

        if (userOptional.isPresent() && passwordEncoder.matches(password, userOptional.get().getPasswordHash())) {
            SysUser user = userOptional.get();
            user.setLastLoginAt(LocalDateTime.now());
            sysUserRepository.save(user);
            return Optional.of(user);
        }

        return Optional.empty();
    }

    /**
     * 根据用户名查找系统用户
     */
    public Optional<SysUser> findUserByUsername(String username) {
        return sysUserRepository.findByUsername(username);
    }

    /**
     * 根据用户名查找客服（通过关联的 SysUser）
     */
    public Optional<Agent> findAgentByUsername(String username) {
        return agentRepository.findByUsername(username);
    }

    /**
     * 获取所有客服列表
     */
    public List<Agent> getAllAgents() {
        return agentRepository.findAllWithUser();
    }

    /**
     * 创建客服记录
     */
    @Transactional
    public Agent createAgent(Long userId, String nickname, Integer maxLoad,
            String welcomeMessage, Boolean autoReplyEnabled,
            List<Long> projectIds) {
        // 检查用户是否存在
        SysUser user = sysUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(I18nUtil.getMessage("agent.user.not.found")));

        // 检查是否已有客服记录
        if (agentRepository.existsByUserId(userId)) {
            throw new RuntimeException(I18nUtil.getMessage("agent.already.exists"));
        }

        // 创建客服记录
        Agent agent = Agent.builder()
                .userId(userId)
                .nickname(nickname != null ? nickname : user.getUsername())
                .maxLoad(maxLoad != null ? maxLoad : AppDefaults.DEFAULT_MAX_LOAD)
                .currentLoad(0)
                .workStatus(WorkStatus.OFFLINE)
                .welcomeMessage(welcomeMessage)
                .autoReplyEnabled(autoReplyEnabled != null ? autoReplyEnabled : false)
                .build();

        Agent savedAgent = agentRepository.save(agent);

        // 保存项目关联
        if (projectIds != null && !projectIds.isEmpty()) {
            saveUserProjects(userId, projectIds);
        }

        return savedAgent;
    }

    /**
     * 更新客服信息
     */
    @Transactional
    public Agent updateAgent(Long id, String nickname, Integer maxLoad,
            String welcomeMessage, Boolean autoReplyEnabled,
            List<Long> projectIds) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(I18nUtil.getMessage("agent.not.found")));

        if (nickname != null) {
            agent.setNickname(nickname);
        }
        if (maxLoad != null) {
            agent.setMaxLoad(maxLoad);
        }
        if (welcomeMessage != null) {
            agent.setWelcomeMessage(welcomeMessage);
        }
        if (autoReplyEnabled != null) {
            agent.setAutoReplyEnabled(autoReplyEnabled);
        }

        Agent savedAgent = agentRepository.save(agent);

        // 更新项目关联
        if (projectIds != null) {
            saveUserProjects(agent.getUserId(), projectIds);
        }

        return savedAgent;
    }

    /**
     * 删除客服
     */
    @Transactional
    public void deleteAgent(Long id) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(I18nUtil.getMessage("agent.not.found")));

        // 删除项目关联
        userProjectRepository.deleteByUserId(agent.getUserId());

        // 删除客服记录
        agentRepository.deleteById(id);
    }

    /**
     * 更新客服工作状态
     */
    @Transactional
    public Agent updateWorkStatus(Long agentId, String workStatus) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException(I18nUtil.getMessage("agent.not.found")));
        agent.setWorkStatus(workStatus);
        return agentRepository.save(agent);
    }

    /**
     * 获取客服信息
     */
    public Optional<Agent> getAgentInfo(Long agentId) {
        return agentRepository.findById(agentId);
    }

    /**
     * 根据用户ID获取客服信息
     */
    public Optional<Agent> getAgentByUserId(Long userId) {
        return agentRepository.findByUserId(userId);
    }

    /**
     * 获取客服关联的项目ID列表
     */
    public List<Long> getAgentProjectIds(Long agentId) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException(I18nUtil.getMessage("agent.not.found")));
        return userProjectRepository.findProjectIdsByUserId(agent.getUserId());
    }

    /**
     * 保存用户-项目关联
     */
    private void saveUserProjects(Long userId, List<Long> projectIds) {
        // 先删除旧的关联
        userProjectRepository.deleteByUserId(userId);

        // 获取用户实体
        SysUser user = sysUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(I18nUtil.getMessage("agent.user.not.found")));

        // 保存新的关联
        for (Long projectId : projectIds) {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException(I18nUtil.getMessage("agent.project.not.found", projectId)));
            UserProject userProject = new UserProject(user, project);
            userProjectRepository.save(userProject);
        }
    }

    /**
     * 获取可以成为客服的用户列表（角色为agent且尚未创建客服记录的用户）
     */
    public List<SysUser> getAvailableUsersForAgent() {
        List<SysUser> agentRoleUsers = sysUserRepository.findByRoleCode(RoleCode.AGENT);
        return agentRoleUsers.stream()
                .filter(user -> !agentRepository.existsByUserId(user.getId()))
                .toList();
    }
}
