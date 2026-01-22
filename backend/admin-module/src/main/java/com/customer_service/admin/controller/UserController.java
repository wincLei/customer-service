package com.customer_service.admin.controller;

import com.customer_service.admin.service.UserService;
import com.customer_service.shared.entity.User;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取用户信息
     */
    @GetMapping("/{uid}")
    public User getUserInfo(@PathVariable String uid, @RequestParam Long projectId) {
        return userService.getUserByUid(projectId, uid);
    }

    /**
     * 获取用户标签
     */
    @GetMapping("/{userId}/tags")
    public Map<String, Object> getUserTags(@PathVariable Long userId) {
        List<String> tags = userService.getUserTags(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("tags", tags);

        return result;
    }

    /**
     * 添加用户标签
     */
    @PostMapping("/{userId}/tags")
    public void addUserTag(@PathVariable Long userId, @RequestBody AddTagRequest request) {
        userService.addUserTag(request.getProjectId(), userId, request.getTagName(), request.getTaggedBy());
    }

    /**
     * 删除用户标签
     */
    @DeleteMapping("/{userId}/tags/{tagName}")
    public void removeUserTag(@PathVariable Long userId, @PathVariable String tagName) {
        userService.removeUserTag(userId, tagName);
    }

    /**
     * 获取客服列表
     */
    @GetMapping("/agents")
    public Map<String, Object> getAgentList(@RequestParam Long projectId) {
        List<Map<String, Object>> agents = userService.getAgentList(projectId);

        Map<String, Object> result = new HashMap<>();
        result.put("agents", agents);

        return result;
    }

    @Data
    static class AddTagRequest {
        private Long projectId;
        private String tagName;
        private Long taggedBy;
    }
}
