package com.example.java_template.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Cấu hình Jackson logic-level: trim chuỗi (+ "" -> null) và format ngày giờ cố định.
 *
 * <p>Cấu hình khác (null inclusion, timezone, fail-on-unknown) đặt trong {@code application.yaml}
 * dưới khối {@code spring.jackson.*}.</p>
 */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Spring Boot tự nạp mọi bean {@link JacksonModule} vào {@code ObjectMapper}. */
    @Bean
    JacksonModule appJacksonModule() {
        SimpleModule module = new SimpleModule("app-jackson");

        // String: trim 2 đầu; "" và "   " -> null
        module.addDeserializer(String.class, new StdDeserializer<String>(String.class) {
            @Override
            public String deserialize(JsonParser p, DeserializationContext text) {
                String value = p.getValueAsString();
                if (value == null) {
                    return null;
                }
                value = value.trim();
                return value.isEmpty() ? null : value;
            }
        });

        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME));
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATE_TIME));

        module.addSerializer(LocalDate.class, new LocalDateSerializer(DATE));
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer(DATE));

        return module;
    }
}
