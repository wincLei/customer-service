package com.customer_service.shared.repository;

import com.customer_service.shared.entity.TicketEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketEventRepository extends JpaRepository<TicketEvent, Long> {

    /**
     * 按工单ID获取事件列表
     */
    List<TicketEvent> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

    /**
     * 统计工单事件数量
     */
    Long countByTicketId(Long ticketId);
}
