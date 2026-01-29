package com.customer_service.portal.service;

import com.customer_service.shared.entity.Ticket;
import com.customer_service.shared.entity.TicketEvent;
import com.customer_service.shared.repository.TicketEventRepository;
import com.customer_service.shared.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Portal 工单服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortalTicketService {

    private final TicketRepository ticketRepository;
    private final TicketEventRepository ticketEventRepository;

    /**
     * 创建工单
     */
    @Transactional
    public Ticket createTicket(Long projectId, Long userId, String title, String description,
            String contactInfo, String priority) {
        Ticket ticket = Ticket.builder()
                .projectId(projectId)
                .userId(userId)
                .creatorType("user")
                .title(title)
                .description(description)
                .contactInfo(contactInfo)
                .priority(priority != null ? priority : "medium")
                .status("open")
                .build();

        ticketRepository.save(ticket);

        // 创建工单事件
        TicketEvent event = TicketEvent.builder()
                .ticketId(ticket.getId())
                .operatorType("user")
                .operatorId(userId)
                .action("create")
                .content("用户创建工单")
                .build();

        ticketEventRepository.save(event);

        log.info("Ticket created: id={}, projectId={}, userId={}", ticket.getId(), projectId, userId);

        return ticket;
    }

    /**
     * 获取用户的工单列表
     */
    public List<Ticket> getUserTickets(Long userId) {
        return ticketRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 获取工单详情
     */
    public Ticket getTicket(Long ticketId) {
        return ticketRepository.findById(ticketId).orElse(null);
    }

    /**
     * 获取工单事件
     */
    public List<TicketEvent> getTicketEvents(Long ticketId) {
        return ticketEventRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
    }

    /**
     * 用户回复工单
     */
    @Transactional
    public TicketEvent replyTicket(Long ticketId, Long userId, String content) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("工单不存在"));

        if (!ticket.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作此工单");
        }

        TicketEvent event = TicketEvent.builder()
                .ticketId(ticketId)
                .operatorType("user")
                .operatorId(userId)
                .action("reply")
                .content(content)
                .build();

        ticketEventRepository.save(event);

        log.info("User replied to ticket: ticketId={}, userId={}", ticketId, userId);

        return event;
    }
}
