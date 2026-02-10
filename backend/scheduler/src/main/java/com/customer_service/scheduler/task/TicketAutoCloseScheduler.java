package com.customer_service.scheduler.task;

import com.customer_service.shared.constant.AppDefaults;
import com.customer_service.shared.constant.OperatorType;
import com.customer_service.shared.constant.TicketAction;
import com.customer_service.shared.constant.TicketStatus;
import com.customer_service.shared.entity.Ticket;
import com.customer_service.shared.entity.TicketEvent;
import com.customer_service.shared.repository.TicketEventRepository;
import com.customer_service.shared.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工单自动关闭定时任务
 * 
 * 规则：客服回复工单后，用户超过 48 小时未回复，自动关闭工单并标记为已解决。
 * 执行频率：每小时执行一次
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TicketAutoCloseScheduler {

    private final TicketRepository ticketRepository;
    private final TicketEventRepository ticketEventRepository;

    /**
     * 自动关闭超时未回复的工单 - 每小时执行一次
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void autoCloseStaleTickets() {
        log.info("Start auto-close stale tickets task");

        try {
            // 计算 48 小时前的截止时间
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(AppDefaults.AUTO_CLOSE_TIMEOUT_HOURS);

            // 查找符合条件的工单：
            // 1. 状态为 open 或 processing
            // 2. 最后一条客服回复时间早于 cutoffTime
            // 3. 客服回复后用户没有再回复
            List<Ticket> staleTickets = ticketRepository.findTicketsForAutoClose(cutoffTime);

            if (staleTickets.isEmpty()) {
                log.info("No stale tickets found for auto-close");
                return;
            }

            log.info("Found {} stale tickets to auto-close", staleTickets.size());

            int closedCount = 0;
            for (Ticket ticket : staleTickets) {
                try {
                    // 更新工单状态为已解决
                    String oldStatus = ticket.getStatus();
                    ticket.setStatus(TicketStatus.RESOLVED);
                    ticket.setResolvedAt(LocalDateTime.now());
                    ticket.setUpdatedAt(LocalDateTime.now());
                    ticketRepository.save(ticket);

                    // 记录系统自动关闭事件
                    TicketEvent event = TicketEvent.builder()
                            .ticketId(ticket.getId())
                            .operatorType(OperatorType.SYSTEM)
                            .action(TicketAction.AUTO_CLOSE)
                            .content(String.format(
                                    "系统自动关闭：客服回复后用户超过%d小时未回复，工单状态从 %s 变更为 resolved",
                                    AppDefaults.AUTO_CLOSE_TIMEOUT_HOURS, oldStatus))
                            .createdAt(LocalDateTime.now())
                            .build();
                    ticketEventRepository.save(event);

                    closedCount++;
                    log.debug("Auto-closed ticket: id={}, projectId={}, userId={}, oldStatus={}",
                            ticket.getId(), ticket.getProjectId(), ticket.getUserId(), oldStatus);
                } catch (Exception e) {
                    log.error("Failed to auto-close ticket id={}: {}", ticket.getId(), e.getMessage());
                }
            }

            log.info("Auto-close stale tickets completed: {}/{} tickets closed",
                    closedCount, staleTickets.size());
        } catch (Exception e) {
            log.error("Auto-close stale tickets task failed", e);
        }
    }
}
