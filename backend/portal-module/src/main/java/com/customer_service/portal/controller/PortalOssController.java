package com.customer_service.portal.controller;

import com.customer_service.shared.service.AliyunOssService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * OSS 上传凭证接口
 */
@RestController
@RequestMapping("/api/portal/oss")
@RequiredArgsConstructor
public class PortalOssController {

    private final AliyunOssService ossService;

    /**
     * 获取 OSS 直传凭证
     */
    @GetMapping("/token")
    public ResponseEntity<Map<String, String>> getUploadToken() {
        Map<String, String> token = ossService.getUploadToken();

        if (token.containsKey("error")) {
            return ResponseEntity.badRequest().body(token);
        }

        return ResponseEntity.ok(token);
    }
}
