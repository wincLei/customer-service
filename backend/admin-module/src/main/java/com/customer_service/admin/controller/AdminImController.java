package com.customer_service.admin.controller;

import com.customer_service.shared.constant.DeviceType;
import com.customer_service.shared.service.WuKongIMService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin 端 IM 控制器
 * 处理客服端的 IM Token 获取
 */
@RestController
@RequestMapping("/api/admin/im")
@RequiredArgsConstructor
public class AdminImController {

    private final WuKongIMService wuKongIMService;

    /**
     * 获取客服的 IM Token
     */
    @PostMapping("/token")
    public ResponseEntity<Map<String, Object>> getToken(@RequestBody TokenRequest request) {
        Map<String, Object> result = new HashMap<>();

        if (request.uid() == null || request.uid().isBlank()) {
            result.put("success", false);
            result.put("error", "客服 UID 不能为空");
            return ResponseEntity.badRequest().body(result);
        }

        String name = request.name() != null ? request.name() : "客服";
        int deviceFlag = request.deviceFlag() != null ? request.deviceFlag() : DeviceType.WEB; // 默认 WEB

        WuKongIMService.ImTokenResult tokenResult = wuKongIMService.getToken(request.uid(), name, deviceFlag);

        if (tokenResult.success()) {
            result.put("success", true);
            result.put("token", tokenResult.token());
            return ResponseEntity.ok(result);
        } else {
            result.put("success", false);
            result.put("error", tokenResult.error());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 创建会话频道（当客服接入会话时调用）
     */
    @PostMapping("/channel/create")
    public ResponseEntity<Map<String, Object>> createChannel(@RequestBody ChannelRequest request) {
        Map<String, Object> result = new HashMap<>();

        if (request.channelId() == null || request.channelId().isBlank()) {
            result.put("success", false);
            result.put("error", "频道 ID 不能为空");
            return ResponseEntity.badRequest().body(result);
        }

        // 创建频道并添加订阅者
        boolean created = wuKongIMService.createChannel(
                request.channelId(),
                2, // 群组类型
                request.subscribers() != null ? request.subscribers() : new String[] {});

        result.put("success", created);
        if (!created) {
            result.put("error", "创建频道失败");
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 添加订阅者到频道
     */
    @PostMapping("/channel/subscribe")
    public ResponseEntity<Map<String, Object>> addSubscriber(@RequestBody SubscribeRequest request) {
        Map<String, Object> result = new HashMap<>();

        boolean success = wuKongIMService.addChannelSubscribers(
                request.channelId(),
                2,
                new String[] { request.uid() });

        result.put("success", success);
        return ResponseEntity.ok(result);
    }

    // 注意: subscribe-queue 端点已废弃，访客频道模式下客服在用户初始化会话时自动被添加为订阅者

    public record TokenRequest(String uid, String name, Integer deviceFlag) {
    }

    public record ChannelRequest(String channelId, String[] subscribers) {
    }

    public record SubscribeRequest(String channelId, String uid) {
    }

    /**
     * 同步历史消息（代理 WuKongIM API）
     */
    @PostMapping("/messages/sync")
    public ResponseEntity<Map<String, Object>> syncMessages(@RequestBody MessageSyncRequest request) {
        Map<String, Object> result = new HashMap<>();

        if (request.loginUid() == null || request.channelId() == null) {
            result.put("code", 400);
            result.put("message", "参数不完整");
            return ResponseEntity.badRequest().body(result);
        }

        int limit = request.limit() != null ? request.limit() : 50;
        long startSeq = request.startMessageSeq() != null ? request.startMessageSeq() : 0;
        long endSeq = request.endMessageSeq() != null ? request.endMessageSeq() : 0;
        int pullMode = request.pullMode() != null ? request.pullMode() : 0;

        List<Map<String, Object>> messages = wuKongIMService.syncMessages(
                request.loginUid(),
                request.channelId(),
                request.channelType(),
                startSeq,
                endSeq,
                limit,
                pullMode);

        result.put("code", 200);
        result.put("data", messages);
        return ResponseEntity.ok(result);
    }

    public record MessageSyncRequest(
            String loginUid,
            String channelId,
            int channelType,
            Long startMessageSeq,
            Long endMessageSeq,
            Integer limit,
            Integer pullMode) {
    }
}
