# Hướng dẫn xác thực JWT demo

Backend tự xác minh tài khoản mẫu và phát hành JWT mà không cần database.

## Tài khoản demo

| Username | Password | Quyền |
| --- | --- | --- |
| admin | admin123 | ADMIN, USER |
| user | user123 | USER |

Tài khoản được lưu trong bộ nhớ và khóa ký mặc định chỉ dùng cho profile dev.

## Chạy backend

~~~bash
./mvnw spring-boot:run
~~~

Profile dev mặc định bật xác thực JWT. Khi chạy ngoài local, đặt biến môi trường JWT_SECRET_BASE64 bằng khóa Base64 riêng.

## Lấy access token

~~~bash
curl -X POST 'http://localhost:8080/admin/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'
~~~

Token nằm trong trường data.accessToken của response và có thời hạn mặc định 15 phút.

## Gọi API cần xác thực

~~~bash
curl 'http://localhost:8080/admin/api/auth/me' \
  -H 'Authorization: Bearer <accessToken>'
~~~

Đường dẫn có tiền tố /admin vì đó là context path mặc định của backend.
