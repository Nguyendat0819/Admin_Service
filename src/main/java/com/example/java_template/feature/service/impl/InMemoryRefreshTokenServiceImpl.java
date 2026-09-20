package com.example.java_template.feature.service.impl;

import com.example.java_template.common.exception.BusinessException;
import com.example.java_template.common.response.DomainCode;
import com.example.java_template.common.util.JwtUtil;
import com.example.java_template.feature.model.dto.RefreshTokenData;
import com.example.java_template.feature.service.RefreshTokenService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Triển khai {@link RefreshTokenService} dùng {@link ConcurrentHashMap} trong JVM heap.
 *
 * <p>Chỉ được kích hoạt khi property {@code app.refresh-token.storage=in-memory}
 * — thường dùng cho:</p>
 * <ul>
 *   <li>Dev/test khi chưa cài Redis</li>
 *   <li>Demo nhanh không cần infra</li>
 *   <li>Unit test tránh dependency ngoài</li>
 * </ul>
 *
 * <p><b>Hạn chế</b> (so với Redis):</p>
 * <ul>
 *   <li>Mất dữ liệu khi restart JVM</li>
 *   <li>Không scale được qua nhiều instance (mỗi node có map riêng)</li>
 *   <li>Không có cơ chế cleanup tự động — TTL phải check thủ công khi lookup</li>
 * </ul>
 */
@Service
@ConditionalOnProperty(
        name = "app.refresh-token.storage",
        havingValue = "in-memory"
)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class InMemoryRefreshTokenServiceImpl implements RefreshTokenService {

    // token -> metadata (TTL check bằng cách parse JWT exp)
    final Map<String, RefreshTokenData> store = new ConcurrentHashMap<>();

    // familyId -> set token strings (để revoke nhanh)
    final Map<String, Set<String>> families = new ConcurrentHashMap<>();

    final JwtUtil jwtUtil;

    public InMemoryRefreshTokenServiceImpl(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        log.info("[RefreshToken/InMemory] Initialized in-memory refresh token store");
    }

    // ============================================================
    //  CREATE
    // ============================================================
    @Override
    public IssuedToken create(String username, String role) {
        String familyId = UUID.randomUUID().toString();
        String token = jwtUtil.generateRefreshToken(username, role, familyId);

        RefreshTokenData data = RefreshTokenData.builder()
                .username(username)
                .role(role)
                .family(familyId)
                .previous(null)
                .used(false)
                .createdAt(System.currentTimeMillis())
                .build();

        store.put(token, data);
        families.computeIfAbsent(familyId, k -> ConcurrentHashMap.newKeySet()).add(token);

        log.info("[RefreshToken/InMemory] Created family={} user={}", familyId, username);
        return new IssuedToken(token, data);
    }

    // ============================================================
    //  ROTATE
    // ============================================================
    @Override
    public IssuedToken rotate(String oldToken) {
        // Validate JWT trước (signature + exp)
        try {
            jwtUtil.parseRefreshToken(oldToken);
        } catch (BusinessException e) {
            throw e; // hết hạn / sai chữ ký → TOKEN_EXPIRED hoặc UNAUTHORIZED
        }

        RefreshTokenData old = store.get(oldToken);
        if (old == null) {
            log.warn("[RefreshToken/InMemory] Rotate failed: token not in store");
            throw new BusinessException(DomainCode.TOKEN_EXPIRED);
        }

        // Theft detection
        if (old.isUsed()) {
            log.error("[RefreshToken/InMemory] SECURITY: reused token in family={}", old.getFamily());
            revokeFamily(old.getFamily());
            throw new BusinessException(DomainCode.UNAUTHORIZED);
        }

        // Rotation
        old.setUsed(true);
        store.put(oldToken, old);

        String newToken = jwtUtil.generateRefreshToken(old.getUsername(), old.getRole(), old.getFamily());
        RefreshTokenData newData = RefreshTokenData.builder()
                .username(old.getUsername())
                .role(old.getRole())
                .family(old.getFamily())
                .previous(oldToken)
                .used(false)
                .createdAt(System.currentTimeMillis())
                .build();
        store.put(newToken, newData);
        families.computeIfAbsent(old.getFamily(), k -> ConcurrentHashMap.newKeySet()).add(newToken);

        log.info("[RefreshToken/InMemory] Rotated family={}", old.getFamily());
        return new IssuedToken(newToken, newData);
    }

    // ============================================================
    //  REVOKE
    // ============================================================
    @Override
    public void revoke(String token) {
        RefreshTokenData data = store.remove(token);
        if (data != null) {
            Set<String> familyTokens = families.get(data.getFamily());
            if (familyTokens != null) familyTokens.remove(token);
        }
    }

    @Override
    public void revokeFamily(String familyId) {
        Set<String> tokens = families.remove(familyId);
        if (tokens != null) {
            for (String t : tokens) store.remove(t);
            log.warn("[RefreshToken/InMemory] Revoked entire family={} ({} tokens)", familyId, tokens.size());
        }
    }
}
