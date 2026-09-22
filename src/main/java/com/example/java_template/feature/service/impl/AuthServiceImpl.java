package com.example.java_template.feature.service.impl;

import com.example.java_template.feature.model.response.AuthResponse;
import com.example.java_template.feature.service.AuthService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    @Override
    public AuthResponse getCurrentUser(Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");
        String firstName = jwt.getClaimAsString("given_name");
        String lastName = jwt.getClaimAsString("family_name");

        // Realm roles: realm_access.roles  →  ["ADMIN", "USER"]
        List<String> roles = new ArrayList<>();
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof List<?> rr) {
            rr.forEach(r -> roles.add(r.toString()));
        }

        // Client roles: resource_access.Auth.roles
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess != null) {
            resourceAccess.forEach((clientId, acces) -> {
                if (acces instanceof Map<?, ?> ca && ca.get("roles") instanceof List<?> cr) {
                    cr.forEach(r -> roles.add(r.toString()));
                }
            });
        }

        return AuthResponse.builder()
                .user(username)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .roles(roles)
                .build();
    }
}
