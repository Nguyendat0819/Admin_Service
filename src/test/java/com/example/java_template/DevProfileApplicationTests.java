package com.example.java_template;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Mockito.mock;

/**
 * Verify Spring context khởi động được với profile {@code dev}
 * (H2 in-memory + security tắt theo application-dev.yaml).
 *
 * <p>Mục tiêu: bắt lỗi "Failed to configure a DataSource" sớm trong CI
 * mà không cần dựng PostgreSQL thật.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
class DevProfileApplicationTests {

    @Test
    void contextLoads() {
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        JwtDecoder jwtDecoder() {
            return mock(JwtDecoder.class);
        }
    }
}
