package com.customer_service.admin.service;

import com.customer_service.shared.entity.Ticket;
import com.customer_service.shared.entity.TicketEvent;
import com.customer_service.shared.entity.User;
import com.customer_service.shared.repository.TicketEventRepository;
import com.customer_service.shared.repository.TicketRepository;
import com.customer_service.shared.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashMap;

/**
 * 工单管理服务（Admin端）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketEventRepository ticketEventRepository;
    private final UserRepository userRepository;

    /**
     * 获取工单列表（带权限过滤）
     *
     * @param projectIds 用户有权限的项目ID列表（null表示管理员，可查看所有）
     * @param status     状态过滤
     * @param priority   优先级过滤
     * @param keyword    关键词搜索
     * @param page       页码
     * @param size       每页大小
     * @return 分页工单列表
     */
    public Page<Ticket> getTickets(Set<Long> projectIds, String status, String priority,
            String keyword, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 管理员可以查看所有工单
        if (projectIds == null || projectIds.isEmpty()) {
            if (keyword != null && !keyword.isBlank()) {
                return ticketRepository.searchTickets(keyword, pageRequest);
            } else if (status != null && !status.isBlank()) {
                return ticketRepository.findByStatus(status, pageRequest);
            } else {
                return ticketRepository.findAll(pageRequest);
            }
        }

        // 客服只能查看授权项目的工单
        return ticketRepository.findByProjectIdIn(projectIds, pageRequest);
    }

    /**
     * 获取工单详情
     */
    public Optional<TicketDetailDTO> getTicketDetail(Long ticketId, Set<Long> projectIds) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);

        if (ticketOpt.isEmpty()) {
            return Optional.empty();
        }

        Ticket ticket = ticketOpt.get();

        // 权限检查
        if (projectIds != null && !projectIds.isEmpty() && !projectIds.contains(ticket.getProjectId())) {
            return Optional.empty();
        }

        List<TicketEvent> events = ticketEventRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);

        // 获取用户信息
        String userName = "未知用户";
        if (ticket.getUserId() != null) {
            Optional<User> user = userRepository.findById(ticket.getUserId());
            userName = user.map(User::getNickname).orElse("用户 " + ticket.getUserId());
        }

        return Optional.of(new TicketDetailDTO(ticket, events, userName));
    }

    /**
     * 回复工单
     */
    @Transactional
    public Ticket replyTicket(Long ticketId, Long operatorId, String content) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("工单不存在"));

        // 创建回复事件
        TicketEvent event = new TicketEvent();
        event.setTicketId(ticketId);
        event.setOperatorId(operatorId);
        event.setOperatorType("agent");
        event.setAction("reply");
        event.setContent(content);
        event.setCreatedAt(LocalDateTime.now());
        ticketEventRepository.save(event);

        // 更新工单状态
        if ("open".equals(ticket.getStatus())) {
            ticket.setStatus("processing");
        }
        ticket.setUpdatedAt(LocalDateTime.now());

        return ticketRepository.save(ticket);
    }

    /**
     * 更新工单状态
     */
    @Transactional
    public Ticket updateStatus(Long ticketId, Long operatorId, String status) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("工单不存在"));

        String oldStatus = ticket.getStatus();
        ticket.setStatus(status);
        ticket.setUpdatedAt(LocalDateTime.now());

        if ("resolved".equals(status) || "closed".equals(status)) {
            ticket.setResolvedAt(LocalDateTime.now());
        }

        // 记录状态变更事件
        TicketEvent event = new TicketEvent();
        event.setTicketId(ticketId);
        event.setOperatorId(operatorId);
        event.setOperatorType("agent");
        event.setAction("status_change");
        event.setContent(String.format("状态从 %s 变更为 %s", oldStatus, status));
        event.setCreatedAt(LocalDateTime.now());
        ticketEventRepository.save(event);

        return ticketRepository.save(ticket);
    }

    /**
     * 分配工单
     */
    @Transactional
    public Ticket assignTicket(Long ticketId, Long operatorId, Long assigneeId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("工单不存在"));

        ticket.setAssigneeId(assigneeId);
        ticket.setUpdatedAt(LocalDateTime.now());

        // 记录分配事件
        TicketEvent event = new TicketEvent();
        event.setTicketId(ticketId);
        event.setOperatorId(operatorId);
        event.setOperatorType("agent");
        event.setAction("assign");
        event.setContent("分配给客服 ID: " + assigneeId);
        event.setCreatedAt(LocalDateTime.now());
        ticketEventRepository.save(event);

        return ticketRepository.save(ticket);
    }

    /**
     * 获取工单统计
     */
    public Map<String, Long> getTicketStats(Set<Long> projectIds) {
        Map<String, Long> stats = new HashMap<>();

        if (projectIds == null || projectIds.isEmpty()) {
            stats.put("total", ticketRepository.count());
            stats.put("open", ticketRepository.countByStatus("open"));
            stats.put("processing", ticketRepository.countByStatus("processing"));
            stats.put("resolved", ticketRepository.countByStatus("resolved"));
            stats.put("closed", ticketRepository.countByStatus("closed"));
        } else {
            stats.put("total", ticketRepository.countByProjectIdIn(projectIds));
            stats.put("open", ticketRepository.countByProjectIdInAndStatus(projectIds, "open"));
            stats.put("processing", ticketRepository.countByProjectIdInAndStatus(projectIds, "processing"));
            stats.put("resolved", ticketRepository.countByProjectIdInAndStatus(projectIds, "resolved"));
            stats.put("closed", ticketRepository.countByProjectIdInAndStatus(projectIds, "closed"));
        }

        return stats;
    }

    public record TicketDetailDTO(
            Ticket ticket,
            List<TicketEvent> events,
            String userName) {
    }
}
