package com.example.java_template.common.persistence;

import com.example.java_template.common.util.SecurityUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * Kích hoạt JPA Auditing + cung cấp {@link AuditorAware} để các field
 * {@code @CreatedBy}/{@code @LastModifiedBy} trong {@link BaseEntity} nhận giá trị
 * từ user hiện tại (lấy qua {@link SecurityUtil#getCurrentUsername()}).
 *
 * <p>Được tách riêng (không đặt lên Application class) để:</p>
 * <ul>
 *     <li>Tránh vòng phụ thuộc khi tạo bean;</li>
 *     <li>Dễ tắt/mở trong test qua {@code excludeFilters} của {@code @SpringBootApplication}.</li>
 * </ul>
 *
 * <p><b>Bật/tắt theo property</b> {@code app.database.enabled} (mặc định {@code true}).
 * Khi dev chưa cấu hình DB thật, set {@code app.database.enabled=false} trong
 * {@code application-dev.yaml} để bean này không khởi tạo (đỡ phải kéo theo
 * toàn bộ JPA autoconfig).</p>
 */
@Configuration
@ConditionalOnProperty(name = "app.database.enabled", havingValue = "true", matchIfMissing = true)
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.ofNullable(SecurityUtil.getCurrentUsername());
    }
}
