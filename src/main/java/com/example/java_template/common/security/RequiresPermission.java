package com.example.java_template.common.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation gọn thay cho {@code @PreAuthorize("@permissionChecker.has('{value}')")}.
 *
 * <p>Ví dụ:</p>
 * <pre>
 *   &#64;RequiresPermission("application:create")
 * </pre>
 *
 * <p>Cơ chế: bean {@code AnnotationTemplateExpressionDefaults} (xem
 * {@link com.example.java_template.common.config.MethodSecurityConfig}) thay thế
 * chuỗi {@code {value}} bằng giá trị thật trước khi SpEL engine đánh giá.</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("@permissionChecker.has('{value}')")
public @interface RequiresPermission {

    String value();
}
