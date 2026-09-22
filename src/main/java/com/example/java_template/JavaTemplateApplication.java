package com.example.java_template;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Điểm vào ứng dụng.
 *
 * <ul>
 *     <li>{@code @EnableAsync} — bật {@code @Async} trên method (gọi task ở thread pool).</li>
 *     <li>{@code @EnableScheduling} — bật {@code @Scheduled}. KHÔNG có nó thì mọi
 *         {@code @Scheduled} im lặng không chạy, không log, không lỗi.</li>
 *     <li>{@code @EnableConfigurationProperties} — cho phép bind property từ yaml vào
 *         class {@code @ConfigurationProperties} (vd {@link com.example.java_template.common.httpclient.InternalProperties}).</li>
 * </ul>
 *
 * <p>{@code @EnableJpaAuditing} được tách riêng trong
 * {@link com.example.java_template.common.persistence.JpaAuditingConfig}.</p>
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties
public class JavaTemplateApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaTemplateApplication.class, args);
    }
}
