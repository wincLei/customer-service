package com.customer_service.portal.service;

import com.customer_service.shared.entity.Agent;
import com.customer_service.shared.repository.AgentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PortalAgentService {

    private final AgentRepository agentRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public Optional<Agent> authenticate(String username, String password) {
        Optional<Agent> agent = agentRepository.findByUsername(username);
        
        if (agent.isPresent() && passwordEncoder.matches(password, agent.get().getPasswordHash())) {
            Agent a = agent.get();
            a.setLastLoginAt(LocalDateTime.now());
            agentRepository.save(a);
            return Optional.of(a);
        }
        
        return Optional.empty();
    }

    public Optional<Agent> findByUsername(String username) {
        return agentRepository.findByUsername(username);
    }
}
