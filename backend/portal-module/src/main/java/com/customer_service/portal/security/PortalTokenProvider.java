package com.customer_service.portal.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Portal 用户 Token 提供者
 * 用于生成和验证 Portal 端用户的访问令牌
 */
@Slf4j
@Component
public class PortalTokenProvider {

    private final SecretKey secretKey;
    private final long expiration;

    public PortalTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration:86400000}") long expiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    /**
     * 生成 Portal 用户 Token
     *
     * @param userId    用户 ID
     * @param projectId 项目 ID
     * @param uid       用户 UID
     * @return JWT Token
     */
    public String generateToken(Long userId, Long projectId, String uid) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("projectId", projectId)
                .claim("uid", uid)
                .claim("type", "portal") // 标识为 portal token
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 从 Token 中获取用户 ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 从 Token 中获取项目 ID
     */
    public Long getProjectIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("projectId", Long.class);
    }

    /**
     * 从 Token 中获取用户 UID
     */
    public String getUidFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("uid", String.class);
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            // 验证是否为 portal token
            String type = claims.get("type", String.class);
            if (!"portal".equals(type)) {
                log.warn("Invalid token type: {}", type);
                return false;
            }
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("Token expired: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.warn("Unsupported token: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("Malformed token: {}", ex.getMessage());
        } catch (SecurityException ex) {
            log.warn("Invalid token signature: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("Token is empty or null: {}", ex.getMessage());
        }
        return false;
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
