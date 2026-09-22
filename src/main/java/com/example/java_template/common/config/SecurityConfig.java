package com.example.java_template.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Cấu hình Spring Security — OAuth2 Resource Server.
 *
 * <p>Backend là OAuth2 Resource Server: chỉ verify chữ ký JWT bằng Public Key
 * từ Keycloak JWKS endpoint. Không tự ký token, không quản lý session,
 * không lưu refresh token.</p>
 *
 * <p>JWT do Keycloak cấp chứa:</p>
 * <ul>
 *   <li>Realm roles  → {@code realm_access.roles}</li>
 *   <li>Client roles → {@code resource_access.<clientId>.roles}</li>
 * </ul>
 * Converter trong config này tự động extract cả hai loại role
 * thành {@code GrantedAuthority("ROLE_<name>")}.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity        // Bật @PreAuthorize("hasRole('ADMIN')") trên method/controller
public class SecurityConfig {

    @Value("${app.keycloak.base-url:http://localhost:8180}")
    private String keycloakBaseUrl;

    // ============================================================
    //  Security Filter Chain
    // ============================================================
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF: REST stateless dùng Bearer token → CSRF không áp dụng
                .csrf(AbstractHttpConfigurer::disable)

                // Bật CORS: Spring Security tự tìm bean CorsConfigurationSource
                .cors(Customizer.withDefaults())

                // Phân quyền request
                .authorizeHttpRequests(auth -> auth
                        // Cho phép truy cập công khai: Swagger, Actuator health, static resources
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/actuator/health"
                        ).permitAll()

                        // Tất cả request khác đều bắt buộc phải có JWT hợp lệ
                        .anyRequest().authenticated()
                )

                // Không tạo/dùng HTTP session (JWT stateless)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // OAuth2 Resource Server: Spring tự verify JWT từ Keycloak JWKS
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
                );

        return http.build();
    }

    // ============================================================
    //  JWT → Spring Security Authorities Converter
    //  Map Keycloak roles → SimpleGrantedAuthority("ROLE_<name>")
    // ============================================================
    @Bean
    public JwtAuthenticationConverter jwtAuthConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmAndClientRoleConverter());
        //preferred_username là claim chuẩn OpenID cho tên đăng nhập
        converter.setPrincipalClaimName("preferred_username");
        return converter;
    }

    /**
     * Extract GrantedAuthority từ cả:
     * - Realm roles  (realm_access.roles)
     * - Client roles (resource_access[clientId].roles)
     */
    private static class KeycloakRealmAndClientRoleConverter
            implements Converter<Jwt, Collection<GrantedAuthority>> {

        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            // --- Realm roles ---
            // Keycloak claim: { "realm_access": { "roles": ["ADMIN", "USER"] } }
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null && realmAccess.get("roles") instanceof List<?> realmRoles) {
                for (Object role : realmRoles) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toString()));
                }
            }

            // --- Client roles ---
            // Keycloak claim: { "resource_access": { "Auth": { "roles": [...] } } }
            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            if (resourceAccess != null) {
                for (Map.Entry<String, Object> entry : resourceAccess.entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof Map<?, ?> clientAccess
                            && clientAccess.get("roles") instanceof List<?> clientRoles) {
                        for (Object role : clientRoles) {
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toString()));
                        }
                    }
                }
            }

            return authorities;
        }
    }

    // ============================================================
    //  CORS Configuration
    // ============================================================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Cho phép origin của Angular dev server và các origin khác nếu cần
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:4200",
                "http://localhost:*"
        ));

        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // Cho phép frontend gửi mọi header, bao gồm Authorization: Bearer <JWT>
        config.setAllowedHeaders(List.of("*"));

        // Cho phép gửi credentials (token trong Authorization header)
        config.setAllowCredentials(true);

        // Cache preflight 1 giờ
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
