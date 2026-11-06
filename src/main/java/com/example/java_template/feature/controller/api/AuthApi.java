package com.example.java_template.feature.controller.api;

import com.example.java_template.common.response.ApiResponse;
import com.example.java_template.feature.model.request.AuthLoginRequest;
import com.example.java_template.feature.model.response.AuthResponse;
import com.example.java_template.feature.model.response.AuthTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/auth")
public interface AuthApi {
    @Operation(summary = "Đăng nhập và lấy access token")
    @PostMapping("/login")
    ApiResponse<AuthTokenResponse> login(@Valid @RequestBody AuthLoginRequest request);

    @Operation(summary = "Lấy thông tin người dùng")
    @GetMapping("/me")
    ApiResponse<AuthResponse> getCurrentUser(@AuthenticationPrincipal Jwt jwt);
}
