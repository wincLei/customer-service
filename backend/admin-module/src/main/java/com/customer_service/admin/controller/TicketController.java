package com.customer_service.admin.controller;

import com.customer_service.admin.service.TicketService;
import com.customer_service.shared.entity.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 工单管理接口（Admin端）
 */
@RestController
@RequestMapping("/api/admin/ticket")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    /**
     * 获取工单列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        // TODO: 从用户信息获取项目权限列表
        // 管理员传 null，客服传其有权限的项目ID列表
        Set<Long> projectIds = null;

        Page<Ticket> tickets = ticketService.getTickets(projectIds, status, priority, keyword, page, size);

        Map<String, Object> result = new HashMap<>();
        result.put("content", tickets.getContent());
        result.put("totalElements", tickets.getTotalElements());
        result.put("totalPages", tickets.getTotalPages());
        result.put("number", tickets.getNumber());
        result.put("size", tickets.getSize());

        return ResponseEntity.ok(result);
    }

    /**
     * 获取工单详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTicketDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Set<Long> projectIds = null; // TODO: 权限过滤

        return ticketService.getTicketDetail(id, projectIds)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 回复工单
     */
    @PostMapping("/{id}/reply")
    public ResponseEntity<Ticket> replyTicket(
            @PathVariable Long id,
            @RequestBody ReplyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        // TODO: 获取当前操作者ID
        Long operatorId = 1L;

        Ticket ticket = ticketService.replyTicket(id, operatorId, request.content());
        return ResponseEntity.ok(ticket);
    }

    /**
     * 更新工单状态
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Ticket> updateStatus(
            @PathVariable Long id,
            @RequestBody StatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long operatorId = 1L;

        Ticket ticket = ticketService.updateStatus(id, operatorId, request.status());
        return ResponseEntity.ok(ticket);
    }

    /**
     * 分配工单
     */
    @PutMapping("/{id}/assign")
    public ResponseEntity<Ticket> assignTicket(
            @PathVariable Long id,
            @RequestBody AssignRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long operatorId = 1L;

        Ticket ticket = ticketService.assignTicket(id, operatorId, request.assigneeId());
        return ResponseEntity.ok(ticket);
    }

    /**
     * 获取工单统计
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats(
            @AuthenticationPrincipal UserDetails userDetails) {

        Set<Long> projectIds = null;

        Map<String, Long> stats = ticketService.getTicketStats(projectIds);
        return ResponseEntity.ok(stats);
    }

    public record ReplyRequest(String content) {
    }

    public record StatusRequest(String status) {
    }

    public record AssignRequest(Long assigneeId) {
    }
}
