package com.example.java_template.feature.controller;

import com.example.java_template.common.response.ApiResponse;
import com.example.java_template.common.response.ApiResponseFactory;
import com.example.java_template.common.security.CookieUtil;
import com.example.java_template.common.util.JwtUtil;
import com.example.java_template.feature.controller.api.AuthApi;
import com.example.java_template.feature.model.request.LoginRequest;
import com.example.java_template.feature.model.response.LoginResponse;
import com.example.java_template.feature.model.response.TokenPair;
import com.example.java_template.feature.service.AuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * AuthController xử lý cookie chứa refresh token.
 *
 * <h3>Quy ước cookie:</h3>
 * <ul>
 *   <li>Refresh token KHÔNG nằm trong response body</li>
 *   <li>Được set vào HttpOnly cookie (JS không đọc được)</li>
 *   <li>Cookie tự động gửi kèm mỗi request tới backend</li>
 *   <li>Browser tự xóa khi Max-Age = 0 (logout)</li>
 * </ul>
 *
 * <h3>Client chỉ nhận access token trong body.</h3>
 */
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController implements AuthApi {

    AuthService authService;
    ApiResponseFactory apiResponseFactory;
    JwtUtil jwtUtil;
    CookieUtil cookieUtil;

    // ============================================================
    //  LOGIN — cấp access + set refresh cookie
    // ============================================================
    @Override
    public ApiResponse<LoginResponse> login(LoginRequest request, HttpServletResponse response) {
        TokenPair pair = authService.login(request);

        // Set refresh token vào HttpOnly cookie
        cookieUtil.setRefreshTokenCookie(response, pair.getRefreshToken(), jwtUtil.getRefreshExpirationSeconds());

        // Body chỉ chứa access token
        return apiResponseFactory.success(pair.toLoginResponse());
    }

    // ============================================================
    //  REFRESH — đọc refresh từ cookie, rotate, set cookie mới
    // ============================================================
    @Override
    public ApiResponse<LoginResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtil.getRefreshTokenFromCookie(request);
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RuntimeException("Refresh token không tồn tại (chưa login hoặc cookie bị xóa)");
        }

        TokenPair newPair = authService.refresh(refreshToken);

        // Set refresh token MỚI vào cookie (token cũ đã bị rotate)
        cookieUtil.setRefreshTokenCookie(response, newPair.getRefreshToken(), jwtUtil.getRefreshExpirationSeconds());

        return apiResponseFactory.success(newPair.toLoginResponse());
    }

    // ============================================================
    //  LOGOUT — revoke refresh + clear cookie
    // ============================================================
    @Override
    public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtil.getRefreshTokenFromCookie(request);

        // Revoke refresh token trong Redis
        authService.logout(refreshToken);

        // Clear cookie (Max-Age=0)
        cookieUtil.clearRefreshTokenCookie(response);

        return apiResponseFactory.success(null);
    }
}
