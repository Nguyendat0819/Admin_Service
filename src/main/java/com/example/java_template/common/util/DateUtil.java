package com.example.java_template.common.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Tiện ích format/parse ngày giờ.
 * <p>Múi giờ mặc định: {@code Asia/Ho_Chi_Minh} — trùng cấu hình trong {@code application.yaml}.</p>
 */
public final class DateUtil {

    public static final String DAY_MO_YEAR = "dd/MM/yyyy";

    private DateUtil() {
    }

    public static String formatLocalDate(Date date, String format) {
        String formatDate = "";
        if (date != null && Strings.isNotBlank(format)) {
            formatDate = new SimpleDateFormat(format).format(date);
        }
        return formatDate;
    }

    public static String formatLocalDateTime(LocalDateTime date, String format) {
        if (date == null) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return date.format(formatter);
    }

    public static LocalDateTime getStartOfDay(LocalDateTime localDateTime) {
        return LocalDateTime.of(localDateTime.toLocalDate(), LocalTime.of(0, 0, 0));
    }

    public static LocalDateTime getEndOfDay(LocalDateTime localDateTime) {
        return LocalDateTime.of(localDateTime.toLocalDate(), LocalTime.of(23, 59, 59));
    }

    /**
     * Parse tự động nhiều định dạng:
     * <ul>
     *     <li>ISO-8601: {@code 2025-12-23T17:00:00.000Z}</li>
     *     <li>{@code dd/MM/yyyy HH:mm}</li>
     *     <li>{@code dd/MM/yyyy} (kết quả đầu ngày)</li>
     *     <li>{@code yyyy-MM-dd HH:mm:ss} (chuẩn DB)</li>
     * </ul>
     * Trả về {@code null} nếu parse lỗi.
     */
    public static LocalDateTime parseToLocalDateTime(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            // ISO-8601 (2025-12-23T17:00:00.000Z)
            if (value.contains("T")) {
                return Instant.parse(value)
                        .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                        .toLocalDateTime();
            }
            // dd/MM/yyyy HH:mm
            if (value.matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}")) {
                return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            }
            // dd/MM/yyyy
            if (value.matches("\\d{2}/\\d{2}/\\d{4}")) {
                return LocalDate.parse(value, DateTimeFormatter.ofPattern(DAY_MO_YEAR)).atStartOfDay();
            }
            // yyyy-MM-dd HH:mm:ss (DB)
            return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            return null;
        }
    }
}
