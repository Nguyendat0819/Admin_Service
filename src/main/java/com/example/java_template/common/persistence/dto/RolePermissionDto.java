package com.example.java_template.common.persistence.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Cấu hình role -> danh sách permission code.
 * Dùng cho cache phân quyền (xem {@link com.example.java_template.common.security.RolePermissionCache}).
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionDto {
    String role;
    List<String> permissions;
}
