# Hướng Dẫn Sử Dụng Keycloak SSO & Xác Thực Token Cho Dự Án Template

Tài liệu này dành cho tất cả thành viên trong team (Frontend, Backend, QA) khi mới pull project về máy để chạy và phát triển.

---

## 1. Tổng Quan Kiến Trúc Xác Thực (OIDC SSO)

Dự án sử dụng cơ chế **Xác thực Đăng nhập một lần (Single Sign-On - SSO)** dựa trên chuẩn quốc tế **OpenID Connect (OIDC) / OAuth 2.0 PKCE** thông qua **Keycloak**:

```
[ Trình duyệt / Angular MFE ]
       │
       ├── (1) Chưa đăng nhập ──> Chuyển hướng sang Keycloak SSO (cổng 8180)
       ├── (2) Đăng nhập thành công ──> Keycloak trả về Token (Access Token, ID Token)
       └── (3) Gọi API Backend ──> Tự động đính kèm Header: "Authorization: Bearer <token>"
                                            │
                                            ▼
                               [ Spring Boot Backend (cổng 8080) ]
                               (Xác thực chữ ký Token qua Keycloak JWKS)
```

- **Token được lưu ở đâu?** Lưu an toàn trong `SessionStorage` của trình duyệt dưới các khóa: `access_token`, `id_token`, `id_token_claims_obj`.
- **Token được gửi thế nào?** Thư viện `@platform/shared` (`authInterceptor`) **tự động gắn** header `Authorization: Bearer <access_token>` vào mọi HTTP request gửi tới Backend (`http://localhost:8080`), lập trình viên **không cần tự code gắn token thủ công**.

---

## 2. Khởi Động Keycloak (Chỉ 1 Lệnh Duy Nhất)

File cấu hình Docker Compose đã được đặt sẵn trong thư mục `Be_Template/Java_Template` với tính năng **tự động nạp sẵn Realm `platform` và Client `Auth`**.

### Bước thực hiện:
Mở terminal tại thư mục `Be_Template/Java_Template` và chạy:

```bash
cd Be_Template/Java_Template
DOCKER_HOST=unix:///var/run/docker.sock docker compose up -d
# (Hoặc lệnh thông thường nếu máy bạn không dùng custom sock: docker compose up -d)
```

### Kiểm tra dịch vụ:
- **Keycloak Web Admin Console**: [http://localhost:8180](http://localhost:8180)
  - Tài khoản Admin tối cao: `admin` / `admin`
- **Realm đã tạo sẵn**: `platform`
- **Client ID**: `Auth`

---

## 3. Danh Sách Tài Khoản Thử Nghiệm Có Sẵn

Bạn có thể dùng ngay 2 tài khoản test đã được nạp sẵn mà không cần tạo mới:

| Tài khoản (Username) | Mật khẩu (Password) | Quyền hạn (Roles) | Mục đích sử dụng |
| :--- | :--- | :--- | :--- |
| **`admin`** | `admin123` | `ADMIN`, `USER` | Kiểm tra tính năng quản trị viên, full quyền |
| **`user`** | `user123` | `USER` | Kiểm tra tính năng người dùng thông thường |

---

## 4. Hướng Dẫn Dành Cho Frontend Developer (`Angular_Template`)

### 4.1. Khởi động dự án Frontend
```bash
cd Fe_Template/Angular_Template
npm install
npm start
```
Ứng dụng chạy tại: **`http://localhost:4200`**

### 4.2. Luồng đăng nhập & Kiểm tra Token
1. Truy cập: `http://localhost:4200/login`
2. Bấm nút **"Đăng nhập với SSO Keycloak"**.
3. Trình duyệt tự động chuyển sang trang đăng nhập Keycloak. Nhập `admin` / `admin123`.
4. Sau khi đăng nhập, hệ thống tự động chuyển về `http://localhost:4200/template`.
5. **Xem Token trực quan:**
   - Ngay trên trang `/template`, khối **"Kiểm Tra Token OIDC & Header Authorization"** sẽ hiển thị đầy đủ chuỗi JWT Access Token, User Claims và nút **"Sao chép Token"**.
   - Bấm nút **"Gửi Request kiểm tra Header"** để thấy thực tế Header `Authorization: Bearer ...` được gửi đi.
   - Hoặc mở **F12 > Application > Storage > Session Storage > http://localhost:4200** để xem token thô.

### 4.3. Cách lấy Token và Thông tin User trong Code Component
```typescript
import { AuthService } from '@core'; // hoặc import từ '@platform/shared'

export class MyFeatureComponent {
  private readonly authService = inject(AuthService);

  // 1. Kiểm tra trạng thái đăng nhập (trả về boolean)
  isLoggedIn = this.authService.isAuthenticated();

  // 2. Lấy chuỗi Access Token (JWT string)
  token = this.authService.getToken();

  // 3. Lấy thông tin người dùng (Claims giải mã từ token)
  userInfo = this.authService.claims; // { preferred_username, email, roles, sub... }

  // 4. Đăng xuất sạch (xóa session trên Keycloak và local)
  logout() {
    this.authService.logout();
  }
}
```

---

## 5. Hướng Dẫn Dành Cho Backend Developer (`Java_Template`)

Backend đóng vai trò là **OAuth2 Resource Server**, xác thực token bằng Public Key từ Keycloak.

### 5.1. Cấu hình `application.yaml`
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8180/realms/platform
          jwk-set-uri: http://localhost:8180/realms/platform/protocol/openid-connect/certs
```

### 5.2. Lấy thông tin User trong Controller / Service Spring Boot
```java
@RestController
@RequestMapping("/api/example")
public class ExampleController {

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");
        String userId = jwt.getSubject(); // Keycloak UUID

        return ResponseEntity.ok(Map.of(
            "username", username,
            "email", email,
            "userId", userId
        ));
    }
}
```

---

## 6. Xử Lý Các Sự Cố Thường Gặp (Troubleshooting)

### Q1: Bị báo lỗi cổng đã được sử dụng (Port 8180 hoặc 6379)?
- Kiểm tra tiến trình đang chiếm cổng:
  ```bash
  sudo lsof -i :8180
  sudo lsof -i :6379
  ```
- Hoặc dừng container cũ đang chiếm cổng:
  ```bash
  DOCKER_HOST=unix:///var/run/docker.sock docker ps
  DOCKER_HOST=unix:///var/run/docker.sock docker stop <container_id>
  ```

### Q2: Muốn reset toàn bộ dữ liệu Keycloak về trạng thái ban đầu?
Chạy lệnh sau để xóa database Keycloak cũ và nạp lại từ file `realm-export.json`:
```bash
cd Be_Template/Java_Template
DOCKER_HOST=unix:///var/run/docker.sock docker compose down -v
DOCKER_HOST=unix:///var/run/docker.sock docker compose up -d
```

### Q3: Sau khi đăng nhập Keycloak báo lỗi "Invalid parameter: redirect_uri"?
- Kiểm tra lại Client `Auth` trên Keycloak Admin xem `Valid redirect URIs` đã có `http://localhost:4200/*` chưa (file `realm-export.json` chuẩn đã có sẵn).
