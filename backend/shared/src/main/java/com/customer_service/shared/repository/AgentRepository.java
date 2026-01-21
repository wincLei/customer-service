package com.customer_service.shared.repository;

import com.customer_service.shared.entity.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {
    Optional<Agent> findByUsername(String username);
    
    Optional<Agent> findByProjectIdAndUsername(Long projectId, String username);
    
    List<Agent> findByProjectIdAndStatusIn(Long projectId, List<String> statuses);
    
    List<Agent> findByProjectId(Long projectId);
}
