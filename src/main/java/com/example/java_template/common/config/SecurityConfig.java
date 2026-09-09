package com.example.java_template.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, com.example.java_template.common.filter.JwtAuthenticationFilter jwtAuthFilter) throws Exception {
        http
                // Tắt CSRF: REST stateless dùng Bearer token (không cookie) -> CSRF không áp dụng.
                .csrf(AbstractHttpConfigurer::disable)
                
                // Bật CORS: Spring Security tự tìm bean CorsConfigurationSource
                .cors(Customizer.withDefaults())
                
                // Phân quyền request
                .authorizeHttpRequests(auth -> auth
                        // Cho phép truy cập công khai vào các API đăng nhập/đăng ký
                        .requestMatchers("/api/auth/**").permitAll()
                        
                        // Cấu hình thêm các endpoint public khác ở đây nếu có (ví dụ swagger, v.v.)
                        // .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        
                        // Bất kỳ request nào khác đều bắt buộc phải có token hợp lệ
                        .anyRequest().authenticated()
                )
                
                // Không tạo/dùng HTTP session (vì đang dùng JWT)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // Thêm filter JWT vào TRƯỚC UsernamePasswordAuthenticationFilter của Spring Security
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Khai báo CORS: origin/method/header nào được phép gọi cross-origin.
    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:4200","http://localost:8080"));

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "PATCH",
                "OPTIONS"
        ));
        // Cho phép frontend gửi tất cả HTTP Header.
        // Ví dụ:
        // Authorization: Bearer <JWT>
        // Content-Type: application/json
        // Accept: application/json
        configuration.setAllowedHeaders(List.of("*"));
        // Cho phép request gửi credentials.
        configuration.setAllowCredentials(true);

        // Tạo đối tượng dùng để ánh xạ URL → CORS configuration.
        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        // Áp dụng CORS configuration cho tất cả URL.
        source.registerCorsConfiguration("/**", configuration);
        //"/**" nghĩa là mọi endpoint.
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}
