package com.customer_service.admin.controller;

import com.customer_service.admin.service.AgentService;
import com.customer_service.shared.annotation.RequirePermission;
import com.customer_service.shared.constant.RoleCode;
import com.customer_service.shared.dto.ApiResponse;
import com.customer_service.shared.entity.Agent;
import com.customer_service.shared.entity.SysUser;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 客服管理控制器
 * 管理客服信息和项目关联
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/agents")
@RequiredArgsConstructor
@RequirePermission(value = "agent:manage", roles = { RoleCode.ADMIN })
public class AgentController {

    private final AgentService agentService;

    /**
     * 获取所有客服列表
     */
    @GetMapping
    public ApiResponse<?> listAgents() {
        List<Agent> agents = agentService.getAllAgents();
        List<Map<String, Object>> result = agents.stream()
                .map(this::toAgentVO)
                .collect(Collectors.toList());
        return ApiResponse.success(result);
    }

    /**
     * 获取可以成为客服的用户列表
     */
    @GetMapping("/available-users")
    public ApiResponse<?> getAvailableUsers() {
        List<SysUser> users = agentService.getAvailableUsersForAgent();
        List<Map<String, Object>> result = users.stream()
                .map(this::toUserVO)
                .collect(Collectors.toList());
        return ApiResponse.success(result);
    }

    /**
     * 获取单个客服详情
     */
    @GetMapping("/{id}")
    public ApiResponse<?> getAgent(@PathVariable Long id) {
        return agentService.getAgentInfo(id)
                .map(agent -> ApiResponse.success(toAgentVO(agent)))
                .orElse(ApiResponse.error("客服不存在"));
    }

    /**
     * 创建客服
     */
    @PostMapping
    public ApiResponse<?> createAgent(@RequestBody CreateAgentRequest request) {
        if (request.getUserId() == null) {
            return ApiResponse.error("请选择用户");
        }

        try {
            Agent agent = agentService.createAgent(
                    request.getUserId(),
                    request.getNickname(),
                    request.getMaxLoad(),
                    request.getWelcomeMessage(),
                    request.getAutoReplyEnabled(),
                    request.getProjectIds());
            log.info("创建客服成功: {} (ID: {})", agent.getNickname(), agent.getId());
            return ApiResponse.success(toAgentVO(agent));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新客服
     */
    @PutMapping("/{id}")
    public ApiResponse<?> updateAgent(@PathVariable Long id, @RequestBody UpdateAgentRequest request) {
        try {
            Agent agent = agentService.updateAgent(
                    id,
                    request.getNickname(),
                    request.getMaxLoad(),
                    request.getWelcomeMessage(),
                    request.getAutoReplyEnabled(),
                    request.getProjectIds());
            log.info("更新客服成功: {} (ID: {})", agent.getNickname(), agent.getId());
            return ApiResponse.success(toAgentVO(agent));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除客服
     */
    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteAgent(@PathVariable Long id) {
        try {
            agentService.deleteAgent(id);
            log.info("删除客服成功: ID={}", id);
            return ApiResponse.success("删除成功");
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新客服工作状态
     */
    @PostMapping("/{id}/status")
    public ApiResponse<?> updateWorkStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        try {
            Agent agent = agentService.updateWorkStatus(id, request.getWorkStatus());
            log.info("更新客服状态成功: {} -> {}", agent.getNickname(), agent.getWorkStatus());
            return ApiResponse.success(toAgentVO(agent));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 转换为客服VO
     */
    private Map<String, Object> toAgentVO(Agent agent) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", agent.getId());
        vo.put("userId", agent.getUserId());
        vo.put("nickname", agent.getNickname());
        vo.put("workStatus", agent.getWorkStatus());
        vo.put("maxLoad", agent.getMaxLoad());
        vo.put("currentLoad", agent.getCurrentLoad());
        vo.put("welcomeMessage", agent.getWelcomeMessage());
        vo.put("autoReplyEnabled", agent.getAutoReplyEnabled());
        vo.put("createdAt", agent.getCreatedAt());
        vo.put("updatedAt", agent.getUpdatedAt());

        // 添加用户信息
        if (agent.getSysUser() != null) {
            Map<String, Object> userVO = new HashMap<>();
            userVO.put("id", agent.getSysUser().getId());
            userVO.put("username", agent.getSysUser().getUsername());
            userVO.put("email", agent.getSysUser().getEmail());
            userVO.put("avatar", agent.getSysUser().getAvatar());
            vo.put("user", userVO);
        }

        // 添加关联的项目ID列表
        List<Long> projectIds = agentService.getAgentProjectIds(agent.getId());
        vo.put("projectIds", projectIds);

        return vo;
    }

    /**
     * 转换为用户VO
     */
    private Map<String, Object> toUserVO(SysUser user) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", user.getId());
        vo.put("username", user.getUsername());
        vo.put("email", user.getEmail());
        vo.put("avatar", user.getAvatar());
        return vo;
    }

    @Data
    public static class CreateAgentRequest {
        private Long userId;
        private String nickname;
        private Integer maxLoad;
        private String welcomeMessage;
        private Boolean autoReplyEnabled;
        private List<Long> projectIds;
    }

    @Data
    public static class UpdateAgentRequest {
        private String nickname;
        private Integer maxLoad;
        private String welcomeMessage;
        private Boolean autoReplyEnabled;
        private List<Long> projectIds;
    }

    @Data
    public static class UpdateStatusRequest {
        private String workStatus;
    }
}
