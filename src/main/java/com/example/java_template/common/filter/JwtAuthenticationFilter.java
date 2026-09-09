package com.example.java_template.common.filter;

import com.example.java_template.common.exception.BusinessException;
import com.example.java_template.common.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver exceptionResolver;

    // Trích xuất token
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // Dùng JwtUtil để kiểm tra (hàm validateToken) và lấy ra username
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request); // Lấy token ra
            
            // Nếu có token thì mới validate (Vì có những API public không truyền token)
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);

                // Tạo Authentication object và lưu vào SecurityContext
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        Collections.emptyList()); // authorities/roles

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            
            // Tiếp tục chuỗi filter
            filterChain.doFilter(request, response);
            
        } catch (BusinessException e) {
            // Khi token không hợp lệ (JwtUtil ném lỗi BusinessException)
            // Nhờ GlobalExceptionHandler xử lý để trả về ApiResponse JSON đồng nhất
            exceptionResolver.resolveException(request, response, null, e);
        } catch (Exception e) {
            // Nhờ GlobalExceptionHandler xử lý các lỗi ngoài ý muốn khác
            exceptionResolver.resolveException(request, response, null, e);
        }
    }
}
