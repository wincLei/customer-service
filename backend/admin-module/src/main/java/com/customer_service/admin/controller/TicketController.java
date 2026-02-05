package com.customer_service.admin.controller;

import com.customer_service.admin.service.TicketService;
import com.customer_service.shared.context.UserContextHolder;
import com.customer_service.shared.dto.ApiResponse;
import com.customer_service.shared.dto.UserContext;
import com.customer_service.shared.entity.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
     * 获取当前用户的项目权限
     * 管理员返回 null（可查看所有），客服返回其关联的项目ID列表
     */
    private Set<Long> getCurrentUserProjectIds() {
        UserContext context = UserContextHolder.getContext();
        if (context == null) {
            return null;
        }
        // 管理员可以查看所有工单
        if (context.isAdmin()) {
            return null;
        }
        // 客服只能查看关联项目的工单
        if (context.getProjectIds() != null && !context.getProjectIds().isEmpty()) {
            return new HashSet<>(context.getProjectIds());
        }
        // 没有关联项目则返回空集合（查不到任何工单）
        return new HashSet<>();
    }

    /**
     * 获取工单列表
     */
    @GetMapping("/list")
    public ApiResponse<?> getTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Set<Long> projectIds = getCurrentUserProjectIds();

        Page<Ticket> tickets = ticketService.getTickets(projectIds, status, priority, keyword, page, size);

        Map<String, Object> result = new HashMap<>();
        result.put("content", tickets.getContent());
        result.put("totalElements", tickets.getTotalElements());
        result.put("totalPages", tickets.getTotalPages());
        result.put("number", tickets.getNumber());
        result.put("size", tickets.getSize());

        return ApiResponse.success(result);
    }

    /**
     * 获取工单详情
     */
    @GetMapping("/{id}")
    public ApiResponse<?> getTicketDetail(@PathVariable Long id) {
        Set<Long> projectIds = getCurrentUserProjectIds();

        return ticketService.getTicketDetail(id, projectIds)
                .map(ApiResponse::success)
                .orElse(ApiResponse.fail(404, "工单不存在"));
    }

    /**
     * 获取指定用户的工单列表
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<Ticket>> getUserTickets(@PathVariable Long userId) {
        Set<Long> projectIds = getCurrentUserProjectIds();
        List<Ticket> tickets = ticketService.getTicketsByUserId(userId, projectIds);
        return ApiResponse.success(tickets);
    }

    /**
     * 回复工单
     */
    @PostMapping("/{id}/reply")
    public ApiResponse<Ticket> replyTicket(
            @PathVariable Long id,
            @RequestBody ReplyRequest request) {

        UserContext context = UserContextHolder.getContext();
        Long operatorId = context != null ? context.getUserId() : 1L;

        Ticket ticket = ticketService.replyTicket(id, operatorId, request.content());
        return ApiResponse.success(ticket);
    }

    /**
     * 更新工单状态
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Ticket> updateStatus(
            @PathVariable Long id,
            @RequestBody StatusRequest request) {

        UserContext context = UserContextHolder.getContext();
        Long operatorId = context != null ? context.getUserId() : 1L;

        Ticket ticket = ticketService.updateStatus(id, operatorId, request.status());
        return ApiResponse.success(ticket);
    }

    /**
     * 分配工单
     */
    @PutMapping("/{id}/assign")
    public ApiResponse<Ticket> assignTicket(
            @PathVariable Long id,
            @RequestBody AssignRequest request) {

        UserContext context = UserContextHolder.getContext();
        Long operatorId = context != null ? context.getUserId() : 1L;

        Ticket ticket = ticketService.assignTicket(id, operatorId, request.assigneeId());
        return ApiResponse.success(ticket);
    }

    /**
     * 获取工单统计
     */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Long>> getStats() {
        Set<Long> projectIds = getCurrentUserProjectIds();

        Map<String, Long> stats = ticketService.getTicketStats(projectIds);
        return ApiResponse.success(stats);
    }

    public record ReplyRequest(String content) {
    }

    public record StatusRequest(String status) {
    }

    public record AssignRequest(Long assigneeId) {
    }
}
