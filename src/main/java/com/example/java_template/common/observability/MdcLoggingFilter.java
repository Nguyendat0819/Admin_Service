package com.example.java_template.common.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.java_template.common.util.SecurityUtil;

import java.io.IOException;
import java.util.UUID;

import static com.example.java_template.common.util.Const.HEADER_REQUEST_ID;
import static com.example.java_template.common.util.Const.REQUEST_ID;
import static com.example.java_template.common.util.Const.USERNAME;

/**
 * Bơm {@code requestId} + {@code username} vào MDC cho mỗi request, giúp log có context
 * đầy đủ để truy vết và đối chiếu giữa các dòng log.
 *
 * <p>requestId: ưu tiên header {@code X-Request-Id} (gateway/API truyền xuống);
 * không có thì tự sinh UUID. Trả lại header cho client để đối chiếu.</p>
 *
 * <p>Thread Tomcat được tái dùng -> cuối filter nhớ {@code MDC.clear()} để không rò
 * context sang request sau.</p>
 */
@Component
public class MdcLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String requestId = request.getHeader(HEADER_REQUEST_ID);
            if (requestId == null || requestId.isBlank()) {
                requestId = UUID.randomUUID().toString();
            }
            MDC.put(REQUEST_ID, requestId);
            MDC.put(USERNAME, SecurityUtil.getCurrentUsername());

            response.setHeader(HEADER_REQUEST_ID, requestId);

            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
