package com.example.java_template.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import com.example.java_template.common.response.ApiResponse;
import com.example.java_template.common.response.ApiResponseFactory;
import com.example.java_template.common.response.DomainCode;

import java.io.IOException;

/**
 * Xử lý lỗi auth ở TẦNG FILTER (trước DispatcherServlet) — nơi mà
 * {@code @RestControllerAdvice} KHÔNG bắt được. Tự ghi {@link ApiResponse} JSON để
 * 401/403 đồng nhất format với các response còn lại.
 *
 * <p>Cắm vào {@code SecurityConfig} qua:</p>
 * <pre>
 *   .oauth2ResourceServer(o -> o.authenticationEntryPoint(this).accessDeniedHandler(this))
 *   .exceptionHandling(ex -> ex.authenticationEntryPoint(this).accessDeniedHandler(this))
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RestSecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ApiResponseFactory responseFactory;
    private final ObjectMapper objectMapper;

    /** 401 — thiếu / sai / hết hạn token. */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        log.warn("Unauthorized: {} - {}", request.getRequestURI(), authException.getMessage());
        write(response, DomainCode.UNAUTHORIZED);
    }

    /** 403 — có token nhưng không đủ quyền (chặn ở tầng URL). */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        log.warn("Forbidden: {} - {}", request.getRequestURI(), accessDeniedException.getMessage());
        write(response, DomainCode.FORBIDDEN);
    }

    private void write(HttpServletResponse response, DomainCode code) throws IOException {
        ApiResponse<?> body = responseFactory.error(code);
        response.setStatus(code.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
