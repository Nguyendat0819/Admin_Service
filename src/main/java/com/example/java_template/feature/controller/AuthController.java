package com.example.java_template.feature.controller;

import com.example.java_template.common.response.ApiResponse;
import com.example.java_template.common.response.ApiResponseFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller xác thực — cung cấp thông tin user hiện tại từ JWT đã xác thực.
 *
 * <p>Frontend gọi {@code GET /api/auth/me} sau khi đã đăng nhập SSO Keycloak
 * để lấy user info (username, email, roles).</p>
 *
 * <p>Đăng nhập/đăng xuất không qua backend — hoàn toàn do Keycloak xử lý
 * (frontend redirect sang Keycloak, nhận token về).</p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Xác thực — lấy thông tin user hiện tại")
public class AuthController {

    private final ApiResponseFactory apiResponseFactory;

    @Operation(
            summary = "Lấy thông tin user hiện tại",
            description = "Trả về username, email, userId (Keycloak sub), roles (realm + client) từ JWT đã xác thực. "
                    + "Cần có Authorization: Bearer <access_token> hợp lệ."
    )
    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> userInfo = new LinkedHashMap<>();

        // Keycloak sub = UUID của user trong Keycloak
        userInfo.put("userId", jwt.getSubject());

        // Các claim OpenID chuẩn
        userInfo.put("username", jwt.getClaimAsString("preferred_username"));
        userInfo.put("email", jwt.getClaimAsString("email"));
        userInfo.put("firstName", jwt.getClaimAsString("given_name"));
        userInfo.put("lastName", jwt.getClaimAsString("family_name"));

        // Realm roles: realm_access.roles  →  ["ADMIN", "USER"]
        List<String> roles = new ArrayList<>();
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof List<?> rr) {
            rr.forEach(r -> roles.add(r.toString()));
        }

        // Client roles: resource_access.Auth.roles
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess != null) {
            resourceAccess.forEach((clientId, access) -> {
                if (access instanceof Map<?, ?> ca && ca.get("roles") instanceof List<?> cr) {
                    cr.forEach(r -> roles.add(r.toString()));
                }
            });
        }
        userInfo.put("roles", roles);

        // Thời điểm hết hạn token
        userInfo.put("tokenExpiresAt", jwt.getExpiresAt());

        return apiResponseFactory.success(userInfo);
    }
}
