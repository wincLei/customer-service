package com.customer_service.admin.controller;

import com.customer_service.admin.dto.UserConversationDTO;
import com.customer_service.admin.service.UserConversationService;
import com.customer_service.shared.annotation.RequirePermission;
import com.customer_service.shared.constant.RoleCode;
import com.customer_service.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作台用户会话控制器
 * 提供客服工作台的用户列表查询接口
 * 
 * 基于 user_conversations 表，替代原有的 conversations 表
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/workbench")
@RequiredArgsConstructor
@RequirePermission(value = "workbench", roles = { RoleCode.ADMIN, RoleCode.AGENT })
public class WorkbenchController {

    private final UserConversationService userConversationService;

    /**
     * 获取用户列表（我的会话/会话列表）
     * 返回多个项目下所有用户的会话列表，按最后消息时间倒序
     * 
     * @param projectIds 项目ID列表（逗号分隔或多个参数）
     * @param page       页码（从0开始，可选，默认0）
     * @param pageSize   每页大小（可选，默认20）
     * @param keyword    搜索关键词（可选，全匹配 uid 或 external_uid）
     */
    @GetMapping("/users")
    public ApiResponse<?> getUserConversations(
            @RequestParam("projectIds") List<Long> projectIds,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword) {
        log.info("Get user conversations for projects: {}, page: {}, pageSize: {}, keyword: {}",
                projectIds, page, pageSize, keyword);

        // 如果提供了分页参数，返回分页结果
        if (page != null && pageSize != null) {
            return ApiResponse
                    .success(userConversationService.getUserConversationsPaged(projectIds, page, pageSize, keyword));
        }

        // 否则返回全部列表（兼容旧接口）
        return ApiResponse.success(userConversationService.getUserConversations(projectIds));
    }

    /**
     * 获取排队中的用户列表
     * 返回有未读消息的用户列表（新用户发来消息但客服还未处理）
     * 
     * @param projectIds 项目ID列表
     */
    @GetMapping("/pending")
    public ApiResponse<Map<String, Object>> getPendingUsers(
            @RequestParam("projectIds") List<Long> projectIds) {
        log.info("Get pending users for projects: {}", projectIds);

        List<UserConversationDTO> pendingUsers = userConversationService.getPendingUserConversations(projectIds);
        long pendingCount = userConversationService.getPendingCount(projectIds);

        Map<String, Object> result = new HashMap<>();
        result.put("users", pendingUsers);
        result.put("count", pendingCount);

        return ApiResponse.success(result);
    }

    /**
     * 标记用户会话为已读
     * 客服查看用户消息后调用此接口清除未读计数
     * 
     * @param userId     用户ID
     * @param messageSeq 消息序号（可选），用于记录已读到哪条消息
     */
    @PostMapping("/users/{userId}/read")
    public ApiResponse<Void> markAsRead(
            @PathVariable Long userId,
            @RequestBody(required = false) MarkAsReadRequest request) {
        Long messageSeq = request != null ? request.messageSeq() : null;
        log.info("Mark user conversation as read: userId={}, messageSeq={}", userId, messageSeq);
        userConversationService.markAsRead(userId, messageSeq);
        return ApiResponse.success(null);
    }

    /**
     * 标记已读请求体
     */
    public record MarkAsReadRequest(Long messageSeq) {
    }

    /**
     * 获取工作台统计数据
     * 
     * @param projectIds 项目ID列表
     */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats(
            @RequestParam("projectIds") List<Long> projectIds) {
        log.info("Get workbench stats for projects: {}", projectIds);

        long pendingCount = userConversationService.getPendingCount(projectIds);
        List<UserConversationDTO> allUsers = userConversationService.getUserConversations(projectIds);

        Map<String, Object> stats = new HashMap<>();
        stats.put("pendingCount", pendingCount);
        stats.put("totalUsers", allUsers.size());

        return ApiResponse.success(stats);
    }
}
