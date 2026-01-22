package com.customer_service.admin.controller;

import com.customer_service.admin.service.ConversationService;
import com.customer_service.shared.dto.ApiResponse;
import com.customer_service.shared.entity.Conversation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * 获取排队中的会话列表
     */
    @GetMapping("/pending")
    public ApiResponse<Map<String, Object>> getPendingConversations(@RequestParam Long projectId) {
        log.info("Get pending conversations for project: {}", projectId);
        List<Conversation> conversations = conversationService.getQueuedConversations(projectId);
        Long queueCount = conversationService.getQueueCount(projectId);

        Map<String, Object> result = new HashMap<>();
        result.put("conversations", conversations);
        result.put("queueCount", queueCount);

        return ApiResponse.success(result);
    }

    /**
     * 获取当前客服的会话列表
     */
    @GetMapping("/my")
    public ApiResponse<List<Conversation>> getMyConversations(@RequestParam Long agentId) {
        log.info("Get conversations for agent: {}", agentId);
        return ApiResponse.success(conversationService.getAgentConversations(agentId));
    }

    /**
     * 接入会话
     */
    @PostMapping("/{id}/accept")
    public ApiResponse<Conversation> acceptConversation(@PathVariable Long id, @RequestParam Long agentId) {
        log.info("Agent {} accepting conversation {}", agentId, id);
        return ApiResponse.success(conversationService.acceptConversation(id, agentId));
    }

    /**
     * 结束会话
     */
    @PostMapping("/{id}/close")
    public ApiResponse<Conversation> closeConversation(@PathVariable Long id) {
        log.info("Closing conversation {}", id);
        return ApiResponse.success(conversationService.closeConversation(id));
    }

    /**
     * 获取统计数据
     */
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getStatistics(@RequestParam Long projectId) {
        log.info("Get statistics for project: {}", projectId);
        return ApiResponse.success(conversationService.getStatistics(projectId));
    }
}
