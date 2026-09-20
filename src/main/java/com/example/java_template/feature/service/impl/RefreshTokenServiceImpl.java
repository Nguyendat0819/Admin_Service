package com.example.java_template.feature.service.impl;

import com.example.java_template.common.exception.BusinessException;
import com.example.java_template.common.response.DomainCode;
import com.example.java_template.common.util.JwtUtil;
import com.example.java_template.feature.model.dto.RefreshTokenData;
import com.example.java_template.feature.service.RefreshTokenService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Triển khai {@link RefreshTokenService} dùng Redis.
 *
 * <h3>Schema trong Redis:</h3>
 * <ul>
 *   <li>{@code refresh_token:<jwt>} → JSON {@link RefreshTokenData} (TTL = refresh expiration)</li>
 *   <li>{@code refresh_family:<familyId>} → SET các jwt thuộc family (TTL dài hơn 1 chút)</li>
 * </ul>
 *
 * <h3>Theft detection:</h3>
 * Token đã bị rotate ({@code used=true}) mà bị tái sử dụng → revoke toàn bộ family.
 */
@Service
@Primary   // Mặc định khi cả 2 bean cùng active (vd: lỗi config)
@ConditionalOnProperty(
        name = "app.refresh-token.storage",
        havingValue = "redis",
        matchIfMissing = true   // ← Mặc định là Redis nếu không cấu hình
)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

    StringRedisTemplate redis;
    JwtUtil jwtUtil;
    ObjectMapper objectMapper;

    private static final String KEY_PREFIX = "refresh_token:";
    private static final String FAMILY_PREFIX = "refresh_family:";

    // ============================================================
    //  CREATE — gọi khi login
    // ============================================================
    @Override
    public IssuedToken create(String username, String role) {
        String familyId = UUID.randomUUID().toString();
        String tokenRefresh = jwtUtil.generateRefreshToken(username, role, familyId);

        RefreshTokenData data = RefreshTokenData.builder()
                .username(username)
                .role(role)
                .family(familyId)
                .previous(null)
                .used(false)
                .createdAt(System.currentTimeMillis())
                .build();

        save(tokenRefresh, data);
        addTokenToFamily(familyId, tokenRefresh);

        log.info("[RefreshToken] Created new family={} for user={}", familyId, username);
        return new IssuedToken(tokenRefresh, data);
    }

    // ============================================================
    //  ROTATE — gọi khi /api/auth/refresh
    // ============================================================
    @Override
    public IssuedToken rotate(String oldToken) {
        RefreshTokenData old = get(oldToken);

        // Case 1: Token không có trong Redis
        // → có thể đã expire (Redis TTL = 0) hoặc bị revoke → user login lại
        if (old == null) {
            log.warn("[RefreshToken] Rotate failed: token not found in Redis (expired or revoked)");
            throw new BusinessException(DomainCode.TOKEN_EXPIRED);
        }

        // Case 2: Token đã bị rotate trước đó (used=true)
        // → nghi ngờ bị đánh cắp! Vô hiệu hóa toàn bộ family
        if (old.isUsed()) {
            log.error("[RefreshToken] SECURITY: reused token detected in family={}, user={}. "
                    + "Revoking entire family.", old.getFamily(), old.getUsername());
            revokeFamily(old.getFamily());
            throw new BusinessException(DomainCode.UNAUTHORIZED);
        }

        // Case 3: Rotation bình thường
        // 1. Đánh dấu token cũ là used=true (giữ trong Redis để phát hiện reuse)
        old.setUsed(true);
        save(oldToken, old);

        // 2. Tạo token mới cùng family
        String newToken = jwtUtil.generateRefreshToken(old.getUsername(), old.getRole(), old.getFamily());
        RefreshTokenData newData = RefreshTokenData.builder()
                .username(old.getUsername())
                .role(old.getRole())
                .family(old.getFamily())
                .previous(oldToken)
                .used(false)
                .createdAt(System.currentTimeMillis())
                .build();
        save(newToken, newData);

        // 3. Cập nhật family tracking
        addTokenToFamily(old.getFamily(), newToken);

        log.info("[RefreshToken] Rotated family={} user={}", old.getFamily(), old.getUsername());
        return new IssuedToken(newToken, newData);
    }

    // ============================================================
    //  REVOKE
    // ============================================================
    @Override
    public void revoke(String token) {
        RefreshTokenData data = get(token);
        if (data != null) {
            redis.opsForSet().remove(FAMILY_PREFIX + data.getFamily(), token);
            redis.delete(KEY_PREFIX + token);
            log.info("[RefreshToken] Revoked single token (family={})", data.getFamily());
        }
    }

    @Override
    public void revokeFamily(String familyId) {
        Set<String> tokens = redis.opsForSet().members(FAMILY_PREFIX + familyId);
        int count = 0;
        if (tokens != null && !tokens.isEmpty()) {
            for (String t : tokens) {
                redis.delete(KEY_PREFIX + t);
                count++;
            }
        }
        redis.delete(FAMILY_PREFIX + familyId);
        log.warn("[RefreshToken] Revoked ENTIRE family={} ({} tokens)", familyId, count);
    }

    // ============================================================
    //  LOW-LEVEL: Redis operations
    // ============================================================
    private void save(String token, RefreshTokenData data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            redis.opsForValue().set(
                    KEY_PREFIX + token,
                    json,
                    jwtUtil.getRefreshExpirationSeconds(),
                    TimeUnit.SECONDS
            );
        } catch (JsonProcessingException e) {
            throw new BusinessException(DomainCode.INTERNAL_ERROR);
        }
    }

    private RefreshTokenData get(String token) {
        String json = redis.opsForValue().get(KEY_PREFIX + token);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, RefreshTokenData.class);
        } catch (JsonProcessingException e) {
            log.error("[RefreshToken] Corrupt JSON in Redis for token (first 20 chars): {}",
                    token.length() > 20 ? token.substring(0, 20) : token);
            return null;
        }
    }

    private void addTokenToFamily(String familyId, String token) {
        redis.opsForSet().add(FAMILY_PREFIX + familyId, token);
        // Family key TTL hơi dài hơn 1 chút để chắc chắn có thể revoke khi cần
        redis.expire(FAMILY_PREFIX + familyId,
                Duration.ofSeconds(jwtUtil.getRefreshExpirationSeconds() + 3600));
    }
}
