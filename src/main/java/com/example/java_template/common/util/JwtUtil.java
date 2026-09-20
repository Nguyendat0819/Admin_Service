package com.example.java_template.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.java_template.common.exception.BusinessException;
import com.example.java_template.common.response.DomainCode;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Lớp tiện ích hỗ trợ các thao tác liên quan đến JSON Web Token (JWT).
 *
 * <p>Hỗ trợ 2 loại token tách biệt (ký bằng 2 secret khác nhau):</p>
 * <ul>
 *   <li><b>Access Token</b>: ngắn hạn (mặc định 15 phút), dùng để gọi API,
 *       gửi qua header {@code Authorization: Bearer ...}</li>
 *   <li><b>Refresh Token</b>: dài hạn (mặc định 7 ngày), dùng để lấy access token mới
 *       khi access hết hạn, gửi qua HttpOnly cookie</li>
 * </ul>
 *
 * <p>Dùng 2 secret khác nhau → kẻ tấn công lộ được access token không thể
 * tự tạo refresh token và ngược lại.</p>
 */
@Component
public class JwtUtil {
    // ============================================================
    //  ACCESS TOKEN (gọi API)
    // ============================================================
    @Value("${jwt.secret:myAccessSecretKey1234567890123456789012345678901234567890}")
    private String accessSecret;

    @Value("${jwt.expiration:900}")
    private Long accessExpiration; // giây

    // ============================================================
    //  REFRESH TOKEN (chỉ dùng để refresh)
    // ============================================================
    @Value("${jwt.refresh.secret:myRefreshSecretKey12345678901234567890123456789012}")
    private String refreshSecret;

    @Value("${jwt.refresh.expiration:604800}")
    private Long refreshExpiration; // giây

    /**
     * Claim {@code type} phân biệt access vs refresh token, tránh dùng nhầm.
     */
    public static final String CLAIM_TYPE = "type";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_FAMILY = "family";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    // ============================================================
    //  KEY DERIVATION
    // ============================================================
    private SecretKey getAccessSigningKey() {
        return Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
    }

    private SecretKey getRefreshSigningKey() {
        return Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
    }

    // ============================================================
    //  ACCESS TOKEN
    // ============================================================
    public String generateAccessToken(String username, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessExpiration * 1000);
        return Jwts.builder()
                .subject(username)
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .claim(CLAIM_ROLE, role)
                .issuedAt(now)
                .expiration(exp)
                .signWith(getAccessSigningKey())
                .compact();
    }

    /**
     * Parse access token — verify chữ ký + expiration.
     * @throws BusinessException nếu sai chữ ký, hết hạn, hoặc type != access.
     */
    public Claims parseAccessToken(String token) {
        Claims claims = parseClaims(token, getAccessSigningKey());
        validateType(claims, TYPE_ACCESS);
        return claims;
    }

    public long getAccessExpirationSeconds() {
        return accessExpiration;
    }

    // ============================================================
    //  REFRESH TOKEN
    // ============================================================
    /**
     * Tạo refresh token có gắn family ID (để tracking rotation chain).
     */
    public String generateRefreshToken(String username, String role, String familyId) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshExpiration * 1000);
        return Jwts.builder()
                .subject(username)
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .claim(CLAIM_ROLE, role)
                .claim(CLAIM_FAMILY, familyId)
                .issuedAt(now)
                .expiration(exp)
                .signWith(getRefreshSigningKey())
                .compact();
    }

    /**
     * Parse refresh token — verify chữ ký + expiration + type.
     * @throws BusinessException nếu sai chữ ký, hết hạn, hoặc type != refresh.
     */
    public Claims parseRefreshToken(String token) {
        Claims claims = parseClaims(token, getRefreshSigningKey());
        validateType(claims, TYPE_REFRESH);
        return claims;
    }

    public long getRefreshExpirationSeconds() {
        return refreshExpiration;
    }

    // ============================================================
    //  LOW-LEVEL: Parse + verify với key bất kỳ (private)
    // ============================================================
    private Claims parseClaims(String token, SecretKey key) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            throw new BusinessException(DomainCode.TOKEN_EXPIRED);
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            throw new BusinessException(DomainCode.UNAUTHORIZED);
        }
    }

    private void validateType(Claims claims, String expectedType) {
        Object type = claims.get(CLAIM_TYPE);
        if (type == null || !expectedType.equals(type.toString())) {
            throw new BusinessException(DomainCode.UNAUTHORIZED);
        }
    }

    // ============================================================
    //  BACKWARD-COMPATIBLE API (cho code cũ)
    // ============================================================
    /** @deprecated dùng {@link #generateAccessToken(String, String)} thay thế. */
    @Deprecated
    public String generateToken(String username, String role) {
        return generateAccessToken(username, role);
    }

    /**
     * Parse token và trả về Claims. Đã có sẵn để thay thế 4 lần parse lặp lại.
     * Tự detect loại token dựa vào claim {@code type}.
     */
    public Claims getClaimsFromToken(String token) {
        // Peek claim type trước để chọn key parse đúng
        // (jjwt sẽ throw SignatureException nếu parse sai key — bắt để thử key khác)
        try {
            return parseRefreshToken(token);
        } catch (BusinessException e) {
            return parseAccessToken(token);
        }
    }

    // ============================================================
    //  COOKIE / REQUEST HELPERS
    // ============================================================
    /**
     * Trích xuất bearer token từ HttpServletRequest header.
     * Hỗ trợ cả 2 dạng: "Bearer xxx" hoặc chỉ "xxx".
     */
    public String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return bearerToken == null ? null : bearerToken.trim();
    }

    // ============================================================
    //  DEPRECATED — giữ để code cũ không lỗi compile
    // ============================================================
    /** @deprecated dùng {@link #parseAccessToken(String)} thay thế. */
    @Deprecated
    public String getUsernameFromToken(String token) {
        return parseAccessToken(token).getSubject();
    }

    /** @deprecated dùng {@link #parseAccessToken(String)} thay thế. */
    @Deprecated
    public String getUserRoleFromToken(String token) {
        return parseAccessToken(token).get(CLAIM_ROLE).toString();
    }

    /** @deprecated dùng claim exp trong parseAccessToken thay thế. */
    @Deprecated
    public long getExpirationEpochMillisFromToken(String token) {
        return parseAccessToken(token).getExpiration().getTime();
    }

    /** @deprecated dùng {@link #parseAccessToken(String)} thay thế. */
    @Deprecated
    public boolean validateToken(String token) {
        try {
            parseAccessToken(token);
            return true;
        } catch (BusinessException e) {
            throw e; // ném lại để GlobalExceptionHandler xử lý
        }
    }

    /** @deprecated dùng {@link #getAccessExpirationSeconds()} thay thế. */
    @Deprecated
    public long getExpiration() {
        return accessExpiration;
    }
}
