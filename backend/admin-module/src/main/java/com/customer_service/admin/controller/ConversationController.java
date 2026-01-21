package com.customer_service.admin.controller;

import com.customer_service.admin.repository.ConversationRepository;
import com.customer_service.shared.dto.ApiResponse;
import com.customer_service.shared.entity.Conversation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/conversations")
@RequiredArgsConstructor
public class ConversationController {
    
    private final ConversationRepository conversationRepository;

    @GetMapping("/pending")
    public ApiResponse<List<Conversation>> getPendingConversations(
            @RequestParam Long projectId) {
        log.info("Get pending conversations for project: {}", projectId);
        List<Conversation> conversations = conversationRepository.findByProjectIdAndStatus(
                projectId, "queued");
        return ApiResponse.success(conversations);
    }

    @GetMapping("/my")
    public ApiResponse<List<Conversation>> getMyConversations(
            @RequestParam Long agentId) {
        log.info("Get conversations for agent: {}", agentId);
        List<Conversation> conversations = conversationRepository.findByAgentIdAndStatus(
                agentId, "active");
        return ApiResponse.success(conversations);
    }

    @PostMapping("/{id}/close")
    public ApiResponse<?> closeConversation(
            @PathVariable Long id,
            @RequestBody CloseConversationRequest request) {
        log.info("Close conversation: {}", id);
        // TODO: 实现关闭会话逻辑
        return ApiResponse.success("Conversation closed successfully");
    }

    public static class CloseConversationRequest {
        private Integer score;
        private String remark;

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }
}
