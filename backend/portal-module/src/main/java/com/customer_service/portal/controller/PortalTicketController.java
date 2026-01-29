package com.customer_service.portal.controller;

import com.customer_service.portal.service.PortalTicketService;
import com.customer_service.shared.dto.ApiResponse;
import com.customer_service.shared.entity.Ticket;
import com.customer_service.shared.entity.TicketEvent;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Portal 工单控制器
 */
@RestController
@RequestMapping("/api/portal/ticket")
@RequiredArgsConstructor
public class PortalTicketController {

    private final PortalTicketService ticketService;

    /**
     * 创建工单请求
     */
    @Data
    public static class CreateTicketRequest {
        private Long projectId;
        private Long userId;
        private String title;
        private String description;
        private String contactInfo;
        private String priority;
    }

    /**
     * 创建工单
     */
    @PostMapping("/create")
    public ApiResponse<Map<String, Object>> createTicket(@RequestBody CreateTicketRequest request) {
        if (request.getProjectId() == null || request.getUserId() == null) {
            return ApiResponse.fail(400, "项目ID和用户ID不能为空");
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            return ApiResponse.fail(400, "工单标题不能为空");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            return ApiResponse.fail(400, "工单描述不能为空");
        }

        Ticket ticket = ticketService.createTicket(
                request.getProjectId(),
                request.getUserId(),
                request.getTitle(),
                request.getDescription(),
                request.getContactInfo(),
                request.getPriority());

        Map<String, Object> data = new HashMap<>();
        data.put("id", ticket.getId());
        data.put("title", ticket.getTitle());
        data.put("status", ticket.getStatus());
        data.put("createdAt", ticket.getCreatedAt());

        return ApiResponse.success(data);
    }

    /**
     * 获取用户工单列表
     */
    @GetMapping("/list")
    public ApiResponse<List<Ticket>> getUserTickets(@RequestParam Long userId) {
        List<Ticket> tickets = ticketService.getUserTickets(userId);
        return ApiResponse.success(tickets);
    }

    /**
     * 获取工单详情
     */
    @GetMapping("/{ticketId}")
    public ApiResponse<Map<String, Object>> getTicketDetail(@PathVariable Long ticketId) {
        Ticket ticket = ticketService.getTicket(ticketId);
        if (ticket == null) {
            return ApiResponse.fail(404, "工单不存在");
        }

        List<TicketEvent> events = ticketService.getTicketEvents(ticketId);

        Map<String, Object> data = new HashMap<>();
        data.put("ticket", ticket);
        data.put("events", events);

        return ApiResponse.success(data);
    }

    /**
     * 工单回复请求
     */
    @Data
    public static class ReplyTicketRequest {
        private Long userId;
        private String content;
    }

    /**
     * 用户回复工单
     */
    @PostMapping("/{ticketId}/reply")
    public ApiResponse<TicketEvent> replyTicket(
            @PathVariable Long ticketId,
            @RequestBody ReplyTicketRequest request) {

        if (request.getUserId() == null) {
            return ApiResponse.fail(400, "用户ID不能为空");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            return ApiResponse.fail(400, "回复内容不能为空");
        }

        try {
            TicketEvent event = ticketService.replyTicket(ticketId, request.getUserId(), request.getContent());
            return ApiResponse.success(event);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }
}
