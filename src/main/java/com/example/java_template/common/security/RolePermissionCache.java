package com.example.java_template.common.security;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Stub {@link PermissionResolver}: trả tập rỗng theo mặc định. Mục đích cho phép
 * {@link KeycloakJwtAuthenticationConverter} khởi động mà không cần triển khai
 * cache thật — đủ để dev local / chạy test.
 *
 * <h3>Nâng cấp lên cache thật</h3>
 * <p>Để gọi Admin Manager lấy role -> permission, hãy:</p>
 * <ol>
 *   <li>Thêm interface {@code AdminManagerClient} trong package
 *       {@code com.example.java_template.feature.client}, có method
 *       {@code getAllRolePermissions()}.</li>
 *   <li>Inject {@code AdminManagerClient} vào class này (xoá {@code @Value(..."0")} và điều kiện stub).</li>
 *   <li>Trong {@link #reload()} gọi {@code adminManagerClient.getAllRolePermissions()},
 *       map sang {@code Map<String, Set<String>>} rồi {@code ref.set(...)}.</li>
 * </ol>
 *
 * <p>Cấu hình bật/tắt qua {@code app.security.cache.enabled} (default: {@code true}).</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.security.cache.enabled", havingValue = "true", matchIfMissing = true)
public class RolePermissionCache implements PermissionResolver {

    /** Khoảng cách tối thiểu giữa 2 lần nạp lazy — chặn gọi Admin dồn dập. */
    static long LAZY_MIN_INTERVAL_MS = 10_000L;

    @Value("${app.admin.refresh-ms:300000}")
    private long refreshMs;

    /** Cache role -> permission; swap nguyên khối khi reload để không thấy map dở dang. */
    private final AtomicReference<Map<String, Set<String>>> ref = new AtomicReference<>(Map.of());

    /** Thời điểm nạp gần nhất — dùng để throttle nạp lazy. */
    private final AtomicLong lastReloadAt = new AtomicLong(0);

    @Override
    public Set<String> permissionsOf(Collection<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }
        Map<String, Set<String>> map = ref.get();
        Set<String> result = new HashSet<>();
        for (String role : roles) {
            result.addAll(map.getOrDefault(role, Set.of()));
        }
        return result;
    }

    /**
     * Stub nạp cache lúc khởi động + định kỳ sau mỗi {@link #refreshMs} ms.
     *
     * <p>Khi bật cache thật: thay bằng gọi {@code adminManagerClient} ở đây.</p>
     */
    @PostConstruct
    @Scheduled(fixedRateString = "${app.admin.refresh-ms:300000}",
            initialDelayString = "${app.admin.refresh-ms:300000}")
    public void reload() {
        // Stub: cache rỗng. Cắm logic thật khi có adminManagerClient.
        lastReloadAt.set(System.currentTimeMillis());
        log.debug("RolePermissionCache reload (stub) — cache hiện đang rỗng, cần cắm AdminManagerClient để có data");
    }

    /** Truy cập nội bộ cho test / debug. */
    public Map<String, Set<String>> currentCache() {
        return ref.get();
    }

    /** Tái nạp cache với map truyền vào (dùng cho test). */
    void reset(Map<String, Set<String>> newMap) {
        ref.set(newMap == null ? Map.of() : newMap);
        lastReloadAt.set(System.currentTimeMillis());
    }
}
