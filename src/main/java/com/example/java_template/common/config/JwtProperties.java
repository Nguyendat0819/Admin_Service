package com.example.java_template.common.config;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component
@ConfigurationProperties(prefix = "app.jwt")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtProperties {
    String issuer;
    long accessTokenTtl;
    String secretBase64;
}
