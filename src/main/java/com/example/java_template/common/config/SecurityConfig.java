package com.example.java_template.common.config;

import com.example.java_template.common.security.AppJwtAuthenticationConverter;
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

// SecurityConfig — cấu hình bảo mật tầng FILTER.
// Mọi request đi qua chuỗi filter này TRƯỚC khi tới controller:
// verify JWT -> dựng Authentication (kèm roles/permission) -> quyết định cho qua hay chặn.
@Configuration               // class định nghĩa bean
@EnableWebSecurity           // bật Spring Security web -> cho phép tự định nghĩa SecurityFilterChain
@EnableMethodSecurity        // bật @PreAuthorize / @PostAuthorize (không có -> các annotation đó bị bỏ qua)
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppJwtAuthenticationConverter appJwtAuthenticationConverter;
    private final RestSecurityExceptionHandler restSecurityExceptionHandler;
    private final CorsProperties  corsProperties;
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
        // Rule phân quyền theo URL: khai báo cụ thể trước, anyRequest() để cuối làm mặc định.
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(appJwtAuthenticationConverter))
                        .authenticationEntryPoint(restSecurityExceptionHandler)
                        .accessDeniedHandler(restSecurityExceptionHandler))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restSecurityExceptionHandler)
                        .accessDeniedHandler(restSecurityExceptionHandler));

        return http.build();
    }

    // Khai báo CORS: origin/method/header nào được phép gọi cross-origin.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
