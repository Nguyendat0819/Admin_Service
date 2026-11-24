package com.example.java_template.common.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.java_template.common.util.Const.ANONYMOUS;
import static com.example.java_template.common.util.Const.PREFERRED_USERNAME;
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
//  để lấy tên người dùng (username) vì trong các hệ thống dùng JWT
    public static String getCurrentUsername() {
        return getJwt().map(jwt -> jwt.getClaimAsString(PREFERRED_USERNAME)).orElse(ANONYMOUS);
    }
    // Role của người đang gọi, đọc từ JWT; không có token thì trả tập rỗng.
    public static Set<String> getRoles() {
        Set<String> roles = new HashSet<>();
        if (getJwt().isPresent()) {
            roles = getRolesFromJwt(getJwt().get());
        }
        return roles;
    }

    public static Set<String> getRolesFromJwt(Jwt jwt) {
        if (jwt == null) {
            return Set.of();
        }
        List<String> roles = jwt.getClaimAsStringList(ROLES);
        return roles == null ? Set.of() : new HashSet<>(roles);
    }

    /**
     * Trả về {@code authorities} mà Spring Security đã gán cho user (đã gồm ROLE_* + permissionCode).
     *
     * <p>Lọc theo ký tự {@code ":"} vì permission theo quy ước có dạng {@code feature:action}.</p>
     */
    public static Set<String> getPermissions() {
        Set<String> permissions = new HashSet<>();
        // SecurityContextHolder để lưu trữ thông tin bảo mật của người dùng hiện tại -> lấy được authentication -> getAuthority(quyền)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            // Lấy danh sách quyền (Authorities) của user
            permissions = authentication.getAuthorities().stream()
                    // Chuyển mỗi GrantedAuthority thành String
                    .map(GrantedAuthority::getAuthority)
                    // Loại bỏ các giá trị null
                    .filter(Objects::nonNull)
                    // Chỉ giữ lại những authority có chứa dấu :
                    .filter(auth -> auth.contains(":"))
                    // Thu thập kết quả thành Set
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
