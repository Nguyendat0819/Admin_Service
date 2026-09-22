package com.example.java_template.common.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.java_template.common.util.Const.ANONYMOUS;
import static com.example.java_template.common.util.Const.PREFERRED_USERNAME;
import static com.example.java_template.common.util.Const.REALM_ACCESS;
import static com.example.java_template.common.util.Const.ROLES;

/**
 * Đọc thông tin user hiện tại từ {@link SecurityContextHolder}.
 *
 * <p>Pattern: dùng {@link UtilityClass} để Lombok generate:
 * class final + private constructor + method static; tránh gọi {@code new SecurityUtil()}.</p>
 */
@UtilityClass
public final class SecurityUtil {

    public static Optional<Jwt> getJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return Optional.of(jwtAuth.getToken());
        }
        return Optional.empty();
    }

    public static String getCurrentUsername() {
        return getJwt().map(jwt -> jwt.getClaimAsString(PREFERRED_USERNAME)).orElse(ANONYMOUS);
    }

    public static Set<String> getRoles() {
        Set<String> roles = new HashSet<>();
        if (getJwt().isPresent()) {
            roles = getRolesFromJwt(getJwt().get());
        }
        return roles;
    }

    @SuppressWarnings("unchecked")
    public static Set<String> getRolesFromJwt(Jwt jwt) {
        Set<String> roles = new HashSet<>();
        if (jwt == null) {
            return roles;
        }
        Map<String, Object> realmAccess = jwt.getClaimAsMap(REALM_ACCESS);
        if (realmAccess != null) {
            Object rolesObj = realmAccess.get(ROLES);
            if (rolesObj instanceof List<?> list) {
                roles = list.stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .collect(Collectors.toSet());
            }
        }
        return roles;
    }

    /**
     * Trả về {@code authorities} mà Spring Security đã gán cho user (đã gồm ROLE_* + permissionCode).
     *
     * <p>Lọc theo ký tự {@code ":"} vì permission theo quy ước có dạng {@code feature:action}.</p>
     */
    public static Set<String> getPermissions() {
        Set<String> permissions = new HashSet<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            permissions = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(Objects::nonNull)
                    .filter(auth -> auth.contains(":"))
                    .collect(Collectors.toSet());
        }
        return permissions;
    }

    public static String getTokenValue() {
        return getJwt().map(Jwt::getTokenValue).orElse(null);
    }

    public static boolean isAuthenticated() {
        return getJwt().isPresent();
    }
}
