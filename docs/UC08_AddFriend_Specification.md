# UC08: Kết bạn (Add Friend)

## 1. Mô tả tổng quan

Chức năng cho phép người dùng tìm kiếm và kết bạn với người dùng khác trong hệ thống thông qua 2 phương thức:
- **Tìm kiếm theo tên**: Nhập tên hoặc username để tìm kiếm
- **Quét mã QR**: Quét mã QR của người dùng khác để lấy thông tin

## 2. Actors

- **Người dùng (User)**: Người muốn kết bạn với người khác

## 3. Preconditions (Điều kiện tiên quyết)

- Người dùng đã đăng nhập vào hệ thống
- Người dùng có quyền truy cập camera (nếu dùng QR)
- Có kết nối internet

## 4. Postconditions (Điều kiện sau)

- Lời mời kết bạn được gửi thành công
- Người nhận được thông báo về lời mời kết bạn
- Trạng thái quan hệ được cập nhật trong database

## 5. Main Flow (Luồng chính)

### 5.1. Tìm kiếm theo tên

1. Người dùng mở màn hình "Tìm kiếm bạn bè"
2. Người dùng chọn tab "Tìm kiếm"
3. Người dùng nhập tên hoặc username vào ô tìm kiếm
4. Hệ thống gọi API `GET /api/friends/search?query={name}`
5. Hệ thống trả về danh sách người dùng phù hợp
6. Người dùng chọn một người từ danh sách
7. Hệ thống kiểm tra trạng thái quan hệ
8. Người dùng nhấn nút "Kết bạn"
9. Hệ thống gửi lời mời kết bạn
10. Hệ thống hiển thị "Đã gửi lời mời kết bạn"

### 5.2. Quét mã QR

1. Người dùng mở màn hình "Tìm kiếm bạn bè"
2. Người dùng chọn tab "Quét QR"
3. Hệ thống yêu cầu quyền truy cập camera
4. Người dùng cho phép truy cập camera
5. Hệ thống mở camera để quét QR
6. Người dùng quét mã QR của người dùng khác
7. Hệ thống giải mã QR lấy userId
8. Hệ thống gọi API `GET /api/friends/user/{userId}`
9. Hệ thống hiển thị thông tin người dùng
10. Người dùng nhấn nút "Kết bạn"
11. Hệ thống gửi lời mời kết bạn
12. Hệ thống hiển thị "Đã gửi lời mời kết bạn"

## 6. Alternative Flows (Luồng thay thế)

### 6.1. Không tìm thấy người dùng

- **Tại bước 5**: Nếu không tìm thấy người dùng nào
  - Hệ thống hiển thị "Không tìm thấy người dùng"
  - Quay lại bước 3

### 6.2. Quét QR thất bại

- **Tại bước 6**: Nếu không quét được mã QR
  - Hệ thống hiển thị "Không thể quét mã QR"
  - Người dùng có thể thử lại hoặc chọn phương thức khác

### 6.3. Đã là bạn bè

- **Tại bước 7**: Nếu đã là bạn bè
  - Hệ thống hiển thị "Đã là bạn bè"
  - Hiển thị nút "Nhắn tin" hoặc "Xem trang cá nhân"

### 6.4. Đã gửi lời mời

- **Tại bước 7**: Nếu đã gửi lời mời trước đó
  - Hệ thống hiển thị "Đã gửi lời mời"
  - Hiển thị nút "Hủy lời mời"

### 6.5. Đã nhận lời mời

- **Tại bước 7**: Nếu đã nhận lời mời từ người này
  - Hệ thống hiển thị "Đã nhận lời mời kết bạn"
  - Hiển thị nút "Chấp nhận" và "Từ chối"

## 7. Exception Flows (Luồng ngoại lệ)

### 7.1. Lỗi kết nối

- **Tại bất kỳ bước nào**: Nếu mất kết nối internet
  - Hệ thống hiển thị "Lỗi kết nối. Vui lòng thử lại"
  - Người dùng có thể thử lại

### 7.2. Lỗi server

- **Tại bước 4, 8, 9**: Nếu server trả về lỗi
  - Hệ thống hiển thị thông báo lỗi cụ thể
  - Người dùng có thể thử lại

### 7.3. Không có quyền camera

- **Tại bước 4**: Nếu người dùng từ chối quyền camera
  - Hệ thống hiển thị "Cần quyền truy cập camera để quét QR"
  - Hướng dẫn người dùng cấp quyền trong settings

## 8. Business Rules (Quy tắc nghiệp vụ)

1. **Không thể tự kết bạn với chính mình**
2. **Không thể gửi nhiều lời mời cho cùng một người**
3. **Lời mời kết bạn có thời hạn 30 ngày**
4. **Sau 30 ngày, lời mời tự động hết hạn**
5. **Người dùng có thể hủy lời mời đã gửi**
6. **Người nhận có thể chấp nhận hoặc từ chối lời mời**
7. **Khi chấp nhận, cả 2 người trở thành bạn bè**
8. **Khi từ chối, lời mời bị xóa**

## 9. API Endpoints

### 9.1. Tìm kiếm người dùng

```http
GET /api/friends/search?query={name}
Authorization: Bearer {token}

Response 200 OK:
{
  "success": true,
  "message": "Tìm thấy 5 người dùng",
  "data": [
    {
      "id": 123,
      "username": "john_doe",
      "name": "John Doe",
      "avatarUrl": "/uploads/avatars/123.jpg",
      "friendshipStatus": "NONE" // NONE, PENDING_SENT, PENDING_RECEIVED, FRIENDS
    }
  ]
}
```

### 9.2. Lấy thông tin người dùng theo ID

```http
GET /api/friends/user/{userId}
Authorization: Bearer {token}

Response 200 OK:
{
  "success": true,
  "data": {
    "id": 123,
    "username": "john_doe",
    "name": "John Doe",
    "avatarUrl": "/uploads/avatars/123.jpg",
    "friendshipStatus": "NONE"
  }
}
```

### 9.3. Kiểm tra trạng thái quan hệ

```http
GET /api/friends/status/{targetUserId}
Authorization: Bearer {token}

Response 200 OK:
{
  "success": true,
  "data": {
    "status": "NONE", // NONE, PENDING_SENT, PENDING_RECEIVED, FRIENDS
    "requestId": null // ID của lời mời nếu có
  }
}
```

### 9.4. Gửi lời mời kết bạn

```http
POST /api/friends/request
Authorization: Bearer {token}
Content-Type: application/json

{
  "receiverId": 123
}

Response 200 OK:
{
  "success": true,
  "message": "Đã gửi lời mời kết bạn",
  "data": {
    "requestId": 456,
    "status": "PENDING"
  }
}

Response 400 Bad Request:
{
  "success": false,
  "message": "Đã gửi lời mời trước đó"
}
```

### 9.5. Chấp nhận lời mời

```http
POST /api/friends/accept/{requestId}
Authorization: Bearer {token}

Response 200 OK:
{
  "success": true,
  "message": "Đã chấp nhận lời mời kết bạn"
}
```

### 9.6. Từ chối lời mời

```http
POST /api/friends/reject/{requestId}
Authorization: Bearer {token}

Response 200 OK:
{
  "success": true,
  "message": "Đã từ chối lời mời kết bạn"
}
```

### 9.7. Hủy lời mời đã gửi

```http
DELETE /api/friends/request/{requestId}
Authorization: Bearer {token}

Response 200 OK:
{
  "success": true,
  "message": "Đã hủy lời mời kết bạn"
}
```

### 9.8. Xem danh sách bạn bè

```http
GET /api/friends
Authorization: Bearer {token}

Response 200 OK:
{
  "success": true,
  "data": [
    {
      "id": 123,
      "username": "john_doe",
      "name": "John Doe",
      "avatarUrl": "/uploads/avatars/123.jpg",
      "friendsSince": "2024-01-15T10:30:00Z"
    }
  ]
}
```

### 9.9. Hủy kết bạn

```http
DELETE /api/friends/{friendId}
Authorization: Bearer {token}

Response 200 OK:
{
  "success": true,
  "message": "Đã hủy kết bạn"
}
```

## 10. Database Schema

### 10.1. Table: friend_requests

```sql
CREATE TABLE friend_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, ACCEPTED, REJECTED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expires_at TIMESTAMP, -- Hết hạn sau 30 ngày
    
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    
    UNIQUE KEY unique_request (sender_id, receiver_id),
    INDEX idx_receiver (receiver_id),
    INDEX idx_status (status),
    INDEX idx_expires (expires_at)
);
```

### 10.2. Table: friendships

```sql
CREATE TABLE friendships (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id_1 BIGINT NOT NULL,
    user_id_2 BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id_1) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id_2) REFERENCES users(id) ON DELETE CASCADE,
    
    UNIQUE KEY unique_friendship (user_id_1, user_id_2),
    INDEX idx_user1 (user_id_1),
    INDEX idx_user2 (user_id_2)
);
```

## 11. QR Code Format

Mã QR chứa thông tin người dùng dưới dạng JSON:

```json
{
  "type": "mapic_user",
  "userId": 123,
  "username": "john_doe",
  "timestamp": 1704067200000
}
```

Hoặc đơn giản chỉ chứa userId:
```
mapic://user/123
```

## 12. UI/UX Requirements

### 12.1. Màn hình tìm kiếm

- Tab "Tìm kiếm" và "Quét QR"
- Search bar với placeholder "Tìm kiếm theo tên hoặc username"
- Danh sách kết quả với avatar, tên, username
- Trạng thái quan hệ hiển thị rõ ràng

### 12.2. Màn hình quét QR

- Camera view toàn màn hình
- Khung quét QR ở giữa
- Hướng dẫn "Đưa mã QR vào khung"
- Nút đóng ở góc trên

### 12.3. Trạng thái nút

- **Chưa có quan hệ**: Nút "Kết bạn" (màu xanh)
- **Đã gửi lời mời**: Nút "Đã gửi" (màu xám, disabled)
- **Đã nhận lời mời**: Nút "Chấp nhận" (màu xanh)
- **Đã là bạn bè**: Nút "Nhắn tin" (màu xanh)

## 13. Notifications

Khi gửi lời mời kết bạn, người nhận sẽ nhận được thông báo:

```
{
  "type": "FRIEND_REQUEST",
  "title": "Lời mời kết bạn",
  "message": "{senderName} đã gửi lời mời kết bạn",
  "data": {
    "requestId": 456,
    "senderId": 123,
    "senderName": "John Doe",
    "senderAvatar": "/uploads/avatars/123.jpg"
  }
}
```

## 14. Testing Scenarios

### 14.1. Test Cases

1. **TC01**: Tìm kiếm người dùng theo tên - thành công
2. **TC02**: Tìm kiếm người dùng - không tìm thấy
3. **TC03**: Quét QR code - thành công
4. **TC04**: Quét QR code - thất bại
5. **TC05**: Gửi lời mời kết bạn - thành công
6. **TC06**: Gửi lời mời - đã gửi trước đó
7. **TC07**: Gửi lời mời - đã là bạn bè
8. **TC08**: Chấp nhận lời mời - thành công
9. **TC09**: Từ chối lời mời - thành công
10. **TC10**: Hủy lời mời đã gửi - thành công

## 15. Performance Requirements

- Tìm kiếm phải trả về kết quả trong < 500ms
- Quét QR phải nhận diện trong < 1s
- Gửi lời mời phải hoàn thành trong < 1s
- Hỗ trợ tìm kiếm với > 10,000 người dùng

## 16. Security Requirements

- Validate userId từ QR code
- Không cho phép SQL injection trong search query
- Rate limiting: Tối đa 10 lời mời/phút
- Không cho phép spam lời mời kết bạn
