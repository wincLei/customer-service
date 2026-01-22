package com.customer_service.portal.service;

import com.customer_service.shared.entity.Agent;
import com.customer_service.shared.entity.SysUser;
import com.customer_service.shared.repository.AgentRepository;
import com.customer_service.shared.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PortalAgentService {

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
    public Optional<SysUser> findByUsername(String username) {
        return sysUserRepository.findByUsername(username);
    }

    /**
     * 根据用户ID获取关联的客服信息
     */
    public Optional<Agent> getAgentByUserId(Long userId) {
        return agentRepository.findByUserId(userId);
    }
}
