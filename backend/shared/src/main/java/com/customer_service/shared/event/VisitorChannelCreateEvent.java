package com.customer_service.shared.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 访客频道创建事件
 * 当用户初始化完成后发布此事件，异步创建 WuKongIM 访客频道
 */
@Getter
public class VisitorChannelCreateEvent extends ApplicationEvent {

    /**
     * 项目ID
     */
    private final Long projectId;

    /**
     * 用户ID（数据库自增ID）
     */
    private final Long userId;

    public VisitorChannelCreateEvent(Object source, Long projectId, Long userId) {
        super(source);
        this.projectId = projectId;
        this.userId = userId;
    }
}
