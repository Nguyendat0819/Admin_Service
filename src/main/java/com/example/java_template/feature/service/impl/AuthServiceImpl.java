package com.example.java_template.feature.service.impl;

import com.example.java_template.feature.model.request.AuthLoginRequest;
import com.example.java_template.feature.model.response.AuthResponse;
import com.example.java_template.feature.model.response.AuthTokenResponse;
import com.example.java_template.feature.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final Duration accessTokenTtl;
    private final Map<String, DemoUser> users;

    public AuthServiceImpl(
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.access-token-ttl}") Duration accessTokenTtl) {
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.accessTokenTtl = accessTokenTtl;
        this.users = Map.of(
                "admin", new DemoUser(passwordEncoder.encode("admin123"), "admin@demo.local",
                        "Admin", "Demo", List.of("ADMIN", "USER")),
                "user", new DemoUser(passwordEncoder.encode("user123"), "user@demo.local",
                        "Demo", "User", List.of("USER"))
        );
    }

    @Override
    public AuthTokenResponse login(AuthLoginRequest request) {
        DemoUser user = users.get(request.username());
        if (user == null || !passwordEncoder.matches(request.password(), user.passwordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(request.username())
                .issuedAt(now)
                .expiresAt(now.plus(accessTokenTtl))
                .claim("preferred_username", request.username())
                .claim("email", user.email())
                .claim("given_name", user.firstName())
                .claim("family_name", user.lastName())
                .claim("roles", user.roles())
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        return new AuthTokenResponse(token, "Bearer", accessTokenTtl.toSeconds());
    }

    @Override
    public AuthResponse getCurrentUser(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles == null) {
            roles = List.of();
        }

        return AuthResponse.builder()
                .user(jwt.getClaimAsString("preferred_username"))
                .email(jwt.getClaimAsString("email"))
                .firstName(jwt.getClaimAsString("given_name"))
                .lastName(jwt.getClaimAsString("family_name"))
                .roles(roles)
                .build();
    }

    private record DemoUser(
            String passwordHash,
            String email,
            String firstName,
            String lastName,
            List<String> roles
    ) {
    }
}
