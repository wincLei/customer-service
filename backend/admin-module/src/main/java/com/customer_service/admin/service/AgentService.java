package com.customer_service.admin.service;

import com.customer_service.shared.entity.Agent;
import com.customer_service.shared.entity.SysUser;
import com.customer_service.shared.repository.AgentRepository;
import com.customer_service.shared.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentRepository agentRepository;
    private final SysUserRepository sysUserRepository;
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
     * 创建客服账号（同时创建 SysUser 和 Agent）
     */
    @Transactional
    public Agent createAgent(Long projectId, String username, String password, String nickname, String email,
            Long roleId) {
        // 先创建 SysUser
        SysUser sysUser = new SysUser();
        sysUser.setUsername(username);
        sysUser.setPasswordHash(passwordEncoder.encode(password));
        sysUser.setEmail(email);
        sysUser.setStatus("active");
        // 需要设置角色关联
        sysUser = sysUserRepository.save(sysUser);

        // 再创建 Agent 关联到 SysUser
        Agent agent = new Agent();
        agent.setProjectId(projectId);
        agent.setUserId(sysUser.getId());
        agent.setNickname(nickname);
        agent.setWorkStatus("online");
        agent.setMaxLoad(10);

        return agentRepository.save(agent);
    }

    /**
     * 更新客服工作状态
     */
    public void updateWorkStatus(Long agentId, String workStatus) {
        Optional<Agent> agent = agentRepository.findById(agentId);
        if (agent.isPresent()) {
            agent.get().setWorkStatus(workStatus);
            agentRepository.save(agent.get());
        }
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
}
