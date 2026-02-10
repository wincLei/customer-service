package com.customer_service.shared.service;

import com.customer_service.shared.constant.DeviceType;
import com.customer_service.shared.util.I18nUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * WuKongIM 服务
 * 负责与 WuKongIM 服务器通信
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WuKongIMService {

    private final RestTemplate restTemplate;

    @Value("${wukongim.api-url:http://localhost:5001}")
    private String apiUrl;

    @Value("${wukongim.app-key:}")
    private String appKey;

    // 系统用户是否已初始化
    private volatile boolean systemUserInitialized = false;

    /**
     * 确保系统用户存在（用于发送系统通知）
     */
    public void ensureSystemUser() {
        if (systemUserInitialized) {
            return;
        }
        try {
            // 注册系统用户 token
            getToken("system", I18nUtil.getMessage("im.system.name"), 2); // deviceFlag=2 for WEB
            systemUserInitialized = true;
            log.info("System user initialized for WuKongIM");
        } catch (Exception e) {
            log.warn("Failed to initialize system user: {}", e.getMessage());
        }
    }

    /**
     * 获取用户连接 Token
     *
     * @param uid        用户ID
     * @param name       用户名称
     * @param deviceFlag 设备类型 (1:APP, 2:WEB) - 必须与前端 SDK 连接时使用的值一致
     * @return IM Token
     */
    public ImTokenResult getToken(String uid, String name, int deviceFlag) {
        try {
            String url = apiUrl + "/user/token";

            // 生成用于 SDK 连接的 token（可以使用 UUID 或其他唯一标识）
            String userToken = java.util.UUID.randomUUID().toString().replace("-", "");

            Map<String, Object> body = new HashMap<>();
            body.put("uid", uid);
            body.put("token", userToken); // WuKongIM 要求必须传入 token
            body.put("device_flag", deviceFlag); // 使用前端传入的设备类型
            body.put("device_level", 1); // 主设备

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (appKey != null && !appKey.isBlank()) {
                headers.set("token", appKey);
                log.debug("Using appKey for WuKongIM auth: {}",
                        appKey.substring(0, Math.min(10, appKey.length())) + "...");
            } else {
                log.warn("WuKongIM appKey is not configured! API calls may fail with 401.");
            }

            log.debug("Calling WuKongIM API: {} with uid: {}, deviceFlag: {} ({})",
                    url, uid, deviceFlag, DeviceType.getName(deviceFlag));
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class);

            log.info("WuKongIM API response: status={}, body={}", response.getStatusCode(), response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                // WuKongIM 返回 {"status": 200} 或 {"status": "ok"} 表示成功
                // 我们返回自己生成的 token 给客户端用于 SDK 连接
                log.info("IM token registered for user: {}, generated token: {}", uid,
                        userToken.substring(0, 8) + "...");
                return new ImTokenResult(true, userToken, null);
            } else {
                log.error("Failed to get IM token: {}", response.getStatusCode());
                return new ImTokenResult(false, null, I18nUtil.getMessage("im.token.error"));
            }
        } catch (Exception e) {
            log.error("Error getting IM token for user: " + uid, e);
            return new ImTokenResult(false, null, I18nUtil.getMessage("im.connect.error", e.getMessage()));
        }
    }

    /**
     * 创建频道
     *
     * @param channelId   频道 ID
     * @param channelType 频道类型（1:个人 2:群组）
     * @param subscribers 订阅者列表
     * @return 是否成功
     */
    public boolean createChannel(String channelId, int channelType, String[] subscribers) {
        try {
            String url = apiUrl + "/channel";

            Map<String, Object> body = new HashMap<>();
            body.put("channel_id", channelId);
            body.put("channel_type", channelType);
            body.put("subscribers", subscribers);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (!appKey.isBlank()) {
                headers.set("token", appKey);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Error creating channel: " + channelId, e);
            return false;
        }
    }

    /**
     * 发送消息
     *
     * @param fromUid     发送者 UID
     * @param channelId   频道 ID
     * @param channelType 频道类型
     * @param content     消息内容（JSON字符串）
     * @return 是否成功
     */
    public boolean sendMessage(String fromUid, String channelId, int channelType, String content) {
        try {
            String url = apiUrl + "/message/send";

            Map<String, Object> body = new HashMap<>();
            body.put("header", Map.of("red_dot", 1, "no_persist", 0));
            body.put("from_uid", fromUid);
            body.put("channel_id", channelId);
            body.put("channel_type", channelType);
            body.put("payload", content);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (!appKey.isBlank()) {
                headers.set("token", appKey);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Error sending message", e);
            return false;
        }
    }

    /**
     * 发送文本消息（便捷方法）
     *
     * @param fromUid     发送者 UID
     * @param channelId   频道 ID
     * @param channelType 频道类型
     * @param text        文本内容
     * @return 是否成功
     */
    public boolean sendTextMessage(String fromUid, String channelId, int channelType, String text) {
        try {
            // 确保系统用户已初始化（如果是系统发送的消息）
            if ("system".equals(fromUid)) {
                ensureSystemUser();
            }
            // 构建消息载荷
            String payloadJson = String.format("{\"type\":1,\"content\":\"%s\"}", escapeJson(text));
            // Base64 编码
            String payload = java.util.Base64.getEncoder().encodeToString(payloadJson.getBytes());
            return sendMessage(fromUid, channelId, channelType, payload);
        } catch (Exception e) {
            log.error("Error sending text message", e);
            return false;
        }
    }

    /**
     * 发送图片消息
     */
    public boolean sendImageMessage(String fromUid, String channelId, int channelType, String imageUrl) {
        try {
            String payloadJson = String.format("{\"type\":2,\"url\":\"%s\"}", escapeJson(imageUrl));
            String payload = java.util.Base64.getEncoder().encodeToString(payloadJson.getBytes());
            return sendMessage(fromUid, channelId, channelType, payload);
        } catch (Exception e) {
            log.error("Error sending image message", e);
            return false;
        }
    }

    /**
     * 添加频道订阅者
     *
     * @param channelId   频道 ID
     * @param channelType 频道类型
     * @param subscribers 订阅者列表
     * @return 是否成功
     */
    public boolean addChannelSubscribers(String channelId, int channelType, String[] subscribers) {
        try {
            String url = apiUrl + "/channel/subscriber_add";

            Map<String, Object> body = new HashMap<>();
            body.put("channel_id", channelId);
            body.put("channel_type", channelType);
            body.put("subscribers", subscribers);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (!appKey.isBlank()) {
                headers.set("token", appKey);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            log.info("Add channel subscribers: channelId={}, subscribers={}, success={}",
                    channelId, subscribers, response.getStatusCode().is2xxSuccessful());
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Error adding channel subscribers: " + channelId, e);
            return false;
        }
    }

    /**
     * 移除频道订阅者
     */
    public boolean removeChannelSubscribers(String channelId, int channelType, String[] subscribers) {
        try {
            String url = apiUrl + "/channel/subscriber_remove";

            Map<String, Object> body = new HashMap<>();
            body.put("channel_id", channelId);
            body.put("channel_type", channelType);
            body.put("subscribers", subscribers);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (!appKey.isBlank()) {
                headers.set("token", appKey);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Error removing channel subscribers: " + channelId, e);
            return false;
        }
    }

    /**
     * 转义 JSON 字符串
     */
    private String escapeJson(String text) {
        if (text == null)
            return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 同步频道消息（获取历史消息）
     *
     * @param loginUid        当前登录用户 UID
     * @param channelId       频道 ID
     * @param channelType     频道类型（1:个人 2:群组）
     * @param startMessageSeq 开始消息序号
     * @param endMessageSeq   结束消息序号
     * @param limit           限制数量
     * @param pullMode        拉取模式（0:向下 1:向上）
     * @return 消息列表
     */
    public java.util.List<Map<String, Object>> syncMessages(String loginUid, String channelId, int channelType,
            long startMessageSeq, long endMessageSeq,
            int limit, int pullMode) {
        try {
            String url = apiUrl + "/channel/messagesync";

            Map<String, Object> body = new HashMap<>();
            body.put("login_uid", loginUid);
            body.put("channel_id", channelId);
            body.put("channel_type", channelType);
            body.put("start_message_seq", startMessageSeq);
            body.put("end_message_seq", endMessageSeq);
            body.put("limit", limit);
            body.put("pull_mode", pullMode);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (appKey != null && !appKey.isBlank()) {
                headers.set("token", appKey);
            }

            log.debug("Syncing messages: loginUid={}, channelId={}, channelType={}, limit={}",
                    loginUid, channelId, channelType, limit);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            // WuKongIM API 可能返回对象或数组，先用 Object 接收
            ResponseEntity<Object> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Object.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object responseBody = response.getBody();
                java.util.List<Map<String, Object>> messages;

                // 处理不同的响应格式
                if (responseBody instanceof java.util.List) {
                    // 直接是数组
                    messages = (java.util.List<Map<String, Object>>) responseBody;
                } else if (responseBody instanceof Map) {
                    // 是对象，可能有 messages 字段或者是错误响应
                    Map<String, Object> responseMap = (Map<String, Object>) responseBody;
                    if (responseMap.containsKey("messages")) {
                        Object messagesObj = responseMap.get("messages");
                        if (messagesObj instanceof java.util.List) {
                            messages = (java.util.List<Map<String, Object>>) messagesObj;
                        } else {
                            log.warn("Unexpected messages field type: {}", messagesObj.getClass());
                            return java.util.Collections.emptyList();
                        }
                    } else {
                        // 可能是错误响应或空结果
                        log.debug("Response does not contain messages array: {}", responseMap);
                        return java.util.Collections.emptyList();
                    }
                } else {
                    log.warn("Unexpected response type: {}", responseBody.getClass());
                    return java.util.Collections.emptyList();
                }

                // 解析每条消息的 payload（Base64 解码）
                for (Map<String, Object> msg : messages) {
                    if (msg.containsKey("payload")) {
                        try {
                            String payloadBase64 = (String) msg.get("payload");
                            String payloadJson = new String(java.util.Base64.getDecoder().decode(payloadBase64));
                            // 解析 JSON 到 Map
                            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                            Map<String, Object> payload = mapper.readValue(payloadJson, Map.class);
                            msg.put("payload", payload);
                        } catch (Exception e) {
                            log.warn("Failed to decode message payload: {}", e.getMessage());
                        }
                    }
                }

                log.info("Synced {} messages for channel {}", messages.size(), channelId);
                return messages;
            } else {
                log.error("Failed to sync messages: {}", response.getStatusCode());
                return java.util.Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("Error syncing messages for channel: " + channelId, e);
            return java.util.Collections.emptyList();
        }
    }

    @Data
    public static class ImTokenResponse {
        private String token;
    }

    public record ImTokenResult(boolean success, String token, String error) {
    }
}
