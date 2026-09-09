package com.example.java_template.feature.controller.api;

import com.example.java_template.common.response.ApiResponse;
import com.example.java_template.feature.model.request.LoginRequest;
import com.example.java_template.feature.model.response.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.v3.oas.annotations.Operation;
@RequestMapping("api/auth")
public interface AuthApi {
    @Operation(summary = "Đăng nhập tài khoản")
    @PostMapping
    ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request);

    @Operation(summary = "Đăng xuất tài khoản")
    @PostMapping
    ApiResponse<Void> logout(HttpServletRequest  request);
}
