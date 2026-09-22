package com.example.java_template.common.security;

import org.springframework.stereotype.Component;

import java.io.Serializable;

import org.jspecify.annotations.NonNull;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

/**
 * Stub cho phép {@code hasPermission(...)} trong SpEL — hiện luôn trả {@code true}.
 *
 * <p>Mở rộng khi cần check theo OBJECT (vd "user A có được approve bản ghi B không") bằng:</p>
 * <pre>
 *   @PreAuthorize("hasPermission(#id, 'Collateral', 'approve')")
 * </pre>
 *
 * <p>Khi đó tự load object theo id trong hàm và áp ràng buộc nghiệp vụ.</p>
 *
 * <p>Được cắm vào SpEL thông qua {@link com.example.java_template.common.config.MethodSecurityConfig}.</p>
 */
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(@NonNull Authentication auth, @NonNull Object targetDomainObject,
                                 @NonNull Object permission) {
        return true;
    }

    @Override
    public boolean hasPermission(@NonNull Authentication auth, @NonNull Serializable targetId,
                                 @NonNull String targetType, @NonNull Object permission) {
        return true;
    }
}
