package com.example.java_template.common.security;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;

import static com.example.java_template.common.util.Const.ADMIN;
import static com.example.java_template.common.util.SecurityUtil.getPermissions;
import static com.example.java_template.common.util.SecurityUtil.getRoles;

/**
 * Check quyền cấp ACTION — dùng trong SpEL của {@code @PreAuthorize} / {@code @RequiresPermission}.
 *
 * <p>Quy tắc: user có role {@link #ADMIN} -> luôn pass; ngược lại check theo permission code
 * đã được {@link KeycloakJwtAuthenticationConverter} nạp vào SecurityContext.</p>
 *
 * <p>Ví dụ:</p>
 * <pre>
 *   @PreAuthorize("@permissionChecker.has('application:create')")
 * </pre>
 */
@Component("permissionChecker")
public class PermissionChecker {

    public boolean has(String permissionCode) {
        if (getRoles().contains(ADMIN)) {
            return true;
        }
        return getPermissions().contains(permissionCode);
    }

    public boolean hasAny(String... codes) {
        if (getRoles().contains(ADMIN)) {
            return true;
        }
        Set<String> permissions = getPermissions();
        for (String code : codes) {
            if (permissions.contains(code)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAll(String... codes) {
        if (getRoles().contains(ADMIN)) {
            return true;
        }
        Set<String> permissions = getPermissions();
        for (String code : codes) {
            if (!permissions.contains(code)) {
                return false;
            }
        }
        return true;
    }
}
