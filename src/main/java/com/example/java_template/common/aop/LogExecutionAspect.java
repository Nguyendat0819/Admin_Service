package com.example.java_template.common.aop;

import com.example.java_template.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * AOP tự log [REQUEST] (body) + [RESPONSE] (body) + thời gian cho mọi method trong
 * {@code ...feature.controller...} hoặc method gắn {@link LogExecution}.
 *
 * <p>Body được JSON-hoá riêng từng arg (1 arg lỗi không làm hỏng cả dòng log) + cắt theo
 * {@code app.log.max-payload-length} (default 1000 ký tự) để log không phình quá to.</p>
 */
@Aspect
@Component
@RequiredArgsConstructor
public class LogExecutionAspect {

    private final ObjectMapper objectMapper;

    @Value("${app.log.max-payload-length:1000}")
    private int maxPayloadLength;

    @Around("@annotation(com.example.java_template.common.aop.LogExecution) "
            + "|| within(com.example.java_template.feature.controller..*)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Logger logger = LoggerFactory.getLogger(method.getDeclaringClass());

        String http = currentRequest()
                .map(r -> r.getMethod() + " " + r.getRequestURI())
                .orElse(method.getName());

        logger.info("[REQUEST] {} - actor={} - body={}",
                http, SecurityUtil.getCurrentUsername(), truncate(argsToJson(joinPoint.getArgs())));
        try {
            Object result = joinPoint.proceed();
            logger.info("[RESPONSE] {} - {}ms - body={}",
                    http, System.currentTimeMillis() - start, truncate(toJson(result)));
            return result;
        } catch (Throwable ex) {
            logger.warn("[RESPONSE-ERR] {} - {}ms - {}", http, System.currentTimeMillis() - start, ex.toString());
            throw ex;
        }
    }

    private Optional<jakarta.servlet.http.HttpServletRequest> currentRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest);
    }

    private String argsToJson(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        return Arrays.stream(args).map(this::toJson).collect(Collectors.joining(", ", "[", "]"));
    }

    /** JSON-hoá an toàn: không serialize được -> dùng {@code String.valueOf}, không ném lỗi ra ngoài. */
    private String toJson(Object o) {
        if (o == null) {
            return "null";
        }
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            return String.valueOf(o);
        }
    }

    private String truncate(String s) {
        if (s == null || s.length() <= maxPayloadLength) {
            return s;
        }
        return s.substring(0, maxPayloadLength) + "...(" + s.length() + " ký tự, đã cắt)";
    }
}
