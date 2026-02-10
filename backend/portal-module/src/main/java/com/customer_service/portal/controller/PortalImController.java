package com.customer_service.portal.controller;

import com.customer_service.shared.constant.DeviceType;
import com.customer_service.shared.service.WuKongIMService;
import com.customer_service.shared.util.I18nUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IM Token 接口
 */
@RestController
@RequestMapping("/api/portal/im")
@RequiredArgsConstructor
public class PortalImController {

    private final WuKongIMService wuKongIMService;

    /**
     * 同步历史消息（代理 WuKongIM API）
     */
    @PostMapping("/messages/sync")
    public ResponseEntity<Map<String, Object>> syncMessages(@RequestBody MessageSyncRequest request) {
        Map<String, Object> result = new HashMap<>();

        if (request.loginUid() == null || request.channelId() == null) {
            result.put("code", 400);
            result.put("message", I18nUtil.getMessage("common.params.incomplete"));
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

        result.put("code", 0);
        result.put("data", messages);
        return ResponseEntity.ok(result);
    }

    public record TokenRequest(String uid, String name, Integer deviceFlag) {
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
