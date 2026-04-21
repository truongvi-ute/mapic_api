# UC08 - Quản lý Bạn bè - Trạng thái Triển khai

## Tổng quan
Backend đã triển khai **ĐẦY ĐỦ** các chức năng theo yêu cầu UC08_AddFriend.

---

## So sánh Yêu cầu vs Triển khai

### ✅ 1. Tìm người để kết bạn

#### 1.1. Tìm kiếm theo tên/username
**Yêu cầu UC08:** Người dùng có thể tìm kiếm người khác theo tên hoặc username

**Triển khai:**
- ✅ Endpoint: `GET /api/friends/search?query={query}`
- ✅ Tìm kiếm theo cả name và username (case-insensitive)
- ✅ Yêu cầu tối thiểu 2 ký tự
- ✅ Loại trừ chính người dùng khỏi kết quả
- ✅ Trả về trạng thái quan hệ (FRIENDS, PENDING_SENT, PENDING_RECEIVED, NONE)

**Code:**
```java
// FriendController.java
@GetMapping("/search")
public ResponseEntity<ApiResponse<List<UserSearchResponse>>> searchUsers(
    @RequestParam String query,
    Authentication authentication)

// UserRepository.java
@Query("SELECT u FROM User u WHERE " +
       "LOWER(u.name) LIKE :query OR " +
       "LOWER(u.username) LIKE :query")
List<User> searchByNameOrUsername(@Param("query") String query);
```

#### 1.2. Tìm kiếm realtime
**Cải tiến:** Tìm kiếm realtime với debounce

**Triển khai Frontend:**
- ✅ Tự động tìm kiếm khi nhập >= 2 ký tự
- ✅ Debounce 500ms để tránh spam requests
- ✅ Hiển thị kết quả ngay lập tức
- ✅ Không cần nút "Tìm"

**Lưu ý:** Chức năng quét mã QR đã được loại bỏ hoàn toàn theo yêu cầu người dùng (2026-04-21)

---

### ✅ 2. Gửi lời mời kết bạn

**Yêu cầu UC08:** Sau khi tìm thấy người dùng, có thể gửi lời mời kết bạn

**Triển khai:**
- ✅ Endpoint: `POST /api/friends/request`
- ✅ Kiểm tra không thể gửi cho chính mình
- ✅ Kiểm tra đã là bạn bè chưa
- ✅ Kiểm tra đã gửi lời mời chưa
- ✅ Tự động set thời gian hết hạn (30 ngày)
- ✅ Trạng thái mặc định: PENDING

**Code:**
```java
@PostMapping("/request")
public ResponseEntity<ApiResponse<Void>> sendFriendRequest(
    @Valid @RequestBody SendFriendRequestDto dto,
    Authentication authentication)

// Entity
@Entity
@Table(name = "friend_requests", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"sender_id", "receiver_id"})
})
public class FriendRequest {
    // Tự động set expires_at = now + 30 days
}
```

---

### ✅ 3. Xem trang cá nhân

**Yêu cầu UC08:** Có thể xem trang cá nhân của người được tìm thấy

**Triển khai:**
- ✅ Endpoint: `GET /api/friends/user/{userId}`
- ✅ Trả về thông tin: id, username, name, avatarUrl
- ✅ Kèm theo trạng thái quan hệ

---

### ✅ 4. Xem danh sách bạn bè

**Yêu cầu UC08:** Người dùng có thể xem danh sách bạn bè của mình

**Triển khai:**
- ✅ Endpoint: `GET /api/friends`
- ✅ Trả về danh sách tất cả bạn bè
- ✅ Thông tin: id, username, name, avatarUrl, friendsSince

**Code:**
```java
@GetMapping
public ResponseEntity<ApiResponse<List<FriendResponse>>> getAllFriends(
    Authentication authentication)

// Bidirectional friendship
@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    @Query("SELECT f FROM Friendship f WHERE f.user1.id = :userId OR f.user2.id = :userId")
    List<Friendship> findAllFriendsByUserId(@Param("userId") Long userId);
}
```

---

### ✅ 5. Xem danh sách lời mời

**Yêu cầu UC08:** Người dùng có thể xem danh sách lời mời kết bạn nhận được

**Triển khai:**
- ✅ Endpoint: `GET /api/friends/requests/pending`
- ✅ Chỉ lấy lời mời chưa hết hạn
- ✅ Thông tin người gửi: id, name, username, avatarUrl, createdAt
- ✅ Endpoint đếm số lượng: `GET /api/friends/requests/pending/count`

**Code:**
```java
@GetMapping("/requests/pending")
public ResponseEntity<ApiResponse<List<FriendRequestResponse>>> getPendingRequests(
    Authentication authentication)

@GetMapping("/requests/pending/count")
public ResponseEntity<ApiResponse<Long>> countPendingRequests(
    Authentication authentication)
```

---

### ✅ 6. Chấp nhận lời mời

**Yêu cầu UC08:** Người dùng có thể chấp nhận lời mời kết bạn

**Triển khai:**
- ✅ Endpoint: `POST /api/friends/accept/{requestId}`
- ✅ Kiểm tra quyền (chỉ receiver mới chấp nhận được)
- ✅ Kiểm tra hết hạn chưa
- ✅ Cập nhật status = ACCEPTED
- ✅ Tạo quan hệ bạn bè 2 chiều (bidirectional)

**Code:**
```java
@PostMapping("/accept/{requestId}")
public ResponseEntity<ApiResponse<Void>> acceptFriendRequest(
    @PathVariable Long requestId,
    Authentication authentication)

// Tạo 2 bản ghi Friendship để query nhanh
Friendship friendship1 = Friendship.builder()
    .user1(request.getSender())
    .user2(request.getReceiver())
    .build();

Friendship friendship2 = Friendship.builder()
    .user1(request.getReceiver())
    .user2(request.getSender())
    .build();
```

---

### ✅ 7. Từ chối lời mời

**Yêu cầu UC08:** Người dùng có thể từ chối lời mời kết bạn

**Triển khai:**
- ✅ Endpoint: `POST /api/friends/reject/{requestId}`
- ✅ Kiểm tra quyền (chỉ receiver mới từ chối được)
- ✅ Xóa lời mời khỏi database

**Code:**
```java
@PostMapping("/reject/{requestId}")
public ResponseEntity<ApiResponse<Void>> rejectFriendRequest(
    @PathVariable Long requestId,
    Authentication authentication)
```

---

### ✅ 8. Hủy kết bạn

**Yêu cầu UC08:** Người dùng có thể hủy kết bạn

**Triển khai:**
- ✅ Endpoint: `DELETE /api/friends/{friendId}`
- ✅ Kiểm tra quan hệ bạn bè tồn tại
- ✅ Xóa cả 2 chiều của quan hệ

**Code:**
```java
@DeleteMapping("/{friendId}")
public ResponseEntity<ApiResponse<Void>> unfriend(
    @PathVariable Long friendId,
    Authentication authentication)

// Repository
@Modifying
@Query("DELETE FROM Friendship f WHERE " +
       "(f.user1.id = :userId1 AND f.user2.id = :userId2) OR " +
       "(f.user1.id = :userId2 AND f.user2.id = :userId1)")
void deleteFriendshipBetweenUsers(@Param("userId1") Long userId1, 
                                   @Param("userId2") Long userId2);
```

---

### ✅ 9. Kiểm tra trạng thái quan hệ

**Triển khai bổ sung:** (Không có trong UC08 nhưng cần thiết cho UX)

- ✅ Endpoint: `GET /api/friends/status/{targetUserId}`
- ✅ Trả về: FRIENDS, PENDING_SENT, PENDING_RECEIVED, NONE
- ✅ Dùng để hiển thị nút phù hợp trên UI

**Code:**
```java
@GetMapping("/status/{targetUserId}")
public ResponseEntity<ApiResponse<String>> getFriendshipStatus(
    @PathVariable Long targetUserId,
    Authentication authentication)
```

---

### ✅ 10. Hủy lời mời đã gửi

**Triển khai bổ sung:** (Không có trong UC08 nhưng cần thiết)

- ✅ Endpoint: `DELETE /api/friends/request/{requestId}`
- ✅ Kiểm tra quyền (chỉ sender mới hủy được)
- ✅ Xóa lời mời

**Code:**
```java
@DeleteMapping("/request/{requestId}")
public ResponseEntity<ApiResponse<Void>> cancelFriendRequest(
    @PathVariable Long requestId,
    Authentication authentication)
```

---

### ✅ 11. Tự động dọn dẹp lời mời hết hạn

**Triển khai bổ sung:** (Tối ưu hiệu năng)

- ✅ Scheduled task chạy hàng ngày
- ✅ Xóa các lời mời PENDING đã hết hạn

**Code:**
```java
// FriendRequestCleanupScheduler.java
@Scheduled(cron = "0 0 2 * * ?") // 2 AM daily
public void cleanupExpiredRequests() {
    friendService.cleanupExpiredRequests();
}
```

---

## Cấu trúc Database

### Table: friend_requests
```sql
CREATE TABLE friend_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    expires_at TIMESTAMP,
    UNIQUE KEY unique_request (sender_id, receiver_id),
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
);
```

### Table: friendships
```sql
CREATE TABLE friendships (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id_1 BIGINT NOT NULL,
    user_id_2 BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    UNIQUE KEY unique_friendship (user_id_1, user_id_2),
    FOREIGN KEY (user_id_1) REFERENCES users(id),
    FOREIGN KEY (user_id_2) REFERENCES users(id)
);
```

**Lưu ý:** Quan hệ bạn bè được lưu 2 chiều để tối ưu query performance.

---

## Các tính năng bảo mật

1. ✅ **Authentication:** Tất cả endpoints yêu cầu JWT token
2. ✅ **Authorization:** Kiểm tra quyền trước khi thực hiện hành động
3. ✅ **Validation:** Validate input data với Bean Validation
4. ✅ **Unique Constraints:** Ngăn duplicate friend requests và friendships
5. ✅ **Expiration:** Lời mời tự động hết hạn sau 30 ngày
6. ✅ **Transaction:** Sử dụng @Transactional để đảm bảo data consistency

---

## Kết luận

✅ **Backend đã triển khai ĐẦY ĐỦ 100% yêu cầu UC08**

### Checklist UC08:
- ✅ Tìm người để kết bạn (Search với realtime)
- ✅ Gửi lời mời kết bạn
- ✅ Xem trang cá nhân
- ✅ Xem danh sách bạn bè
- ✅ Xem danh sách lời mời
- ✅ Chấp nhận lời mời
- ✅ Từ chối lời mời
- ✅ Hủy kết bạn
- ❌ Quét mã QR (đã loại bỏ theo yêu cầu)

### Tính năng bổ sung:
- ✅ Kiểm tra trạng thái quan hệ
- ✅ Hủy lời mời đã gửi
- ✅ Đếm số lượng lời mời pending
- ✅ Tự động dọn dẹp lời mời hết hạn
- ✅ Bidirectional friendship cho performance

### Sẵn sàng cho Frontend:
Backend đã hoàn thiện và sẵn sàng để frontend tích hợp. Tất cả endpoints đã được test và hoạt động đúng.
