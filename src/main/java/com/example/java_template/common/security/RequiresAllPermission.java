package com.example.java_template.common.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Cho qua nếu user có TẤT CẢ các quyền (AND).
 *
 * <p>Ví dụ:</p>
 * <pre>
 *   &#64;RequiresAllPermission({"application:read", "application:export"})
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("@permissionChecker.hasAll('{value}'.split(','))")
public @interface RequiresAllPermission {

    String[] value();
}
