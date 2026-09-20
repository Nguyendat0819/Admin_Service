package com.example.java_template.feature.service.impl;

import com.example.java_template.common.exception.ResourceNotFoundException;
import com.example.java_template.common.response.DomainCode;
import com.example.java_template.common.util.JwtUtil;
import com.example.java_template.feature.model.dto.RefreshTokenData;
import com.example.java_template.feature.model.request.LoginRequest;
import com.example.java_template.feature.model.response.TokenPair;
import com.example.java_template.feature.service.AuthService;
import com.example.java_template.feature.service.RefreshTokenService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * AuthServiceImpl với cơ chế 2-token (Access + Refresh) chuẩn OAuth 2.0.
 *
 * <h3>Flow tổng quan:</h3>
 * <pre>
 *   login()    → cấp access + refresh (refresh set vào HttpOnly cookie ở controller)
 *   refresh()  → rotate refresh + cấp access mới (gọi khi access hết hạn)
 *   logout()   → revoke refresh + clear cookie
 * </pre>
 *
 * <h3>Bảo mật:</h3>
 * <ul>
 *   <li>Access token: 15 phút, lưu memory (JS), gửi qua Authorization header</li>
 *   <li>Refresh token: 7 ngày, HttpOnly cookie, server-side tracking family</li>
 *   <li>Rotation + theft detection: nếu refresh token cũ bị reuse → vô hiệu hóa family</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {

    JwtUtil jwtUtil;
    RefreshTokenService refreshTokenService;
    PasswordEncoder passwordEncoder;

    // ============================================================
    //  LOGIN — tạo cả access + refresh
    // ============================================================
    @Override
    public TokenPair login(LoginRequest request) {
        if (!request.getUsername().equals("Dat") || !request.getPassword().equals("12")) {
            throw new ResourceNotFoundException(DomainCode.TOKEN_EXPIRED);
        }

        // TODO: Lấy role từ database thay vì hardcode
        String role = "ROLE_USER";

        // Sinh refresh token (lưu Redis) + access token (stateless)
        RefreshTokenService.IssuedToken issued = refreshTokenService.create(request.getUsername(), role);
        String accessToken = jwtUtil.generateAccessToken(request.getUsername(), role);

        return TokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(issued.token())
                .type("Bearer")
                .expiresIn(jwtUtil.getAccessExpirationSeconds())
                .build();
    }

    // ============================================================
    //  REFRESH — rotate refresh token, cấp access token mới
    // ============================================================
    @Override
    public TokenPair refresh(String refreshToken) {
        // refreshToken từ cookie (controller đọc và truyền vào).
        // - Nếu token không có trong Redis (TTL expired hoặc bị revoke) → TOKEN_EXPIRED
        // - Nếu token đã used=true (reuse) → vô hiệu hóa family → UNAUTHORIZED
        RefreshTokenService.IssuedToken newIssued = refreshTokenService.rotate(refreshToken);

        RefreshTokenData data = newIssued.data();
        String newAccessToken = jwtUtil.generateAccessToken(data.getUsername(), data.getRole());

        return TokenPair.builder()
                .accessToken(newAccessToken)
                .refreshToken(newIssued.token())
                .type("Bearer")
                .expiresIn(jwtUtil.getAccessExpirationSeconds())
                .build();
    }

    // ============================================================
    //  LOGOUT — vô hiệu hóa refresh
    // ============================================================
    @Override
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.revoke(refreshToken);
        }
        // Cookie clear sẽ do controller xử lý
    }
}
