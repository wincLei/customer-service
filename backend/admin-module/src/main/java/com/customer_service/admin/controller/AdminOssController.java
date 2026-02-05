package com.customer_service.admin.controller;

import com.customer_service.shared.dto.ApiResponse;
import com.customer_service.shared.service.AliyunOssService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理端 OSS 上传凭证接口
 */
@RestController
@RequestMapping("/api/admin/oss")
@RequiredArgsConstructor
public class AdminOssController {

    private final AliyunOssService ossService;

    /**
     * 获取 OSS 直传凭证（V4 签名）
     */
    @GetMapping("/token")
    public ApiResponse<Map<String, String>> getUploadToken() {
        Map<String, String> token = ossService.getUploadToken();

        if (token.containsKey("error")) {
            return ApiResponse.error(400, token.get("error"));
        }

        return ApiResponse.success(token);
    }
}
