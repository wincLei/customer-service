package com.customer_service.shared.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 阿里云 OSS 服务
 * 提供客户端直传签名
 */
@Slf4j
@Service
public class AliyunOssService {

    @Value("${aliyun.oss.access-key-id:}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret:}")
    private String accessKeySecret;

    @Value("${aliyun.oss.endpoint:oss-cn-hangzhou.aliyuncs.com}")
    private String endpoint;

    @Value("${aliyun.oss.bucket:}")
    private String bucket;

    @Value("${aliyun.oss.dir:uploads/}")
    private String dir;

    /**
     * 获取客户端直传签名
     * 
     * @return 包含签名信息的 Map
     */
    public Map<String, String> getUploadToken() {
        Map<String, String> result = new HashMap<>();

        if (accessKeyId.isBlank() || accessKeySecret.isBlank() || bucket.isBlank()) {
            log.warn("OSS configuration is incomplete");
            result.put("error", "OSS 配置不完整");
            return result;
        }

        try {
            // 设置过期时间（5分钟后）
            Instant expireTime = Instant.now().plus(5, ChronoUnit.MINUTES);
            String expiration = expireTime.toString();

            // 构建 Policy
            String policyJson = String.format("""
                    {
                        "expiration": "%s",
                        "conditions": [
                            {"bucket": "%s"},
                            ["starts-with", "$key", "%s"],
                            ["content-length-range", 0, 104857600]
                        ]
                    }
                    """, expiration, bucket, dir);

            // Base64 编码 Policy
            String policy = Base64.getEncoder().encodeToString(
                    policyJson.getBytes(StandardCharsets.UTF_8));

            // 计算签名
            String signature = calculateSignature(policy, accessKeySecret);

            // 构建 Host
            String host = String.format("https://%s.%s", bucket, endpoint);

            result.put("accessKeyId", accessKeyId);
            result.put("policy", policy);
            result.put("signature", signature);
            result.put("host", host);
            result.put("dir", dir);
            result.put("expire", String.valueOf(expireTime.getEpochSecond()));

            return result;
        } catch (Exception e) {
            log.error("Failed to generate OSS upload token", e);
            result.put("error", "生成上传凭证失败");
            return result;
        }
    }

    /**
     * 计算签名
     */
    private String calculateSignature(String policy, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        byte[] signData = mac.doFinal(policy.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signData);
    }

    /**
     * 生成文件访问 URL
     */
    public String getFileUrl(String key) {
        return String.format("https://%s.%s/%s", bucket, endpoint, key);
    }
}
