package com.customer_service.shared.constant;

/**
 * WuKongIM 设备类型常量
 * 用于 IM 连接时区分不同的客户端类型
 * 前端获取 token 和连接 SDK 时必须使用相同的 deviceFlag
 */
public final class DeviceType {

    private DeviceType() {
        // 防止实例化
    }

    /**
     * Web 端（客服工作台）
     */
    public static final int WEB = 1;

    /**
     * H5 移动端（用户聊天）
     */
    public static final int H5 = 2;

    /**
     * 验证设备类型是否有效
     */
    public static boolean isValid(int deviceType) {
        return deviceType == WEB || deviceType == H5;
    }

    /**
     * 获取设备类型名称
     */
    public static String getName(int deviceType) {
        return switch (deviceType) {
            case WEB -> "WEB";
            case H5 -> "H5";
            default -> "UNKNOWN";
        };
    }
}
