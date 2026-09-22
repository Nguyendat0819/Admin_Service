package com.example.java_template.common.config;

import com.example.java_template.common.security.KeycloakJwtAuthenticationConverter;
import com.example.java_template.common.security.RestSecurityExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Cấu hình bảo mật tầng FILTER.
 *
 * <p>Mọi request đi qua chuỗi filter này TRƯỚC khi tới controller:
 * verify JWT -> dựng Authentication (kèm roles/permission) -> quyết định cho qua hay chặn.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter;
    private final RestSecurityExceptionHandler restSecurityExceptionHandler;

    @Value("${app.security.enabled:true}")
    private boolean securityEnabled;

    private static final String[] PUBLIC_ENDPOINTS = {
            "/actuator/health/**",
            "/actuator/info",
            "/actuator/prometheus",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF: REST stateless dùng Bearer token -> CSRF không áp dụng.
                .csrf(AbstractHttpConfigurer::disable)
                // Bật CORS: tìm bean CorsConfigurationSource theo type.
                .cors(Customizer.withDefaults())
                // Stateless: không tạo session; mỗi request tự mang token.
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (!securityEnabled) {
            // Tắt auth cho cụm local/test: truy cập tự do, log vẫn in user thật qua MDC.
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtAuthenticationConverter))
                        .authenticationEntryPoint(restSecurityExceptionHandler)
                        .accessDeniedHandler(restSecurityExceptionHandler))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restSecurityExceptionHandler)
                        .accessDeniedHandler(restSecurityExceptionHandler));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "http://localhost:8080"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
