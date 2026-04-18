# System Class Documentation - Authentication & Registration (Redis-based)

This document describes the core classes and their attributes required to implement the revised authentication flow (Save After OTP via Redis) while adhering to **SOLID** principles.

## 1. Data Transfer Objects (DTOs)

These classes carry data between the client and server.

### RegisterRequest
- `String username`: Tên đăng nhập.
- `String email`: Email người dùng.
- `String password`: Mật khẩu (chưa mã hóa).
- `String name`: Tên hiển thị.

### LoginRequest
- `String username`: Tên đăng nhập hoặc Email.
- `String password`: Mật khẩu.

### VerifyOtpRequest
- `String email`: Email cần xác thực.
- `String code`: Mã OTP 6 số.
- `OtpType type`: Loại xác thực (REGISTRATION, FORGOT_PASSWORD).

### ResetPasswordRequest
- `String email`: Email của người dùng.
- `String otp`: Mã xác thực.
- `String newPassword`: Mật khẩu mới.

---

## 2. Entities (Persistent Data)

### Account (Abstract Class)
*Lớp cơ sở cho mọi loại tài khoản (User, Admin, Moderator).*
- `Long id`: Primary key.
- `String username`: Tên đăng nhập (Unique).
- `String password`: Mật khẩu (Hashed).
- `AccountStatus status`: Trạng thái (ACTIVE, BLOCKED).
- `LocalDateTime createdAt`.
- `LocalDateTime updatedAt`.

### User (Extends Account)
- `String name`: Tên hiển thị.
- `String email`: Email (Unique).
- `String phoneNumber`: Số điện thoại.
- `Boolean isBlocked`: Trạng thái bị Admin khóa.
- `Integer failedLoginAttempts`: Số lần đăng nhập sai.
- `LocalDateTime lockoutUntil`: Thời gian hết hạn khóa đăng nhập.
- *(Lưu ý: Không còn isVerified vì tài khoản chỉ được tạo trong DB sau khi đã xác thực OTP).*

---

## 3. Services (Business Logic)

### IAuthService (Interface)
*Tuân thủ **Interface Segregation** và **Dependency Inversion**.*
- `register(RegisterRequest)`: Xử lý bước 1, lưu thông tin vào Redis và gửi OTP.
- `verifyRegistration(VerifyOtpRequest)`: Kiểm tra OTP, nếu đúng thì lấy data từ Redis lưu vào DB.
- `login(LoginRequest)`: Xác thực thông tin và cấp JWT.
- `resendOtp(ResendOtpRequest)`: Kiểm tra Redis và gửi lại mã mới.
- `forgotPassword(String email)`: Gửi OTP reset mật khẩu.
- `resetPassword(ResetPasswordRequest)`: Cập nhật mật khẩu mới.

### IOtpService (Interface)
*Đảm nhận **Single Responsibility**: Chỉ quản lý OTP và dữ liệu tạm thời.*
- `createRegistrationSession(String email, RegisterRequest data)`: Lưu data đăng ký vào Redis (TTL 15p).
- `getRegistrationSession(String email)`: Lấy data đăng ký từ Redis.
- `generateOtp(String email, OtpType type)`: Tạo mã 6 số, lưu vào Redis (TTL 5p).
- `validateOtp(String email, String code, OtpType type)`: So khớp mã trong Redis.
- `deleteOtp(String email, OtpType type)`: Xóa mã sau khi dùng.

### IEmailService (Interface)
*Tách biệt logic gửi thông báo.*
- `sendOtpEmail(String to, String code, OtpType type)`: Gửi mail chứa mã xác thực.

---

## 4. Redis Storage Schema

Dữ liệu được lưu trữ trong Redis theo định dạng Key-Value:

| Key Pattern | Value Type | TTL | Mô tả |
| :--- | :--- | :--- | :--- |
| `pending-reg:{email}` | JSON (RegisterRequest) | 15 min | Thông tin đăng ký chờ xác thực. |
| `otp:{type}:{email}` | String (6 digits) | 5 min | Mã OTP hiện tại. |
| `otp-attempts:{type}:{email}` | Integer | 5 min | (Tùy chọn) Đếm số lần nhập sai. |

---

## 5. Architectural Flow (SOLID Mapping)

1. **S (Single Responsibility):** 
   - `AuthService` không quan tâm OTP được lưu ở đâu (DB hay Redis), nó chỉ gọi `OtpService`.
   - `OtpService` không quan tâm logic đăng ký hay login, nó chỉ quản lý mã và phiên làm việc tạm thời.
2. **O (Open/Closed):** 
   - `IEmailService` có thể được mở rộng thành `ISmsService` mà không ảnh hưởng tới logic nghiệp vụ của `AuthService`.
3. **D (Dependency Inversion):** 
   - Controller phụ thuộc vào `IAuthService` (Interface), không phụ thuộc vào class thực thi cụ thể.
