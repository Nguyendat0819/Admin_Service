package com.example.java_template.feature.service;

import com.example.java_template.feature.model.request.LoginRequest;
import com.example.java_template.feature.model.response.LoginResponse;
import com.example.java_template.feature.model.response.TokenPair;

public interface AuthService {

    /**
     * Đăng nhập → trả về cặp (accessToken, refreshToken).
     * Controller sẽ set refreshToken vào HttpOnly cookie, body chỉ chứa accessToken.
     */
    TokenPair login(LoginRequest request);

    /**
     * Refresh: rotate refresh token → cấp cặp mới.
     * @param refreshToken refresh token từ HttpOnly cookie.
     * @return TokenPair mới.
     */
    TokenPair refresh(String refreshToken);

    /**
     * Đăng xuất: vô hiệu hóa refresh token trong Redis (cookie clear do controller xử lý).
     * @param refreshToken refresh token từ cookie (null được phép).
     */
    void logout(String refreshToken);
}
