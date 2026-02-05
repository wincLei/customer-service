package com.customer_service.shared.service;

import com.aliyun.oss.common.utils.BinaryUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.utils.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 阿里云 OSS 服务
 * 使用 V4 签名方式提供客户端直传签名
 * 参考文档:
 * https://help.aliyun.com/zh/oss/user-guide/obtain-signature-information-from-the-server-and-upload-data-to-oss
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

    @Value("${aliyun.oss.region:cn-hangzhou}")
    private String region;

    @Value("${aliyun.oss.domain:}")
    private String domain;

    @Value("${aliyun.oss.dir:uploads/}")
    private String dir;

    // 过期时间（秒）
    private static final long EXPIRE_TIME = 3600L;

    // 最大文件大小（30MB）
    private static final long MAX_FILE_SIZE = 34952533L;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 生成过期时间 ISO8601 格式字符串
     */
    private String generateExpiration(long seconds) {
        Instant instant = Instant.now().plusSeconds(seconds);
        ZonedDateTime zonedDateTime = instant.atZone(ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return zonedDateTime.format(formatter);
    }

    /**
     * 获取客户端直传签名（V4 签名版本）
     * 
     * @return 包含签名信息的 Map
     */
    public Map<String, String> getUploadToken() {
        Map<String, String> result = new HashMap<>();

        if (accessKeyId.isBlank() || accessKeySecret.isBlank() || bucket.isBlank()) {
            log.warn("OSS configuration is incomplete: accessKeyId={}, bucket={}",
                    accessKeyId.isBlank() ? "missing" : "ok",
                    bucket.isBlank() ? "missing" : "ok");
            result.put("error", "OSS 配置不完整");
            return result;
        }

        try {
            // 获取当前日期（UTC）
            ZonedDateTime now = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
            String date = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String xOssDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));

            // 构建 x-oss-credential
            String xOssCredential = String.format("%s/%s/%s/oss/aliyun_v4_request",
                    accessKeyId, date, region);

            // 步骤1: 创建 policy
            Map<String, Object> policy = new HashMap<>();
            policy.put("expiration", generateExpiration(EXPIRE_TIME));

            List<Object> conditions = new ArrayList<>();

            // bucket 条件
            Map<String, String> bucketCondition = new HashMap<>();
            bucketCondition.put("bucket", bucket);
            conditions.add(bucketCondition);

            // 签名版本条件
            Map<String, String> signatureVersionCondition = new HashMap<>();
            signatureVersionCondition.put("x-oss-signature-version", "OSS4-HMAC-SHA256");
            conditions.add(signatureVersionCondition);

            // credential 条件
            Map<String, String> credentialCondition = new HashMap<>();
            credentialCondition.put("x-oss-credential", xOssCredential);
            conditions.add(credentialCondition);

            // date 条件
            Map<String, String> dateCondition = new HashMap<>();
            dateCondition.put("x-oss-date", xOssDate);
            conditions.add(dateCondition);

            // 文件大小限制
            conditions.add(Arrays.asList("content-length-range", 1, MAX_FILE_SIZE));

            // 成功状态码
            conditions.add(Arrays.asList("eq", "$success_action_status", "200"));

            String formateDir = dir + DateUtils.formatDate(new Date(), "yyyyMMdd") + "/";
            // 文件前缀限制
            conditions.add(Arrays.asList("starts-with", "$key", formateDir));

            policy.put("conditions", conditions);

            String jsonPolicy = objectMapper.writeValueAsString(policy);
            log.debug("Policy JSON: {}", jsonPolicy);

            // 步骤2: 构造待签名字符串（StringToSign）
            String stringToSign = new String(Base64.encodeBase64(jsonPolicy.getBytes()));

            // 步骤3: 计算 SigningKey
            byte[] dateKey = hmacSha256(("aliyun_v4" + accessKeySecret).getBytes(), date);
            byte[] dateRegionKey = hmacSha256(dateKey, region);
            byte[] dateRegionServiceKey = hmacSha256(dateRegionKey, "oss");
            byte[] signingKey = hmacSha256(dateRegionServiceKey, "aliyun_v4_request");

            // 步骤4: 计算 Signature
            byte[] signatureBytes = hmacSha256(signingKey, stringToSign);
            String signature = BinaryUtil.toHex(signatureBytes);

            // 构建 Host
            String host = String.format("https://%s.%s", bucket, endpoint);

            // 返回签名信息
            result.put("policy", stringToSign);
            result.put("signature", signature);
            result.put("x_oss_credential", xOssCredential);
            result.put("x_oss_date", xOssDate);
            result.put("host", host);
            result.put("domain", domain);
            result.put("dir", formateDir);

            log.debug("Generated OSS upload token: host={}, dir={}", host, formateDir);

            return result;
        } catch (Exception e) {
            log.error("Failed to generate OSS upload token", e);
            result.put("error", "生成上传凭证失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * HMAC-SHA256 计算
     */
    private byte[] hmacSha256(byte[] key, String data) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
            return mac.doFinal(data.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC-SHA256", e);
        }
    }

    /**
     * 生成文件访问 URL
     */
    public String getFileUrl(String key) {
        return String.format("https://%s.%s/%s", bucket, endpoint, key);
    }
}
