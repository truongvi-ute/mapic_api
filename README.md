# MAPIC Backend

Backend API cho ứng dụng MAPIC - Mạng xã hội dựa trên vị trí.

## Công nghệ sử dụng

- Java 17
- Spring Boot 3.x
- Spring Security (JWT Authentication)
- Spring Data JPA
- PostgreSQL
- Maven

## Yêu cầu

- JDK 17 trở lên
- Maven 3.6+
- PostgreSQL 14+

## Cài đặt

1. Clone repository:
```bash
git clone <repository-url>
cd backend
```

2. Cấu hình database trong `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mapic
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. Build project:
```bash
./mvnw clean install
```

4. Chạy ứng dụng:
```bash
./mvnw spring-boot:run
```

Server sẽ chạy tại `http://localhost:8080`

## API Documentation

### Authentication
- `POST /api/auth/register` - Đăng ký tài khoản
- `POST /api/auth/verify-registration` - Xác thực OTP đăng ký
- `POST /api/auth/login` - Đăng nhập
- `POST /api/auth/forgot-password` - Quên mật khẩu
- `POST /api/auth/reset-password` - Đặt lại mật khẩu
- `POST /api/auth/change-password` - Đổi mật khẩu
- `POST /api/auth/resend-otp` - Gửi lại OTP

### User
- `GET /api/user/profile` - Lấy thông tin profile
- `PUT /api/user/update-profile` - Cập nhật profile
- `POST /api/user/upload-avatar` - Upload ảnh đại diện
- `POST /api/user/upload-cover` - Upload ảnh bìa

### Moments
- `POST /api/moments` - Tạo khoảnh khắc mới
- `GET /api/moments/my-moments` - Lấy danh sách khoảnh khắc của tôi

## Cấu trúc thư mục

```
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/mapic/backend/
│   │   │       ├── config/         # Cấu hình
│   │   │       ├── controller/     # REST Controllers
│   │   │       ├── dto/            # Data Transfer Objects
│   │   │       ├── entity/         # JPA Entities
│   │   │       ├── repository/     # JPA Repositories
│   │   │       ├── service/        # Business Logic
│   │   │       └── security/       # Security Configuration
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   └── test/
├── uploads/                        # User uploaded files (gitignored)
├── pom.xml
└── README.md
```

## License

Private project
