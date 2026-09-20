package com.example.java_template.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Cấu hình Redis + fallback ObjectMapper cho refresh token store.
 *
 * <h3>Bean {@link StringRedisTemplate}</h3>
 * <p>Dùng để serialize/deserialize thủ công (qua Jackson + JSON) cho
 * {@code RefreshTokenData} — giúp dễ debug khi dùng {@code redis-cli}
 * và tránh phụ thuộc vào Java class.</p>
 *
 * <h3>Bean {@link ObjectMapper} (fallback)</h3>
 * <p>Spring Boot 4.x modular starter ({@code spring-boot-starter-webmvc})
 * <b>không tự động</b> cấu hình ObjectMapper như starter {@code web} cũ.
 * Để {@code JacksonAutoConfiguration} trigger, project PHẢI có
 * {@code spring-boot-starter-json} trên classpath.</p>
 *
 * <p>Bean này được khai báo explicit + đánh dấu {@link ConditionalOnMissingBean}
 * — chỉ thực sự được tạo khi không có bean {@code ObjectMapper} nào khác.</p>
 *
 * <p>Nếu {@code spring-boot-starter-json} đã có (recommended) thì auto-config
 * sẽ tạo bean primary và bean này bị bỏ qua (không trùng).</p>
 */
@Configuration
public class RedisConfig {

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    /**
     * ObjectMapper dự phòng.
     * <p>Chỉ được tạo khi KHÔNG có bean ObjectMapper nào trong context
     * (Spring Boot auto-config từ spring-boot-starter-json).</p>
     */
    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapperFallback() {
        return new ObjectMapper();
    }
}
