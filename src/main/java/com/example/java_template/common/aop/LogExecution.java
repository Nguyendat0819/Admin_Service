package com.example.java_template.common.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Đánh dấu method cần log execution. Mặc định chỉ log REQUEST; bật {@link #logResponse()}
 * nếu cần log cả RESPONSE body.
 *
 * <p>Lưu ý: Toàn bộ {@code *Controller} trong package {@code feature.controller} được
 * log tự động qua pointcut {@code within(...)} — không cần annotation.</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecution {

    boolean logResponse() default false;
}
