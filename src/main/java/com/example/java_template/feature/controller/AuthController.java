package com.example.java_template.feature.controller;

import com.example.java_template.common.response.ApiResponse;
import com.example.java_template.common.response.ApiResponseFactory;
import com.example.java_template.common.util.JwtUtil;
import com.example.java_template.feature.controller.api.AuthApi;
import com.example.java_template.feature.model.request.LoginRequest;
import com.example.java_template.feature.model.response.LoginResponse;
import com.example.java_template.feature.service.AuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController implements AuthApi {
    private  final AuthService authService;
    private final ApiResponseFactory apiResponseFactory;
    private final JwtUtil jwtUtil;

    @Override
    public ApiResponse<LoginResponse> login(LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);
        return apiResponseFactory.success(loginResponse);
    }

    @Override
    public ApiResponse<Void> logout(HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);
        authService.logout(token);
        return apiResponseFactory.success(null);
    }
}
