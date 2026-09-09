package com.example.java_template.feature.service.impl;

import com.example.java_template.common.util.JwtUtil;
import com.example.java_template.feature.model.request.LoginRequest;
import com.example.java_template.feature.model.response.LoginResponse;
import com.example.java_template.feature.service.AuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {
    private JwtUtil jwtUtil;
    private TokenBlacklistService tokenBlacklistService;
    PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request){
        String token = jwtUtil.generateToken(request.getUsername());

        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .expiresIn(jwtUtil.getExpiration())
                .build();
    }

    @Override
    public void logout(String token){
        if(token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
        }

        tokenBlacklistService.addBlacklistedToken(token);
    }
}
