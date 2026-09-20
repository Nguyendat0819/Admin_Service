package com.example.java_template.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Helper thao tác với HttpOnly cookie chứa refresh token.
 *
 * <p>Thuộc tầng security vì cookie chứa credential dài hạn cần cấu hình bảo mật chặt.</p>
 *
 * <p><b>Cấu hình an toàn mặc định:</b></p>
 * <ul>
 *   <li><b>HttpOnly</b>: JS không đọc được → chống XSS đánh cắp refresh token</li>
 *   <li><b>Secure</b>: chỉ gửi qua HTTPS (bật khi deploy production)</li>
 *   <li><b>SameSite=Lax</b>: chống CSRF cơ bản (Strict nếu không cần cross-site redirect)</li>
 *   <li><b>Path=/</b>: áp dụng cho mọi route</li>
 * </ul>
 */
@Component
public class CookieUtil {

    @Value("${cookie.refresh-name:refreshToken}")
    private String refreshCookieName;

    @Value("${cookie.path:/}")
    private String path;

    @Value("${cookie.secure:false}")
    private boolean secure;

    @Value("${cookie.same-site:Lax}")
    private String sameSite;

    /**
     * Set refresh token vào HttpOnly cookie với maxAge = thời gian sống của refresh token.
     */
    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken, long maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(refreshCookieName, refreshToken)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(path)
                .maxAge(maxAgeSeconds)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * Đọc refresh token từ cookie.
     * @return refresh token hoặc null nếu không có.
     */
    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (jakarta.servlet.http.Cookie c : cookies) {
            if (refreshCookieName.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

    /**
     * Xóa refresh token cookie (dùng khi logout).
     * Set Max-Age=0 để browser tự xóa.
     */
    public void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(refreshCookieName, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(path)
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
