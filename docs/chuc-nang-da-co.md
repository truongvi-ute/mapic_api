# Các Chức Năng Đã Có - MAPIC

## Tổng Quan

MAPIC là một ứng dụng mạng xã hội dựa trên vị trí địa lý, cho phép người dùng chia sẻ khoảnh khắc (moments) với hình ảnh và thông tin địa điểm. Dưới đây là danh sách đầy đủ các chức năng đã được triển khai.

---

## 1. Xác Thực & Quản Lý Tài Khoản

### 1.1. Đăng Ký Tài Khoản

**Mô tả**: Người dùng có thể tạo tài khoản mới với email verification

**Tính năng**:
- Nhập thông tin: username, name, email, password
- Validation username (chỉ chữ cái, số, gạch dưới)
- Kiểm tra username và email trùng lặp
- Mã hóa password với BCrypt
- Gửi OTP qua email để xác thực
- Tài khoản ở trạng thái INACTIVE cho đến khi verify

**API Endpoint**: `POST /api/auth/register`

**Screens**: RegisterScreen

---

### 1.2. Xác Thực OTP

**Mô tả**: Xác nhận email thông qua mã OTP 6 số

**Tính năng**:
- Nhập mã OTP 6 số
- Verify OTP với expiration time
- Kích hoạt tài khoản sau khi verify thành công
- Tự động login sau khi verify
- Nhận JWT token

**API Endpoint**: `POST /api/auth/verify-registration`

**Screens**: VerifyOTPScreen

---

### 1.3. Đăng Nhập

**Mô tả**: Đăng nhập vào hệ thống với username và password

**Tính năng**:
- Nhập username và password
- Xác thực credentials
- Kiểm tra tài khoản đã verify
- Kiểm tra tài khoản không bị block
- Tạo JWT token (24h expiration)
- Lưu token và user info vào AsyncStorage

**API Endpoint**: `POST /api/auth/login`

**Screens**: LoginScreen

---

### 1.4. Quên Mật Khẩu

**Mô tả**: Khôi phục mật khẩu qua email

**Tính năng**:
- Nhập email để yêu cầu reset password
- Kiểm tra email tồn tại và đã verify
- Gửi OTP qua email
- Verify OTP
- Đặt mật khẩu mới

**API Endpoints**:
- `POST /api/auth/forgot-password` - Gửi OTP
- `POST /api/auth/reset-password` - Reset password với OTP

**Screens**: ForgotPasswordScreen, ResetPasswordScreen

---

### 1.5. Gửi Lại OTP

**Mô tả**: Gửi lại mã OTP nếu không nhận được hoặc hết hạn

**Tính năng**:
- Hỗ trợ 3 loại OTP: registration, forgot-password, change-password
- Kiểm tra điều kiện phù hợp với từng loại
- Tạo OTP mới và gửi email

**API Endpoint**: `POST /api/auth/resend-otp`

**Screens**: VerifyOTPScreen, ResetPasswordScreen

---

### 1.6. Đăng Xuất

**Mô tả**: Đăng xuất khỏi ứng dụng

**Tính năng**:
- Xóa JWT token khỏi AsyncStorage
- Xóa user info khỏi local storage
- Redirect về màn hình login

**Screens**: ProfileScreen, SettingsScreen

---

## 2. Quản Lý Profile

### 2.1. Xem Profile

**Mô tả**: Xem thông tin profile của bản thân hoặc người khác

**Tính năng**:
- Hiển thị avatar, name, username, bio
- Hiển thị location, website, gender, date of birth
- Hiển thị số lượng moments, friends
- Hiển thị danh sách moments của user
- Phân quyền xem moments (public/private dựa trên friendship)

**API Endpoint**: `GET /api/users/{userId}`

**Screens**: ProfileScreen, UserProfileScreen

---

### 2.2. Chỉnh Sửa Profile

**Mô tả**: Cập nhật thông tin cá nhân

**Tính năng**:
- Cập nhật name, bio, location, website
- Cập nhật gender, date of birth
- Upload và thay đổi avatar
- Validation input

**API Endpoints**:
- `PUT /api/users/profile` - Cập nhật profile
- `POST /api/users/avatar` - Upload avatar

**Screens**: EditProfileScreen

---

## 3. Moments (Khoảnh Khắc)

### 3.1. Tạo Moment

**Mô tả**: Chia sẻ khoảnh khắc với hình ảnh và vị trí

**Tính năng**:
- Chọn ảnh từ thư viện hoặc chụp ảnh mới
- Nhập caption (mô tả)
- Tự động lấy vị trí hiện tại (GPS)
- Hiển thị địa chỉ từ coordinates
- Chọn category (TRAVEL, FOOD, NATURE, URBAN, SPORTS, CULTURE, BUSINESS, ENTERTAINMENT, EDUCATION, OTHER)
- Chọn privacy (Public/Private)
- Tự động detect province từ địa chỉ hoặc coordinates
- Upload ảnh lên server
- Lưu moment vào database

**API Endpoints**:
- `POST /api/moments/create` - Tạo moment (multipart/form-data)
- `POST /api/moments/upload-image` - Upload ảnh riêng

**Screens**: CreateMomentScreen

---

### 3.2. Xem Feed

**Mô tả**: Xem feed moments từ bạn bè và public moments

**Tính năng**:
- Hiển thị moments từ friends và public moments
- Pagination (load more khi scroll)
- Sort by created date (mới nhất trước)
- Hiển thị author info, image, caption, location
- Hiển thị reaction count, comment count, save count
- Pull to refresh

**API Endpoint**: `GET /api/moments/feed/paginated`

**Screens**: HomeScreen

---

### 3.3. Xem Moments Của User

**Mô tả**: Xem tất cả moments của một user cụ thể

**Tính năng**:
- Hiển thị moments của user (grid hoặc list view)
- Phân quyền xem:
  - Xem tất cả nếu là chính mình
  - Xem tất cả nếu là bạn bè
  - Chỉ xem public nếu không phải bạn bè
- Pagination
- Sort options

**API Endpoint**: `GET /api/moments/user/{userId}/paginated`

**Screens**: ProfileScreen, UserProfileScreen

---

### 3.4. Xem Moments Đã Lưu

**Mô tả**: Xem danh sách moments đã save

**Tính năng**:
- Hiển thị tất cả moments đã save
- Pagination
- Lọc bỏ moments đã bị xóa
- Unsave moment

**API Endpoint**: `GET /api/moments/saved/paginated`

**Screens**: ProfileScreen (Saved tab)

---

### 3.5. Xóa Moment

**Mô tả**: Xóa moment của chính mình

**Tính năng**:
- Chỉ author mới có thể xóa
- Soft delete (status = DELETED)
- Confirm dialog trước khi xóa

**API Endpoint**: `DELETE /api/moments/{momentId}`

**Screens**: ProfileScreen, MomentDetailScreen

---

### 3.6. Lưu/Bỏ Lưu Moment

**Mô tả**: Save moment để xem lại sau

**Tính năng**:
- Toggle save/unsave
- Cập nhật save count
- Hiển thị trạng thái saved

**API Endpoint**: `POST /api/moments/{momentId}/save`

**Screens**: MomentCard component

---

### 3.7. Xem Chi Tiết Moment

**Mô tả**: Xem moment ở chế độ full screen

**Tính năng**:
- Hiển thị ảnh full screen
- Zoom in/out
- Swipe để xem moment khác
- Hiển thị thông tin đầy đủ

**API Endpoint**: `GET /api/moments/{momentId}`

**Screens**: ImageViewerScreen

---

## 4. Khám Phá (Explore)

### 4.1. Khám Phá Theo Tỉnh Thành

**Mô tả**: Xem moments theo từng tỉnh thành Việt Nam

**Tính năng**:
- Hiển thị 45 tỉnh thành Việt Nam
- Lọc theo vùng miền (Bắc, Trung, Nam)
- Tìm kiếm tỉnh thành
- Xem moments của từng tỉnh
- Chỉ hiển thị public moments
- Pagination

**API Endpoints**:
- `GET /api/provinces` - Lấy danh sách tỉnh
- `GET /api/provinces/region/{region}` - Lọc theo vùng
- `GET /api/provinces/search` - Tìm kiếm
- `GET /api/moments/province/{provinceName}/paginated` - Moments theo tỉnh

**Screens**: ExploreScreen, ProvinceMomentsScreen

---

### 4.2. Khám Phá Theo Danh Mục

**Mô tả**: Xem moments theo category

**Tính năng**:
- 10 categories: TRAVEL, FOOD, NATURE, URBAN, SPORTS, CULTURE, BUSINESS, ENTERTAINMENT, EDUCATION, OTHER
- Hiển thị icon và tên category
- Xem moments của từng category
- Chỉ hiển thị public moments
- Pagination

**API Endpoint**: `GET /api/moments/category/{category}/paginated`

**Screens**: ExploreScreen, CategoryMomentsScreen

---

### 4.3. Xem Bản Đồ Moments

**Mô tả**: Xem moments trên bản đồ

**Tính năng**:
- Hiển thị moments dưới dạng markers trên map
- Click vào marker để xem moment
- Zoom in/out map
- Tự động center vào vị trí hiện tại
- Hiển thị preview moment khi click marker

**Screens**: MomentMapScreen

---

## 5. Tương Tác Xã Hội

### 5.1. Reactions (Cảm Xúc)

**Mô tả**: Thả cảm xúc cho moments và comments

**Tính năng**:
- 6 loại reactions: LIKE (👍), LOVE (❤️), HAHA (😂), WOW (😮), SAD (😢), ANGRY (😠)
- Reaction cho moments
- Reaction cho comments
- Toggle reaction (thay đổi hoặc remove)
- Hiển thị reaction count
- Hiển thị reaction của user hiện tại
- Xem danh sách users đã react

**API Endpoints**:
- `POST /api/reactions/moment/{momentId}` - Toggle moment reaction
- `GET /api/reactions/moment/{momentId}` - Lấy reactions của moment
- `GET /api/reactions/moment/{momentId}/my-reaction` - Reaction của user
- `POST /api/reactions/comment/{commentId}` - Toggle comment reaction
- `GET /api/reactions/comment/{commentId}` - Lấy reactions của comment

**Components**: ReactionPicker, MomentCard

---

### 5.2. Comments (Bình Luận)

**Mô tả**: Bình luận và trả lời bình luận

**Tính năng**:
- Comment trên moments
- Reply comment (nested comments)
- Hiển thị author info
- Hiển thị thời gian comment
- Xóa comment của chính mình
- Hiển thị comment count
- Pagination cho comments
- Reaction trên comments

**API Endpoints**:
- `POST /api/comments/moment/{momentId}` - Tạo comment
- `GET /api/comments/moment/{momentId}` - Lấy comments
- `DELETE /api/comments/{commentId}` - Xóa comment

**Components**: CommentsModal

---

## 6. Bạn Bè (Friendship)

### 6.1. Gửi Lời Mời Kết Bạn

**Mô tả**: Gửi friend request đến user khác

**Tính năng**:
- Tìm kiếm user theo tên
- Gửi friend request
- Kiểm tra trạng thái friendship hiện tại
- Không cho phép gửi duplicate request

**API Endpoint**: `POST /api/friends/send-request`

**Screens**: AddFriendScreen, UserProfileScreen

---

### 6.2. Chấp Nhận/Từ Chối Lời Mời

**Mô tả**: Quản lý friend requests nhận được

**Tính năng**:
- Xem danh sách pending requests
- Accept request
- Reject request
- Hiển thị thông tin requester

**API Endpoints**:
- `GET /api/friends/requests` - Lấy pending requests
- `POST /api/friends/accept/{friendshipId}` - Accept
- `POST /api/friends/reject/{friendshipId}` - Reject

**Screens**: FriendsScreen (Requests tab)

---

### 6.3. Xem Danh Sách Bạn Bè

**Mô tả**: Xem tất cả bạn bè

**Tính năng**:
- Hiển thị danh sách friends
- Hiển thị avatar, name, username
- Click để xem profile
- Unfriend

**API Endpoint**: `GET /api/friends/list`

**Screens**: FriendsScreen

---

### 6.4. Hủy Kết Bạn

**Mô tả**: Unfriend một user

**Tính năng**:
- Unfriend từ danh sách bạn bè
- Unfriend từ profile
- Confirm dialog

**API Endpoint**: `DELETE /api/friends/unfriend/{friendshipId}`

**Screens**: FriendsScreen, UserProfileScreen

---

### 6.5. Tìm Kiếm Người Dùng

**Mô tả**: Tìm kiếm users để kết bạn

**Tính năng**:
- Tìm kiếm theo tên
- Hiển thị kết quả với avatar và info
- Hiển thị trạng thái friendship
- Gửi friend request trực tiếp

**API Endpoint**: `GET /api/friends/search`

**Screens**: AddFriendScreen

---

## 7. Thông Báo (Notifications)

### 7.1. Nhận Thông Báo

**Mô tả**: Nhận thông báo về các hoạt động liên quan

**Các loại thông báo**:
- **MOMENT_REACTION**: Ai đó react moment của bạn
- **NEW_COMMENT**: Ai đó comment moment của bạn
- **NEW_REPLY**: Ai đó reply comment của bạn
- **COMMENT_REACTION**: Ai đó react comment của bạn
- **FRIEND_REQUEST**: Ai đó gửi friend request
- **FRIEND_ACCEPTED**: Friend request được chấp nhận
- **MOMENT_SAVED**: Ai đó save moment của bạn (optional)

**Tính năng**:
- Hiển thị avatar của actor
- Hiển thị nội dung thông báo
- Hiển thị thời gian
- Phân biệt đã đọc/chưa đọc
- Click để xem chi tiết
- Pagination

**API Endpoint**: `GET /api/notifications`

**Screens**: NotificationScreen

---

### 7.2. Đánh Dấu Đã Đọc

**Mô tả**: Đánh dấu thông báo đã đọc

**Tính năng**:
- Đánh dấu từng thông báo
- Đánh dấu tất cả
- Cập nhật unread count

**API Endpoints**:
- `PUT /api/notifications/{id}/read` - Đánh dấu 1 thông báo
- `PUT /api/notifications/read-all` - Đánh dấu tất cả

**Screens**: NotificationScreen

---

### 7.3. Đếm Thông Báo Chưa Đọc

**Mô tả**: Hiển thị số lượng thông báo chưa đọc

**Tính năng**:
- Badge trên notification icon
- Real-time update (có thể cải thiện với WebSocket)

**API Endpoint**: `GET /api/notifications/unread-count`

**Components**: Tab navigation badge

---

## 8. Albums

### 8.1. Tạo Album

**Mô tả**: Tạo album để tổ chức moments

**Tính năng**:
- Nhập tên album
- Nhập mô tả
- Chọn moments để thêm vào album
- Unique album name per user

**API Endpoint**: `POST /api/albums`

**Screens**: AlbumsScreen

---

### 8.2. Xem Albums

**Mô tả**: Xem danh sách albums của user

**Tính năng**:
- Hiển thị tất cả albums
- Hiển thị cover image (first moment)
- Hiển thị số lượng moments trong album
- Click để xem chi tiết

**API Endpoint**: `GET /api/albums`

**Screens**: AlbumsScreen

---

### 8.3. Xem Chi Tiết Album

**Mô tả**: Xem moments trong album

**Tính năng**:
- Hiển thị tất cả moments trong album
- Grid layout
- Thêm/xóa moments khỏi album
- Cập nhật thông tin album

**API Endpoint**: `GET /api/albums/{albumId}`

**Screens**: AlbumDetailScreen

---

### 8.4. Cập Nhật Album

**Mô tả**: Chỉnh sửa thông tin album

**Tính năng**:
- Đổi tên album
- Đổi mô tả
- Thêm/xóa moments

**API Endpoint**: `PUT /api/albums/{albumId}`

**Screens**: AlbumDetailScreen

---

## 9. Hashtags (Cơ Sở Hạ Tầng)

### 9.1. Hashtag System

**Mô tả**: Hệ thống hashtag cho moments và comments

**Tính năng**:
- Tự động extract hashtags từ caption/comment
- Lưu hashtags vào database
- Track usage count
- Polymorphic tagging (moments, comments)
- Tìm kiếm theo hashtag (có thể implement)

**Entities**: Hashtag, Tagging

**Note**: Infrastructure đã có, chưa có UI/UX hoàn chỉnh

---

## 10. Admin & Moderation

### 10.1. Database Seeding

**Mô tả**: Tự động seed dữ liệu mẫu

**Tính năng**:
- Seed 45 tỉnh thành Việt Nam
- Seed 20 users mẫu (minh1, huong2, ..., anh20)
- Seed 100 moments với ảnh thật
- Seed friendships giữa users
- Tự động download ảnh từ picsum.photos
- Chạy tự động lần đầu khởi động

**API Endpoint**: `POST /api/admin/seed-database`

**Service**: DataSeederService

---

### 10.2. Recalculate Statistics

**Mô tả**: Tính lại các thống kê

**Tính năng**:
- Recalculate reaction counts
- Recalculate comment counts
- Recalculate save counts
- Fix inconsistent data

**API Endpoint**: `POST /api/admin/recalculate-stats`

**Service**: MomentStatsService

---

### 10.3. Clean Invalid Moments

**Mô tả**: Xóa moments không hợp lệ

**Tính năng**:
- Xóa moments không có author
- Xóa moments không có image
- Cleanup orphaned data

**API Endpoint**: `DELETE /api/admin/clean-invalid-moments`

---

## 11. Tính Năng Hỗ Trợ

### 11.1. Validation

**Tính năng**:
- Email validation
- Password strength validation
- Username format validation
- Required field validation
- Input sanitization

**Components**: ValidatedInput

---

### 11.2. Image Handling

**Tính năng**:
- Image picker (camera/library)
- Image compression
- Image upload
- Image caching
- Image viewer với zoom

**Utils**: imageHelper.ts

---

### 11.3. Location Services

**Tính năng**:
- Get current location (GPS)
- Reverse geocoding (coordinates → address)
- Province detection từ coordinates
- Province detection từ address name

**Services**: ProvinceService, expo-location

---

### 11.4. Error Handling

**Tính năng**:
- Global error handling
- Network error handling
- 401 auto logout
- User-friendly error messages
- Retry mechanism

**Utils**: api.ts interceptors

---

## 12. Cơ Sở Hạ Tầng Cho Tương Lai

### 12.1. Messaging System (Infrastructure)

**Entities đã có**:
- Conversation
- Message (polymorphic)
- TextMessage
- AttachmentMessage
- ShareMessage
- CallMessage
- Participant
- MessageReaction

**Note**: Database schema đã có, chưa implement logic và UI

---

### 12.2. SOS Alert System (Infrastructure)

**Entities đã có**:
- SOSAlert
- SOSRecipient
- Location

**Note**: Database schema đã có, chưa implement logic và UI

---

### 12.3. Report System (Infrastructure)

**Entities đã có**:
- MomentReport
- CommentReport
- ReportReason enum
- ReportStatus enum

**Note**: Database schema đã có, chưa implement logic và UI

---

### 12.4. Video Support (Infrastructure)

**Entities đã có**:
- Video entity

**Note**: Database schema đã có, chưa implement logic và UI

---

## 13. Tổng Kết Chức Năng

### ✅ Đã Hoàn Thành (Production Ready)

1. **Authentication**: Register, Login, OTP, Forgot Password
2. **User Profile**: View, Edit, Avatar
3. **Moments**: Create, View, Delete, Save, Feed
4. **Explore**: By Province, By Category, Map View
5. **Social**: Reactions, Comments, Replies
6. **Friendship**: Add, Accept, Reject, Unfriend, Search
7. **Notifications**: Receive, Read, Unread Count
8. **Albums**: Create, View, Update
9. **Admin**: Seeding, Stats, Cleanup

### 🚧 Infrastructure Ready (Chưa Có UI/UX)

1. **Hashtags**: Database và logic đã có
2. **Messaging**: Database schema đã có
3. **SOS Alerts**: Database schema đã có
4. **Reports**: Database schema đã có
5. **Videos**: Database schema đã có

### 📊 Thống Kê

- **Screens**: 21 screens
- **Components**: 17 reusable components
- **API Endpoints**: 50+ endpoints
- **Entities**: 50+ database entities
- **Services**: 15+ backend services
- **Repositories**: 18+ data repositories

---

## 14. User Journey Examples

### Journey 1: Người Dùng Mới

1. Mở app → Màn hình Login
2. Click "Đăng ký" → RegisterScreen
3. Nhập thông tin → Nhận OTP qua email
4. Nhập OTP → VerifyOTPScreen
5. Verify thành công → Tự động login → HomeScreen
6. Xem feed (rỗng vì chưa có bạn bè)
7. Click tab Explore → Xem moments public
8. Click tab Profile → Chỉnh sửa profile
9. Upload avatar, điền bio
10. Tạo moment đầu tiên

### Journey 2: Tương Tác Xã Hội

1. Tìm kiếm bạn bè → AddFriendScreen
2. Gửi friend request
3. Đợi accept → Nhận notification
4. Xem feed → Thấy moments của bạn bè
5. React và comment
6. Nhận notification khi có người react/comment
7. Reply comments
8. Save moments yêu thích

### Journey 3: Khám Phá

1. Click tab Explore
2. Chọn tỉnh thành → Xem moments ở Đà Nẵng
3. Chọn category → Xem moments về Food
4. Click vào moment → Xem chi tiết
5. React và comment
6. Save moment
7. Xem trên map → MomentMapScreen

---

## Kết Luận

MAPIC đã có đầy đủ các chức năng cơ bản của một mạng xã hội dựa trên vị trí địa lý:

- ✅ Authentication & Authorization hoàn chỉnh
- ✅ User management và profiles
- ✅ Content creation và sharing (moments)
- ✅ Social interactions (reactions, comments, friendships)
- ✅ Discovery features (explore by location/category)
- ✅ Notification system
- ✅ Album organization
- ✅ Admin tools

Hệ thống sẵn sàng cho việc phát triển thêm các tính năng nâng cao như messaging, video support, và SOS alerts với infrastructure đã được chuẩn bị sẵn.
