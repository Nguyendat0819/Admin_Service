package com.example.java_template;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JavaTemplateApplicationTests {

    @Test
    void contextLoads() {
        // Verify Spring context khởi tạo thành công (không lỗi bean)
        // Sau khi tích hợp Keycloak, thêm test với token hợp lệ cho endpoint /api/template/test
    }
}
