package com.customer_service.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * WuKongIM Webhook 消息通知 DTO
 * 对应 msg.notify 事件的消息格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WKMessageNotify {

    /**
     * 消息头信息
     */
    private Map<String, Object> header;

    /**
     * 消息设置标识
     */
    private Integer setting;

    /**
     * 服务端消息ID（全局唯一）
     */
    @JsonProperty("message_id")
    private Long messageId;

    /**
     * 字符串类型的服务端消息ID
     */
    @JsonProperty("message_idstr")
    private String messageIdStr;

    /**
     * 客户端消息唯一编号
     */
    @JsonProperty("client_msg_no")
    private String clientMsgNo;

    /**
     * 消息序列号（频道内唯一，递增）
     */
    @JsonProperty("message_seq")
    private Long messageSeq;

    /**
     * 发送者 UID
     */
    @JsonProperty("from_uid")
    private String fromUid;

    /**
     * 频道 ID
     */
    @JsonProperty("channel_id")
    private String channelId;

    /**
     * 频道类型 (1=个人, 2=群组, 10=访客)
     */
    @JsonProperty("channel_type")
    private Integer channelType;

    /**
     * 服务端消息时间戳（秒）
     */
    private Long timestamp;

    /**
     * Base64 编码的消息内容
     */
    private String payload;
}
