package com.example.java_template.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình OpenAPI / Swagger UI cho toàn bộ API.
 *
 * <p>Mở Swagger UI tại: {@code http://localhost:8080/swagger-ui.html}
 * (hoặc {@code /swagger-ui/index.html} trên springdoc 2.x+).</p>
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Java Template API")
                        .version("v1.0.0")
                        .description("REST API backend template — Spring Boot 4.x + OAuth2 Resource Server (Keycloak)")
                        .contact(new Contact()
                                .name("Backend Team")
                                .email("backend@example.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://example.com")))
                // Đăng ký scheme Bearer: áp dụng cho mọi endpoint yêu cầu JWT
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Nhập JWT access token (không cần tiền tố 'Bearer ')")));
    }
}
