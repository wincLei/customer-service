package com.customer_service.shared.repository;

import com.customer_service.shared.entity.AutoReplyRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutoReplyRuleRepository extends JpaRepository<AutoReplyRule, Long> {

    List<AutoReplyRule> findByProjectIdAndEnabledTrueOrderByPriorityAsc(Long projectId);

    List<AutoReplyRule> findByProjectIdOrderByPriorityAsc(Long projectId);
}
