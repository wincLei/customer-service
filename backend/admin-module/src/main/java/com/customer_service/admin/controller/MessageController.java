package com.customer_service.admin.controller;

import com.customer_service.admin.service.MessageService;
import com.customer_service.shared.annotation.RequirePermission;
import com.customer_service.shared.constant.RoleCode;
import com.customer_service.shared.entity.Message;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息管理控制器
 * 客服和管理员可访问
 */
@RestController
@RequestMapping("/api/admin/messages")
@RequiredArgsConstructor
@RequirePermission(value = "workbench", roles = { RoleCode.ADMIN, RoleCode.AGENT })
public class MessageController {

    private final MessageService messageService;

    /**
     * 获取会话的消息列表
     */
    @GetMapping("/conversation/{conversationId}")
    public List<Message> getMessages(@PathVariable Long conversationId) {
        return messageService.getConversationMessages(conversationId);
    }

    /**
     * 分页获取消息（用于加载更多）
     */
    @GetMapping("/conversation/{conversationId}/page")
    public Page<Message> getMessagesPage(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return messageService.getConversationMessagesPage(conversationId, page, size);
    }

    /**
     * 发送消息
     */
    @PostMapping("/send")
    public Message sendMessage(@RequestBody SendMessageRequest request) {
        return messageService.sendMessage(
                request.getProjectId(),
                request.getConversationId(),
                request.getSenderId(),
                request.getSenderType(),
                request.getContentType(),
                request.getContent());
    }

    @Data
    static class SendMessageRequest {
        private Long projectId;
        private Long conversationId;
        private Long senderId;
        private String senderType;
        private String contentType;
        private String content;
    }
}
