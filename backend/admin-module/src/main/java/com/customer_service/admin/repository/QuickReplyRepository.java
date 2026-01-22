package com.customer_service.admin.repository;

import com.customer_service.shared.entity.QuickReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuickReplyRepository extends JpaRepository<QuickReply, Long> {

    List<QuickReply> findByProjectId(Long projectId);

    List<QuickReply> findByProjectIdAndCreatorIdIsNull(Long projectId);

    List<QuickReply> findByProjectIdAndCreatorId(Long projectId, Long creatorId);
}
