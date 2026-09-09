package com.example.java_template.feature.service;

import com.example.java_template.feature.model.request.LoginRequest;
import com.example.java_template.feature.model.response.LoginResponse;

public interface AuthService {
    public LoginResponse login(LoginRequest request);
    public void logout(String token);
}
