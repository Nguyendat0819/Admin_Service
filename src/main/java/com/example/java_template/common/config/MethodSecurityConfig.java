package com.example.java_template.common.config;

import com.example.java_template.common.security.CustomPermissionEvaluator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.core.annotation.AnnotationTemplateExpressionDefaults;

/**
 * Cắm {@link CustomPermissionEvaluator} vào tầng METHOD security + bật template
 * {@code '{value}'} trong các meta-annotation ({@code @RequiresPermission}, ...).
 *
 * <p>Hai bean đều dùng {@code static} để tránh Spring khởi tạo cả class
 * {@code @Configuration} quá sớm — pattern khuyến nghị của Spring cho loại này.</p>
 */
@Configuration
public class MethodSecurityConfig {

    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(CustomPermissionEvaluator evaluator) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(evaluator);
        return handler;
    }

    /**
     * KHÔNG có bean này, Spring hiểu {@code '{value}'} theo nghĩa đen
     * -> check quyền tên "{value}" -> luôn 403.
     */
    @Bean
    static AnnotationTemplateExpressionDefaults annotationTemplateExpressionDefaults() {
        return new AnnotationTemplateExpressionDefaults();
    }
}
