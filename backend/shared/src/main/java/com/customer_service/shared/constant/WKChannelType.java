package com.customer_service.shared.constant;

/**
 * WuKongIM 频道类型常量
 * 用于 IM 通信时区分不同的频道类型
 * 前后端必须保持一致
 */
public final class WKChannelType {

    private WKChannelType() {
        // 防止实例化
    }

    /**
     * 个人频道 - 用于一对一私聊
     */
    public static final int PERSONAL = 1;

    /**
     * 群组频道 - 用于群聊
     */
    public static final int GROUP = 2;

    /**
     * 客服频道 - 已废弃，使用 VISITOR 替代
     * 
     * @deprecated 使用 VISITOR 访客频道替代
     */
    @Deprecated
    public static final int CUSTOMER_SERVICE = 3;

    /**
     * 社区话题频道
     */
    public static final int COMMUNITY_TOPIC = 4;

    /**
     * 社区频道
     */
    public static final int COMMUNITY = 5;

    /**
     * 数据频道 - 用于系统数据推送
     */
    public static final int DATA = 6;

    /**
     * 访客频道 - 用于客服场景
     * 支持一个访客订阅者 + 多个客服订阅者
     * 频道ID = 访客UID (格式: {projectId}_{guestUid})
     */
    public static final int VISITOR = 10;

    /**
     * 验证频道类型是否有效
     */
    public static boolean isValid(int channelType) {
        return channelType == PERSONAL || channelType == GROUP ||
                channelType == CUSTOMER_SERVICE || channelType == COMMUNITY_TOPIC ||
                channelType == COMMUNITY || channelType == DATA || channelType == VISITOR;
    }

    /**
     * 获取频道类型名称
     */
    public static String getName(int channelType) {
        return switch (channelType) {
            case PERSONAL -> "个人频道";
            case GROUP -> "群组频道";
            case CUSTOMER_SERVICE -> "客服频道(已废弃)";
            case COMMUNITY_TOPIC -> "社区话题频道";
            case COMMUNITY -> "社区频道";
            case DATA -> "数据频道";
            case VISITOR -> "访客频道";
            default -> "未知频道";
        };
    }
}
