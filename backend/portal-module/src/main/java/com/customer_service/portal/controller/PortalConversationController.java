package com.customer_service.portal.controller;

import com.customer_service.portal.service.PortalConversationService;
import com.customer_service.portal.service.PortalMessageService;
import com.customer_service.shared.dto.ApiResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Portal 会话控制器
 */
@RestController
@RequestMapping("/api/portal")
@RequiredArgsConstructor
public class PortalConversationController {

    private final PortalConversationService conversationService;
    private final PortalMessageService messageService;

    /**
     * 会话初始化请求
     */
    @Data
    public static class ConversationInitRequest {
        private Long projectId;
        private Long userId;
    }

    /**
     * 初始化会话
     * 返回会话信息，包括已分配的客服 UID（如果有）
     */
    @PostMapping("/conversation/init")
    public ApiResponse<Map<String, Object>> initConversation(@RequestBody ConversationInitRequest request) {
        if (request.getProjectId() == null || request.getUserId() == null) {
            return ApiResponse.fail(400, "项目ID和用户ID不能为空");
        }

        var result = conversationService.initOrGetConversation(request.getProjectId(), request.getUserId());

        Map<String, Object> data = new HashMap<>();
        data.put("id", result.conversation().getId());
        data.put("status", result.conversation().getStatus());
        data.put("isNew", result.isNew());
        data.put("welcomeMessage", result.welcomeMessage());

        // 返回已分配客服的 UID（用于 Personal Channel 通信）
        if (result.conversation().getAgentId() != null) {
            data.put("agentId", result.conversation().getAgentId());
            data.put("agentUid", "agent_" + result.conversation().getAgentId());
        }

        return ApiResponse.success(data);
    }

    /**
     * 获取会话消息历史
     */
    @GetMapping("/conversation/{conversationId}/messages")
    public ApiResponse<List<PortalMessageService.MessageDTO>> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "100") int limit) {

        var messages = messageService.getConversationMessages(conversationId, limit);
        return ApiResponse.success(messages);
    }

    /**
     * 结束会话
     */
    @PostMapping("/conversation/{conversationId}/end")
    public ApiResponse<Void> endConversation(@PathVariable Long conversationId) {
        conversationService.endConversation(conversationId);
        return ApiResponse.success(null);
    }

    /**
     * 消息发送请求
     */
    @Data
    public static class SendMessageRequest {
        private Long conversationId;
        private Long userId;
        private String userUid; // 用户 UID，用于 IM 频道
        private String msgType;
        private String content;
    }

    /**
     * 消息发送响应
     */
    @Data
    public static class SendMessageResponse {
        private PortalMessageService.MessageDTO message;
        private boolean channelCreated;

        public SendMessageResponse(PortalMessageService.MessageSendResult result) {
            this.message = result.message();
            this.channelCreated = result.channelCreated();
        }
    }

    /**
     * 发送消息
     */
    @PostMapping("/message/send")
    public ApiResponse<SendMessageResponse> sendMessage(@RequestBody SendMessageRequest request) {
        if (request.getConversationId() == null || request.getUserId() == null) {
            return ApiResponse.fail(400, "会话ID和用户ID不能为空");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            return ApiResponse.fail(400, "消息内容不能为空");
        }

        String msgType = request.getMsgType();
        if (msgType == null || msgType.isBlank()) {
            msgType = "text";
        }

        var result = messageService.sendMessage(
                request.getConversationId(),
                request.getUserId(),
                request.getUserUid(),
                msgType,
                request.getContent());

        return ApiResponse.success(new SendMessageResponse(result));
    }
}
