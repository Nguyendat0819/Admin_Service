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
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private static final String MASK = "***";
    private static final List<String> SENSITIVE_KEYS = List.of("password", "secret", "token", "credential");
    @Value("${app.log.max-payload-length:1000}")
    private int maxPayloadLength;

    @Around("@annotation(com.example.java_template.common.aop.LogExecution) "
            + "|| within(com.example.java_template.feature.controller..*)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Logger logger = LoggerFactory.getLogger(method.getDeclaringClass());

        String http = currentRequest()
                .map(r -> r.getMethod() + " " + r.getRequestURI())
                .orElse(method.getName());

        logger.info("[REQUEST] {} - actor={} - body={}",
                http, SecurityUtil.getCurrentUsername(),
                truncate(argsToJson(signature.getParameterNames(), joinPoint.getArgs())));
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
    // lấy HttpServletRequest hiện tại (nếu đang trong request)
    private Optional<jakarta.servlet.http.HttpServletRequest> currentRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest);
    }
    // JSON-hoá từng arg riêng -> 1 arg lỗi (vd MultipartFile) không làm hỏng cả dòng log.
    // Tham số có tên nhạy cảm (vd @RequestParam password) bị che nguyên giá trị.
    private String argsToJson(String[] names, Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        return IntStream.range(0, args.length)
                .mapToObj(i -> names != null && i < names.length && isSensitive(names[i])
                        ? "\"" + MASK + "\""
                        : toJson(args[i]))
                .collect(Collectors.joining(", ", "[", "]"));
    }

    /** JSON-hoá an toàn: không serialize được -> dùng {@code String.valueOf}, không ném lỗi ra ngoài. */
    private String toJson(Object o) {
        if (o == null) {
            return "null";
        }
        String binary = describeBinary(o);
        if (binary == null) {
            return binary;
        }
        // Danh sách tệp tải lên (List<MultipartFile>) phải tóm tắt từng phần tử, không để Jackson đọc nội dung tệp.
        if (o instanceof Collection<?> items && items.stream().anyMatch(item -> describeBinary(item) != null)) {
            return items.stream().map(this::toJson).collect(Collectors.joining(", ", "[", "]"));
        }
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            return String.valueOf(o);
        }
    }



    // Tóm tắt tệp, luồng, mảng byte thành một dòng ngắn; trả null nếu không phải dữ liệu nhị phân.
    // Để Jackson duyệt các kiểu này thì nó gọi getContentAsByteArray()/getInputStream(): đọc trọn tệp vào RAM
    // và mở luồng không đóng, mỗi lần tải tệp hay tải lên đều dính. Tệp xuất trả ResponseEntity<byte[]>
    // thì Jackson mã base64 cả tệp chỉ để rồi bị cắt, nên cũng chỉ ghi số byte.
    private String describeBinary(Object o) {
        if (o instanceof ResponseEntity<?> response && response.getBody() instanceof Resource resource) {
            return "\"ResponseEntity[status=" + response.getStatusCode() + ", resource=" + resource.getFilename() + "]\"";
        }
        if (o instanceof ResponseEntity<?> response && response.getBody() instanceof byte[] bytes) {
            return "\"ResponseEntity[status=" + response.getStatusCode() + ", bytes=" + bytes.length + "]\"";
        }
        if (o instanceof Resource resource) {
            return "\"[Resource: " + resource.getFilename() + "]\"";
        }
        if (o instanceof MultipartFile file) {
            return "\"[MultipartFile: " + file.getOriginalFilename() + ", size=" + file.getSize() + "]\"";
        }
        if (o instanceof InputStream || o instanceof OutputStream) {
            return "\"[Stream]\"";
        }
        if (o instanceof byte[] bytes) {
            return "\"[bytes: " + bytes.length + "]\"";
        }
        return null;
    }


    // Duyệt đệ quy object và mảng, thay giá trị của thuộc tính có tên nhạy cảm bằng ***.
    private void maskSensitive(JsonNode node) {
        if (node instanceof ObjectNode objectNode) {
            for (String name : new ArrayList<>(objectNode.propertyNames())) {
                if (isSensitive(name)) {
                    objectNode.put(name, MASK);
                } else {
                    maskSensitive(objectNode.get(name));
                }
            }
        } else if (node != null && node.isArray()) {
            for (JsonNode child : node) {
                maskSensitive(child);
            }
        }
    }


    // Tham số có tên chứa từ khoá nhạy cảm (mật khẩu, token...) thì không ghi giá trị ra log.
    private boolean isSensitive(String name) {
        if (name == null) {
            return false;
        }
        String lower = name.toLowerCase(Locale.ROOT);
        return SENSITIVE_KEYS.stream().anyMatch(lower::contains);
    }

    // cắt bớt nếu vượt giới hạn -> tránh log khổng lồ
    private String truncate(String s) {
        if (s == null || s.length() <= maxPayloadLength) {
            return s;
        }
        return s.substring(0, maxPayloadLength) + "...(" + s.length() + " ký tự, đã cắt)";
    }
}
