package com.customer_service.portal.controller;

import com.customer_service.portal.service.PortalUserService;
import com.customer_service.shared.constant.DeviceType;
import com.customer_service.shared.dto.ApiResponse;
import com.customer_service.shared.entity.User;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Portal 用户控制器
 * 处理客户端用户的初始化和信息获取
 */
@RestController
@RequestMapping("/api/portal/user")
@RequiredArgsConstructor
public class PortalUserController {

    private final PortalUserService portalUserService;

    /**
     * 用户初始化请求
     */
    @Data
    public static class UserInitRequest {
        private Long projectId;
        private String guestUid;
        private String externalUid;
        private String nickname;
        private String avatar;
        private String phone;
        /** 设备类型: 1=WEB, 2=H5 (默认为 H5) */
        private Integer deviceFlag;
    }

    /**
     * 用户初始化响应（包含 IM Token）
     */
    @Data
    public static class UserInitResponse {
        private Long id;
        private String uid;
        private String externalUid;
        private String nickname;
        private String avatar;
        private String phone;
        private Boolean isGuest;
        private Boolean merged;
        private Long mergedFromUserId;
        /** WuKongIM 连接 Token */
        private String imToken;

        public static UserInitResponse from(PortalUserService.UserInitResult result) {
            UserInitResponse response = new UserInitResponse();
            User user = result.user();
            response.setId(user.getId());
            response.setUid(user.getUid());
            response.setExternalUid(user.getExternalUid());
            response.setNickname(user.getNickname());
            response.setAvatar(user.getAvatar());
            response.setPhone(user.getPhone());
            response.setIsGuest(user.getIsGuest());
            response.setMerged(result.merged());
            response.setMergedFromUserId(result.mergedFromUserId());
            response.setImToken(result.imToken());
            return response;
        }
    }

    /**
     * 初始化用户
     * 支持游客创建、认证用户创建、以及游客到认证用户的自动合并
     * 同时返回 WuKongIM 连接 Token，前端可直接使用该 Token 连接 IM
     *
     * 场景说明：
     * 1. 仅传入 guestUid: 创建或返回游客用户
     * 2. 传入 guestUid 和 externalUid:
     * - 如果 externalUid 对应的用户已存在，合并游客数据到该用户
     * - 如果不存在，将游客升级为认证用户
     * 3. 仅传入 externalUid: 创建或返回认证用户
     */
    @PostMapping("/init")
    public ApiResponse<UserInitResponse> initUser(@RequestBody UserInitRequest request) {
        if (request.getProjectId() == null) {
            return ApiResponse.fail(400, "项目ID不能为空");
        }

        // 默认设备类型为 H5
        int deviceFlag = request.getDeviceFlag() != null ? request.getDeviceFlag() : DeviceType.H5;

        PortalUserService.UserInitResult result = portalUserService.initOrMergeUser(
                request.getProjectId(),
                request.getGuestUid(),
                request.getExternalUid(),
                request.getNickname(),
                request.getAvatar(),
                request.getPhone(),
                deviceFlag);

        return ApiResponse.success(UserInitResponse.from(result));
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/{projectId}/{uid}")
    public ApiResponse<UserInitResponse> getUser(
            @PathVariable Long projectId,
            @PathVariable String uid) {
        return portalUserService.getUserByUid(projectId, uid)
                .map(user -> {
                    UserInitResponse response = new UserInitResponse();
                    response.setId(user.getId());
                    response.setUid(user.getUid());
                    response.setExternalUid(user.getExternalUid());
                    response.setNickname(user.getNickname());
                    response.setAvatar(user.getAvatar());
                    response.setPhone(user.getPhone());
                    response.setIsGuest(user.getIsGuest());
                    response.setMerged(false);
                    return ApiResponse.success(response);
                })
                .orElse(ApiResponse.fail(404, "用户不存在"));
    }
}
