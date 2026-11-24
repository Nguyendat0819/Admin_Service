# Quy ước code — Java_Template

## 1. Mục đích và phạm vi

Tài liệu này quy định cách tổ chức, viết, kiểm tra và bảo trì mã nguồn trong Java_Template. Áp dụng cho mã Java, REST API, JPA, cấu hình, log và kiểm thử.

Project hiện dùng Java 17, Spring Boot, Maven Wrapper và cấu trúc package gốc `com.example.java_template`. Package gốc hiện có dấu gạch dưới để giữ tương thích với mã nguồn đang có; package mới bên dưới dùng chữ thường, không gạch dưới. Nếu cần đổi package gốc, thực hiện thành một thay đổi riêng để cập nhật đồng bộ mã nguồn và cấu hình.

Tên trong mã nguồn luôn dùng tiếng Anh. Comment, log message và nội dung hướng đến người dùng dùng tiếng Việt. Không đưa quy định đặc thù của OFFICE_SERVICE vào template này.

## 2. Cấu trúc project

Mã nguồn nằm trong `src/main/java/com/example/java_template`. Các package hiện có:

~~~text
com.example.java_template/
  JavaTemplateApplication.java
  common/
    aop/                 AOP dùng chung
    client/              Client gọi dịch vụ ngoài và DTO dùng chung
    config/              Cấu hình Spring
    exception/           Exception và xử lý lỗi tập trung
    httpclient/          Cấu hình HTTP client dùng chung
    observability/       Log, trace và thông tin theo dõi request
    persistence/         JPA auditing và hỗ trợ truy cập dữ liệu
      dto/               BaseEntity và DTO persistence dùng chung hiện tại
    response/            ApiResponse, ApiResponseFactory, mã lỗi, phân trang
    security/            Cấu hình và thành phần xác thực/phân quyền
    service/             Dịch vụ dùng chung thực sự giữa nhiều feature
    util/                Hàm tiện ích thuần, không chứa nghiệp vụ
    validator/           Validator dùng chung
  feature/
    constant/            Hằng số và mã nghiệp vụ của feature
    controller/
      api/               Interface khai báo endpoint
    entity/              JPA entity của feature
    mapper/              Tạo khi feature cần ánh xạ DTO/entity
    model/
      dto/                DTO nội bộ của feature
      request/            Dữ liệu đầu vào API
      response/           Dữ liệu trả ra API
    repository/
      custom/             Interface truy vấn tùy chỉnh
        impl/             Cài đặt truy vấn tùy chỉnh
    service/               Interface nghiệp vụ
      impl/                Cài đặt nghiệp vụ
~~~

Quy tắc tổ chức:

- Giữ cách chia package hiện tại: thành phần dùng chung ở common, mã nghiệp vụ ở feature.
- Controller chỉ nhận HTTP request, áp dụng validation/quyền và gọi service; không đặt luật nghiệp vụ trong controller.
- Service thực hiện nghiệp vụ và điều phối các lần gọi repository/client.
- Repository chỉ truy vấn và ghi dữ liệu; không chứa luật nghiệp vụ.
- Entity mô tả dữ liệu persistence. Không trả entity trực tiếp ra API.
- Request, response và DTO đặt trong model tương ứng; không khai báo DTO lồng trong service/controller.
- Mapper đặt trong feature/mapper khi cần chuyển đổi lặp lại giữa entity và DTO. Project có MapStruct; dùng mapper thay vì rải logic ánh xạ ở nhiều controller/service.
- common chỉ chứa thành phần được nhiều feature dùng và không phụ thuộc ngược vào feature. Không chuyển một lớp nghiệp vụ riêng vào common chỉ để tiện import.

## 3. Quy tắc đặt tên

- Package: chữ thường, không gạch dưới; ví dụ: `com.example.java_template.feature.order`.
- Class, interface, enum: PascalCase, dùng danh từ; ví dụ: `OrderService`, `CreateOrderRequest`, `OrderStatus`.
- Method và biến: camelCase; method bắt đầu bằng động từ, ví dụ: `findOrderByCode`, `createOrder`.
- Boolean: dùng tiền tố dễ hiểu như `is`, `has`, `can`, ví dụ: `isActive`, `hasPermission`.
- Hằng số: UPPER_SNAKE_CASE, ví dụ: `MAX_PAGE_SIZE`.
- Hậu tố theo vai trò: `Controller`, `Service`, `ServiceImpl`, `Repository`, `Request`, `Response`, `Dto`, `Mapper`, `Config`, `Exception`.
- Entity dùng tên số ít; bảng cơ sở dữ liệu dùng snake_case.
- REST endpoint dùng danh từ số nhiều và kebab-case, ví dụ: `/api/v1/order-items`.
- Không viết tắt tùy tiện. Dùng từ nghiệp vụ phổ biến và thống nhất trong toàn project.

## 4. Định dạng Java

- Dùng 4 dấu cách để thụt lề; không dùng tab.
- Giới hạn dòng ở khoảng 120 ký tự; tách biểu thức dài theo cấu trúc dễ đọc.
- Dùng dấu ngoặc nhọn kiểu K&R và mỗi file chỉ có một top-level class.
- Sắp xếp import theo nhóm package, không dùng wildcard import.
- Xóa import, biến, tham số và dependency không dùng.
- Dùng Lombok theo mẫu hiện có khi giúp giảm boilerplate; không để Lombok che khuất luồng xử lý hoặc quy tắc nghiệp vụ.

## 5. Comment, ngôn ngữ và các bước trong hàm

- Mỗi hàm tự viết có một dòng comment tiếng Việt ngay phía trên, mô tả hàm đang làm gì theo hành vi hiện tại.
- Comment phải đúng với mã đang chạy; không ghi lịch sử thay đổi hoặc mô tả sai hành vi.
- Với mỗi hàm có xử lý, ghi các bước thực hiện bằng comment tiếng Việt: Bước 1, Bước 2, Bước 3... Số thứ tự bắt đầu lại từ Bước 1 trong từng hàm và tăng theo đúng thứ tự chạy.
- Không tạo bước giả cho thân hàm rỗng hoặc hàm chỉ chuyển tiếp đơn giản. Với hàm ngắn một dòng, comment phía trên vẫn phải nêu rõ mục đích.
- Trong thân hàm, chỉ thêm giải thích cho đoạn khó hiểu; tập trung giải thích vì sao phải làm theo cách đó, không lặp lại câu lệnh đang hiển thị.
- Mỗi trường của request/DTO có một dòng comment tiếng Việt nêu ý nghĩa nghiệp vụ.
- Không viết khối Javadoc dài. Chỉ dùng Javadoc ngắn khi cần tài liệu hóa API/thư viện công khai hoặc quy ước công cụ bắt buộc.
- Tên package, class, method, biến và trường luôn bằng tiếng Anh. Comment, log message và chuỗi hiển thị cho người dùng bằng tiếng Việt.
- Mã mẫu kiến trúc chưa có nơi gọi thật phải ghi rõ “ví dụ mẫu kiến trúc” trong comment để tránh bị hiểu nhầm là luồng đang chạy.

Ví dụ method:

~~~java
// Tạo đơn hàng sau khi xác nhận sản phẩm và số lượng hợp lệ.
public OrderResponse createOrder(CreateOrderRequest request) {
    // Bước 1: Kiểm tra dữ liệu đầu vào theo quy tắc nghiệp vụ.
    validateRequest(request);

    // Bước 2: Đọc dữ liệu cần thiết theo lô để tránh truy vấn trong vòng lặp.
    List<Product> products = productRepository.findAllByCodeIn(request.productCodes());

    // Bước 3: Lưu đơn hàng và trả dữ liệu phản hồi.
    Order order = orderRepository.save(orderMapper.toEntity(request, products));
    return orderMapper.toResponse(order);
}
~~~

Ví dụ request:

~~~java
public record CreateOrderRequest(
        // Mã sản phẩm khách hàng chọn.
        @NotBlank String productCode,

        // Số lượng khách hàng muốn mua.
        @Positive int quantity
) {
}
~~~

## 6. Hiệu suất và gọi dịch vụ ngoài

- Không truy vấn/ghi database trong vòng lặp từng phần tử. Không gọi API bên ngoài một lần cho mỗi phần tử trong vòng lặp.
- Trước khi xử lý danh sách, gom ID/mã cần thiết rồi truy vấn theo lô; ánh xạ và xử lý kết quả trong bộ nhớ khi phù hợp.
- Khi ghi nhiều bản ghi, ưu tiên thao tác theo lô hoặc cơ chế batch phù hợp với database/ORM. Không mặc định rằng saveAll luôn tạo một câu SQL duy nhất; kiểm tra cấu hình và SQL thực tế khi hiệu suất quan trọng.
- Với API ngoài, ưu tiên endpoint nhận danh sách hoặc xử lý theo lô. Nếu bắt buộc gọi riêng từng mục, giới hạn số lượng đồng thời, đặt timeout, xử lý lỗi theo từng mục và tránh retry thao tác không idempotent.
- Không dùng parallel stream không giới hạn để gọi database hoặc dịch vụ ngoài.
- Tránh N+1 bằng projection, truy vấn join phù hợp hoặc EntityGraph. Dùng phân trang cho danh sách lớn.
- Không tải toàn bộ bảng nếu chỉ cần một trang hoặc một số trường. Chỉ chọn dữ liệu cần dùng.
- Nếu chưa thể tránh vòng lặp có I/O, ghi rõ lý do, giới hạn dữ liệu, đo thời gian và để comment giải thích vì sao cần cách làm đó.

## 7. Spring Boot, dependency injection và cấu hình

- Dùng constructor injection. Với Lombok, ưu tiên `@RequiredArgsConstructor` và dependency `final`; không dùng `@Autowired` trên field.
- Đặt `@Transactional` ở service nơi bắt đầu/điều phối nghiệp vụ, không đặt ở controller.
- Với hàm chỉ đọc, dùng `@Transactional(readOnly = true)` ở mức hàm. Tránh đặt readOnly ở mức lớp nếu lớp đó có cả hàm ghi.
- Cấu hình theo profile hiện có: `application-local.yaml`, `application-prod.yaml`; test dùng cấu hình riêng trong `src/test/resources`.
- Gom cấu hình có kiểu dữ liệu vào `@ConfigurationProperties`; tránh rải `@Value` trong nhiều lớp.
- Không hardcode secret, mật khẩu, token, private key hoặc thông tin production. Đọc từ biến môi trường hoặc secret manager.
- Không ghi secret thật vào YAML, Git, log hoặc exception.
- Dùng Bean Validation ở request boundary với `@Valid` và annotation phù hợp như `@NotBlank`, `@NotNull`, `@Size`, `@Positive`. Quy tắc phụ thuộc nhiều trường dùng validator riêng.

## 8. REST API và phản hồi

- Dùng đúng HTTP method: GET để đọc, POST để tạo/thực hiện lệnh, PUT để thay thế, PATCH để cập nhật một phần, DELETE để xóa.
- Dùng status code phù hợp: 200, 201, 204, 400, 401, 403, 404, 409 và 500.
- Endpoint mới dùng version và danh từ số nhiều, ví dụ: `/api/v1/orders`, `/api/v1/orders/{orderCode}`.
- Giữ định dạng phản hồi thống nhất với `common.response.ApiResponse` và tạo response bằng `ApiResponseFactory`. Không tự tạo envelope khác nếu chưa thống nhất.
- Dùng request/response DTO; không serialize entity JPA trực tiếp.
- Danh sách lớn phải có phân trang và quy ước sort/filter thống nhất. Giới hạn page size tối đa.
- API lỗi trả mã và thông điệp phù hợp; không gửi stack trace, SQL, tên bảng hoặc thông tin nội bộ cho client.
- Kiểm tra tương thích ngược khi đổi endpoint, response hoặc mã lỗi đang được client sử dụng.
- Endpoint mẫu hiện có trong template chỉ để kiểm tra khởi động; endpoint nghiệp vụ mới phải theo quy ước version ở trên.

## 9. Exception và logging

- Ném exception nghiệp vụ rõ nghĩa; dùng các exception/mã lỗi hiện có trong common trước khi tạo loại mới.
- Xử lý lỗi HTTP tập trung qua `GlobalExceptionHandler` hoặc handler chung hiện có; không lặp logic chuyển exception thành response trong từng controller.
- Không nuốt exception, không dùng catch rỗng. Chỉ bắt exception khi có thể xử lý, bổ sung ngữ cảnh hoặc chuyển thành lỗi nghiệp vụ có nghĩa.
- Dùng SLF4J theo cấu hình Logback của project. Message log viết tiếng Việt, có ngữ cảnh và dùng placeholder: `log.info("Đã tạo đơn hàng, mã={}", orderCode)`.
- Dùng ERROR cho lỗi cần xử lý, WARN cho tình huống bất thường nhưng có thể tiếp tục, INFO cho sự kiện nghiệp vụ quan trọng và DEBUG cho chi tiết hỗ trợ điều tra.
- Không log mật khẩu, token, dữ liệu thẻ, secret hoặc dữ liệu cá nhân không cần thiết. Không nối chuỗi để tạo log.

## 10. Database và JPA

- Entity đặt trong `feature.entity`, Repository trong `feature.repository`; bảng dùng snake_case và khai báo mapping rõ ràng.
- Kế thừa `common.persistence.dto.BaseEntity` nếu entity cần khóa tự tăng, audit và cờ xóa mềm theo mẫu project.
- Quan hệ JPA dùng LAZY khi phù hợp. Không dùng EAGER chỉ để khắc phục một màn hình; tối ưu bằng truy vấn rõ ràng.
- Chọn cascade theo vòng đời thật của dữ liệu; không đặt cascade remove rộng trên quan hệ có thể dùng chung.
- Dùng truy vấn có tham số, không ghép chuỗi input vào native SQL.
- Dùng migration có phiên bản qua Flyway hoặc Liquibase theo công cụ project thống nhất. Không dùng `ddl-auto=update` trên production.
- Xem xét index theo truy vấn thực tế; không tạo index cho mọi cột một cách máy móc.
- Tôn trọng xóa mềm qua `is_deleted` nếu bảng đang áp dụng quy ước này; không thay bằng xóa vật lý khi chưa rõ quy tắc nghiệp vụ.

## 11. Bảo mật

- Kiểm tra và chuẩn hóa dữ liệu đầu vào ở ranh giới API.
- Dùng Spring Security và kiểm tra quyền ở endpoint/service theo mô hình của project; không dựa vào việc ẩn nút trên giao diện.
- Truy vấn database phải parameterized; không ghép SQL từ input người dùng.
- Không trả dữ liệu nội bộ hoặc thông tin xác thực trong response.
- Không tự viết thuật toán lưu mật khẩu/token. Dùng thư viện và cấu hình bảo mật đã được project phê duyệt.
- Khi gọi dịch vụ ngoài, dùng HTTP client đã cấu hình timeout; không tin cậy nội dung response trước khi kiểm tra.

## 12. Kiểm thử

- Dùng JUnit 5 và Mockito cho unit test; dùng Spring Boot Test cho integration test khi cần kiểm tra wiring hoặc database.
- Tên test nêu hành vi, ví dụ: `shouldReturnNotFoundWhenOrderDoesNotExist`.
- Tổ chức test theo Given–When–Then: dữ liệu đầu vào, hành động, kết quả mong đợi.
- Kiểm tra cả trường hợp thành công, dữ liệu không hợp lệ, không tìm thấy, xung đột và lỗi phụ thuộc ngoài.
- Không mock chính lớp đang được kiểm thử; mock các dependency bên ngoài ranh giới của lớp.
- Không phụ thuộc vào thứ tự chạy test hoặc dữ liệu dùng chung có thể làm test khác thất bại.

## 13. Git và review

- Commit theo Conventional Commits: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`.
- Tên branch ngắn, tiếng Anh, theo dạng `feature/order-search`, `fix/login-lock`, `docs/code-convention`.
- Trước khi gửi review, kiểm tra format/import, bỏ mã debug, không commit secret và chạy các kiểm tra phù hợp với thay đổi.
- Review checklist: đúng cấu trúc package; controller không có nghiệp vụ; không I/O trong vòng lặp; không trả entity; validation và quyền đã có; lỗi/log không lộ dữ liệu nhạy cảm; test bao phủ hành vi quan trọng.
- Dùng Maven Wrapper trong project để chạy build/test, ví dụ: `./mvnw test`, `./mvnw package`; Windows dùng `mvnw.cmd`.

## 14. Khi muốn thay đổi convention

Nếu một thay đổi cần lệch quy ước, ghi rõ lý do trong mô tả pull request và giữ phạm vi lệch nhỏ nhất. Nếu quy ước hiện tại không còn phù hợp, cập nhật tài liệu này cùng thay đổi mã nguồn để tài liệu và project tiếp tục thống nhất.

