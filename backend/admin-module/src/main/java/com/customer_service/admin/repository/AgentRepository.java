package com.customer_service.admin.repository;

import com.customer_service.shared.entity.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {
    Optional<Agent> findByProjectIdAndUsername(Long projectId, String username);
    
    java.util.List<Agent> findByProjectIdAndStatusIn(Long projectId, java.util.List<String> statuses);
    
    java.util.List<Agent> findByProjectId(Long projectId);
}
