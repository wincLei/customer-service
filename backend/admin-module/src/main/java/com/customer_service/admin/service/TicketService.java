package com.customer_service.admin.service;

import com.customer_service.shared.constant.OperatorType;
import com.customer_service.shared.constant.TicketAction;
import com.customer_service.shared.constant.TicketStatus;
import com.customer_service.shared.entity.Project;
import com.customer_service.shared.entity.Ticket;
import com.customer_service.shared.entity.TicketEvent;
import com.customer_service.shared.entity.User;
import com.customer_service.shared.repository.ProjectRepository;
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

import com.customer_service.shared.util.I18nUtil;

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
    private final ProjectRepository projectRepository;

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
        String userName = I18nUtil.getMessage("ticket.unknown.user");
        Long userId = ticket.getUserId();
        String userNickname = null;
        String userExternalUid = null;
        if (userId != null) {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                userNickname = user.getNickname();
                userExternalUid = user.getExternalUid();
                userName = userNickname != null ? userNickname
                        : I18nUtil.getMessage("ticket.user.prefix") + userId;
            } else {
                userName = I18nUtil.getMessage("ticket.user.prefix") + userId;
            }
        }

        // 获取项目名称
        String projectName = null;
        if (ticket.getProjectId() != null) {
            projectName = projectRepository.findById(ticket.getProjectId())
                    .map(Project::getName)
                    .orElse(null);
        }

        return Optional
                .of(new TicketDetailDTO(ticket, events, userName, userId, userNickname, userExternalUid, projectName));
    }

    /**
     * 获取指定用户的工单列表
     *
     * @param userId     用户ID
     * @param projectIds 用户有权限的项目ID列表（null表示管理员，可查看所有）
     * @return 用户工单列表
     */
    public List<Ticket> getTicketsByUserId(Long userId, Set<Long> projectIds) {
        List<Ticket> tickets = ticketRepository.findByUserIdOrderByCreatedAtDesc(userId);

        // 如果不是管理员，需要过滤掉没有权限的项目工单
        if (projectIds != null && !projectIds.isEmpty()) {
            tickets = tickets.stream()
                    .filter(t -> projectIds.contains(t.getProjectId()))
                    .toList();
        }

        return tickets;
    }

    /**
     * 回复工单
     */
    @Transactional
    public Ticket replyTicket(Long ticketId, Long operatorId, String content) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException(I18nUtil.getMessage("ticket.not.found")));

        // 创建回复事件
        TicketEvent event = new TicketEvent();
        event.setTicketId(ticketId);
        event.setOperatorId(operatorId);
        event.setOperatorType(OperatorType.AGENT);
        event.setAction(TicketAction.REPLY);
        event.setContent(content);
        event.setCreatedAt(LocalDateTime.now());
        ticketEventRepository.save(event);

        // 更新工单状态
        if (TicketStatus.OPEN.equals(ticket.getStatus())) {
            ticket.setStatus(TicketStatus.PROCESSING);
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
                .orElseThrow(() -> new RuntimeException(I18nUtil.getMessage("ticket.not.found")));

        String oldStatus = ticket.getStatus();
        ticket.setStatus(status);
        ticket.setUpdatedAt(LocalDateTime.now());

        if (TicketStatus.RESOLVED.equals(status) || TicketStatus.CLOSED.equals(status)) {
            ticket.setResolvedAt(LocalDateTime.now());
        }

        // 记录状态变更事件
        TicketEvent event = new TicketEvent();
        event.setTicketId(ticketId);
        event.setOperatorId(operatorId);
        event.setOperatorType(OperatorType.AGENT);
        event.setAction(TicketAction.STATUS_CHANGE);
        event.setContent(I18nUtil.getMessage("ticket.status.change",
                I18nUtil.getMessage("ticket.status." + oldStatus),
                I18nUtil.getMessage("ticket.status." + status)));
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
                .orElseThrow(() -> new RuntimeException(I18nUtil.getMessage("ticket.not.found")));

        ticket.setAssigneeId(assigneeId);
        ticket.setUpdatedAt(LocalDateTime.now());

        // 记录分配事件
        TicketEvent event = new TicketEvent();
        event.setTicketId(ticketId);
        event.setOperatorId(operatorId);
        event.setOperatorType(OperatorType.AGENT);
        event.setAction(TicketAction.ASSIGN);
        event.setContent(I18nUtil.getMessage("ticket.assign.agent", assigneeId));
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
            stats.put("open", ticketRepository.countByStatus(TicketStatus.OPEN));
            stats.put("processing", ticketRepository.countByStatus(TicketStatus.PROCESSING));
            stats.put("resolved", ticketRepository.countByStatus(TicketStatus.RESOLVED));
            stats.put("closed", ticketRepository.countByStatus(TicketStatus.CLOSED));
        } else {
            stats.put("total", ticketRepository.countByProjectIdIn(projectIds));
            stats.put("open", ticketRepository.countByProjectIdInAndStatus(projectIds, TicketStatus.OPEN));
            stats.put("processing", ticketRepository.countByProjectIdInAndStatus(projectIds, TicketStatus.PROCESSING));
            stats.put("resolved", ticketRepository.countByProjectIdInAndStatus(projectIds, TicketStatus.RESOLVED));
            stats.put("closed", ticketRepository.countByProjectIdInAndStatus(projectIds, TicketStatus.CLOSED));
        }

        return stats;
    }

    public record TicketDetailDTO(
            Ticket ticket,
            List<TicketEvent> events,
            String userName,
            Long userId,
            String userNickname,
            String userExternalUid,
            String projectName) {
    }
}
