package com.example.java_template.feature.model.response;

public record AuthTokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
