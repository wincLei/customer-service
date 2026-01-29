package com.customer_service.portal.controller;

import com.customer_service.portal.service.WebhookService;
import com.customer_service.shared.dto.WKMessageNotify;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * WuKongIM Webhook 回调控制器
 * 接收来自 WuKongIM 的各类事件通知
 * 
 * 注意：此接口仅供 WuKongIM 服务调用，不对外暴露
 */
@RestController
@RequestMapping("/internal/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final WebhookService webhookService;

    /**
     * WuKongIM 事件回调入口
     * 根据 event 参数路由到不同的处理逻辑
     * 
     * @param event 事件类型: msg.notify, msg.offline, user.onlinestatus
     */
    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestParam("event") String event,
            @RequestBody String body) {

        log.info("Received webhook event: {}, body length: {}", event, body.length());

        try {
            switch (event) {
                case "msg.notify":
                    // 所有消息通知
                    webhookService.handleMessageNotify(body);
                    break;
                case "msg.offline":
                    // 离线消息通知（需要推送）
                    webhookService.handleOfflineMessage(body);
                    break;
                case "user.onlinestatus":
                    // 用户在线状态变更
                    webhookService.handleUserOnlineStatus(body);
                    break;
                default:
                    log.warn("Unknown webhook event: {}", event);
            }
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Failed to process webhook event: {}", event, e);
            // 返回 200 避免 WuKongIM 重试（可根据业务需求调整）
            return ResponseEntity.ok("ERROR: " + e.getMessage());
        }
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
