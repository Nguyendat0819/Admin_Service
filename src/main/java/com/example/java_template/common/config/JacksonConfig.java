package com.example.java_template.common.config;

import com.example.java_template.common.util.StringUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Cấu hình các quy tắc Jackson dùng chung cho dữ liệu JSON của ứng dụng.
 *
 * <p>Chuỗi được xử lý bởi {@code CleanStringDeserializer}; ngày giờ dùng format cố định.
 * Cấu hình khác (null inclusion, timezone, fail-on-unknown) đặt trong {@code application-local.yaml}
 * dưới khối {@code spring.jackson.*}.</p>
 */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Tạo module chứa các bộ chuyển đổi riêng của ứng dụng.
     *
     * <p>Spring Boot phát hiện bean {@link JacksonModule} này rồi thêm vào {@code ObjectMapper};
     * vì vậy mapper mặc định có thể dùng các quy tắc đọc và ghi JSON được khai báo bên dưới.</p>
     */
    @Bean
//  appJacksonModule() tạo module chứa bộ xử lý chuỗi và ngày giờ. Spring Boot gắn module này vào ObjectMapper mặc định.
    JacksonModule appJacksonModule() {
//  Chuỗi được đăng ký hai lần. Theo quy tắc của SimpleModule
        SimpleModule module = new SimpleModule("app-jackson");

        // Handler ban đầu trim String, nhưng sẽ bị đăng ký CLEAN bên dưới thay thế:
        // SimpleModule áp dụng handler được đăng ký sau cùng cho cùng một kiểu.
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
        // CLEAN là handler String cuối cùng; createContextual chọn RAW riêng cho tên có "password".
        module.addDeserializer(String.class, CleanStringDeserializer.CLEAN);

        // Serializer đổi Java -> JSON; deserializer đổi JSON -> Java theo cùng pattern
        // để dữ liệu xuất ra có thể được đọc lại theo đúng định dạng.
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME));
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATE_TIME));

        module.addSerializer(LocalDate.class, new LocalDateSerializer(DATE));
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer(DATE));

        return module;
    }

    // Chọn cách đọc String theo tên property; chuỗi rỗng được quy về null ở cuối deserialize().
    static final class CleanStringDeserializer extends StdDeserializer<String> {

        static final CleanStringDeserializer CLEAN = new CleanStringDeserializer(true);
        static final CleanStringDeserializer RAW = new CleanStringDeserializer(false);

        private final boolean clean;

        // Cờ này quyết định có đi vào nhánh chuẩn hóa hay không; RAW=false bỏ qua nhánh đó.
        private CleanStringDeserializer(boolean clean) {
            super(String.class);
            this.clean = clean;
        }

        // Jackson gọi hàm này theo từng property để chọn policy; property null dùng CLEAN mặc định.
        // Tên có "password" (không phân biệt hoa thường) nhận RAW để tránh sửa nội dung nhập.
        //createContextual() chọn chế độ theo tên trường: tên có chứa password thì dùng RAW; các trường khác dùng CLEAN
        @Override
        public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
            boolean password = property != null
                    && property.getName() != null
                    && property.getName().toLowerCase(Locale.ROOT).contains("password");
            return password ? RAW : CLEAN;
        }

        // Đọc một giá trị JSON thành String; object/mảng bị từ chối thay vì bị ép thành null.
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) {
            // Báo Jackson sai kiểu thay vì âm thầm ép object/mảng thành null.
            if (p.hasToken(JsonToken.START_OBJECT) || p.hasToken(JsonToken.START_ARRAY)) {
                return (String) ctxt.handleUnexpectedToken(String.class, p);
            }
            String value = p.getValueAsString();
            if (value == null) {
                return null;
            }
            if (clean) {
                // Bước chuẩn hóa dự định đặt ở đây hiện đang bị comment, nên CLEAN chưa trim hay đổi Unicode.
                value = StringUtil.stripInvisible(Normalizer.normalize(value, Normalizer.Form.NFC));
            }
            // Chỉ chuỗi rỗng thành null; chuỗi chỉ có khoảng trắng vẫn được giữ nguyên khi trim đang tắt.
            return value.isEmpty() ? null : value;
        }
    }
}
