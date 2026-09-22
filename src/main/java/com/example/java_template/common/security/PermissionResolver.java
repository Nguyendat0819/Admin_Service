package com.example.java_template.common.security;

import java.util.Collection;
import java.util.Set;

/**
 * Đổi tập role -> tập permission code.
 *
 * <p>Triển khai mặc định là {@link RolePermissionCache} (in-memory cache, refresh định kỳ
 * từ Admin Manager). Có thể thay thế bằng cài đặt khác (DB, config server...) mà không
 * ảnh hưởng {@link PermissionChecker}.</p>
 */
public interface PermissionResolver {

    Set<String> permissionsOf(Collection<String> roles);
}
