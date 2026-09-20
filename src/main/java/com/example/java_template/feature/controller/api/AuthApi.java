package com.example.java_template.feature.controller.api;

import com.example.java_template.common.response.ApiResponse;
import com.example.java_template.feature.model.request.LoginRequest;
import com.example.java_template.feature.model.response.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.v3.oas.annotations.Operation;

@RequestMapping("api/auth")
public interface AuthApi {
    @Operation(summary = "Đăng nhập tài khoản - trả access token trong body, refresh token set vào HttpOnly cookie")
    @PostMapping("/login")
    ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response);

    @Operation(summary = "Đăng xuất - revoke refresh token, clear cookie")
    @PostMapping("/logout")
    ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response);

    @Operation(summary = "Refresh token - đọc refresh từ HttpOnly cookie, rotate, set cookie mới + cấp access mới")
    @PostMapping("/refreshToken")
    ApiResponse<LoginResponse> refreshToken(HttpServletRequest request, HttpServletResponse response);
}
