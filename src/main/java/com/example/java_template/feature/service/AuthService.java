package com.example.java_template.feature.service;

import com.example.java_template.feature.model.response.AuthResponse;
import org.springframework.security.oauth2.jwt.Jwt;

public interface AuthService {
    AuthResponse getCurrentUser(Jwt jwt);
}
