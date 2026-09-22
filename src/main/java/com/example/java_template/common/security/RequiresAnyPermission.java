package com.example.java_template.common.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Cho qua nếu user có ÍT NHẤT 1 trong các quyền (OR).
 *
 * <p>Ví dụ:</p>
 * <pre>
 *   &#64;RequiresAnyPermission({"application:create", "application:update"})
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("@permissionChecker.hasAny('{value}'.split(','))")
public @interface RequiresAnyPermission {

    String[] value();
}
