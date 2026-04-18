# Kiến Trúc và Công Nghệ Hệ Thống MAPIC

## Tổng Quan

MAPIC là một ứng dụng mạng xã hội dựa trên vị trí địa lý, cho phép người dùng chia sẻ "moments" (khoảnh khắc) với hình ảnh, vị trí và thông tin địa điểm. Hệ thống được xây dựng theo kiến trúc Client-Server với frontend mobile và backend RESTful API.

---

## 1. Kiến Trúc Tổng Thể

### 1.1. Mô Hình Kiến Trúc

```
┌─────────────────────────────────────────────────────────┐
│                    FRONTEND (Mobile)                     │
│              React Native + Expo Router                  │
│                                                           │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────┐ │
│  │ Screens  │  │Components│  │ Services │  │  Store  │ │
│  └──────────┘  └──────────┘  └──────────┘  └─────────┘ │
└─────────────────────────────────────────────────────────┘
                            │
                    HTTP/REST API (JWT)
                            │
┌─────────────────────────────────────────────────────────┐
│                    BACKEND (Server)                      │
│                  Spring Boot 3.2.5                       │
│                                                           │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────┐ │
│  │Controller│  │ Service  │  │Repository│  │ Entity  │ │
│  └──────────┘  └──────────┘  └──────────┘  └─────────┘ │
└─────────────────────────────────────────────────────────┘
                            │
                         JPA/Hibernate
                            │
┌─────────────────────────────────────────────────────────┐
│                    DATABASE & CACHING                     │
│           PostgreSQL (JPA) + Redis (Caching/Session)      │
└─────────────────────────────────────────────────────────┘
```

### 1.2. Luồng Dữ Liệu

1. **User Request** → Frontend gửi HTTP request với JWT token
2. **Authentication** → Backend xác thực token qua JwtAuthenticationFilter
3. **Business Logic** → Controller → Service → Repository
4. **Caching & Transient Data** → Redis xử lý OTP và Real-time Location
5. **Database** → JPA/Hibernate thực hiện query PostgreSQL cho dữ liệu bền vững
6. **Cloud Storage** → Upload/Download hình ảnh qua AWS S3 hoặc Cloudinary
7. **Response** → DTO được trả về dạng JSON
8. **UI Update** → Frontend cập nhật giao diện

---

## 2. Công Nghệ Frontend

### 2.1. Core Technologies

| Công nghệ | Phiên bản | Mục đích |
|-----------|-----------|----------|
| **React Native** | 0.81.5 | Framework mobile đa nền tảng |
| **Expo** | ~54.0.33 | Toolchain và SDK cho React Native |
| **Expo Router** | ~6.0.23 | File-based routing system |
| **TypeScript** | ~5.9.2 | Type safety và developer experience |
| **React** | 19.1.0 | UI library |

### 2.2. State Management & Data

- **Zustand** (5.0.11): Global state management (lightweight alternative to Redux)
- **Axios** (1.7.9): HTTP client với interceptors cho JWT
- **AsyncStorage** (2.1.0): Local storage cho token và user data

### 2.3. UI & UX Libraries

- **@expo/vector-icons** (15.0.3): Icon library
- **@react-navigation/bottom-tabs** (7.4.0): Bottom tab navigation
- **@react-navigation/native** (7.1.8): Navigation framework
- **expo-image** (3.0.11): Optimized image component
- **react-native-gesture-handler** (2.28.0): Touch gesture handling
- **react-native-reanimated** (4.1.1): Smooth animations

### 2.4. Maps & Location

- **@maplibre/maplibre-react-native** (10.4.2): Open-source map rendering
- **expo-location** (19.0.8): GPS và location services
- **expo-haptics** (15.0.8): Haptic feedback

### 2.5. Media & Files

- **expo-image-picker** (17.0.10): Camera và photo library access
- **expo-constants** (18.0.13): Environment variables

### 2.6. Cấu Trúc Thư Mục Frontend

```
frontend/
├── app/                          # Expo Router pages (file-based routing)
│   ├── _layout.tsx              # Root layout
│   ├── (tabs)/                  # Tab navigation group
│   │   ├── index.tsx            # Home screen
│   │   ├── explore.tsx          # Explore screen
│   │   ├── profile.tsx          # Profile screen
│   │   └── _layout.tsx          # Tab layout
│   ├── login.tsx                # Login screen
│   ├── register.tsx             # Register screen
│   └── ...                      # Other screens
│
├── src/
│   ├── screens/                 # Screen components (21 screens)
│   │   ├── HomeScreen.tsx
│   │   ├── ExploreScreen.tsx
│   │   ├── ProfileScreen.tsx
│   │   ├── MomentMapScreen.tsx
│   │   └── ...
│   │
│   ├── components/              # Reusable UI components (17 components)
│   │   ├── MomentCard.tsx
│   │   ├── CommentsModal.tsx
│   │   ├── ReactionPicker.tsx
│   │   ├── ValidatedInput.tsx
│   │   └── ...
│   │
│   ├── services/                # API integration
│   │   ├── api.ts              # Axios instance với JWT interceptors
│   │   ├── authService.ts      # Authentication APIs
│   │   ├── momentService.ts    # Moment APIs
│   │   ├── friendship.ts       # Friendship APIs
│   │   ├── albumService.ts     # Album APIs
│   │   └── provinceService.ts  # Province APIs
│   │
│   ├── hooks/                   # Custom React hooks
│   │   ├── usePaginatedMoments.ts
│   │   └── ...
│   │
│   ├── store/                   # Zustand stores
│   │   └── ...
│   │
│   ├── utils/                   # Helper functions
│   │   ├── useAuth.ts          # Authentication utilities
│   │   ├── validation.ts       # Form validation
│   │   ├── imageHelper.ts      # Image processing
│   │   └── ...
│   │
│   ├── types/                   # TypeScript type definitions
│   └── constants/               # App constants
│
├── .env                         # Environment variables
├── .env.example                 # Environment template
├── package.json                 # Dependencies
└── tsconfig.json                # TypeScript config
```

---

## 3. Công Nghệ Backend

### 3.1. Core Technologies

| Công nghệ | Phiên bản | Mục đích |
|-----------|-----------|----------|
| **Spring Boot** | 3.2.5 | Backend framework |
| **Java** | 17 | Programming language |
| **Maven** | - | Build tool và dependency management |
| **PostgreSQL** | - | Relational database (Dữ liệu bền vững) |
| **Redis** | - | Key-Value Store (OTP, Real-time Location, Caching) |
| **Hibernate/JPA** | - | ORM framework |

### 3.2. Security & Authentication

- **Spring Security** (3.2.5): Authentication và authorization
- **JWT (JJWT)** (0.12.5): Token-based authentication
  - jjwt-api: API definitions
  - jjwt-impl: Implementation
  - jjwt-jackson: JSON processing
- **BCrypt**: Password hashing
- **Bucket4j** (8.10.1): Rate limiting

### 3.3. Additional Libraries

- **Lombok**: Reduce boilerplate code (@Data, @RequiredArgsConstructor, etc.)
- **Spring Boot Starter Mail**: Email service cho OTP
- **Spring Boot Starter Validation**: Input validation
- **PostgreSQL Driver**: Database connectivity

### 3.4. Cấu Trúc Thư Mục Backend

```
backend/
├── src/main/
│   ├── java/com/mapic/backend/
│   │   ├── config/                      # Configuration classes
│   │   │   ├── SecurityConfig.java      # Spring Security config
│   │   │   ├── CorsConfig.java          # CORS configuration
│   │   │   ├── JwtAuthenticationFilter.java  # JWT filter
│   │   │   └── WebConfig.java           # Web configuration
│   │   │
│   │   ├── controllers/                 # REST API endpoints
│   │   │   ├── admin/                   # Admin endpoints
│   │   │   ├── moderator/               # Moderator endpoints
│   │   │   ├── AuthController.java      # /api/auth/*
│   │   │   ├── MomentController.java    # /api/moments/*
│   │   │   ├── UserController.java      # /api/users/*
│   │   │   ├── FriendshipController.java # /api/friends/*
│   │   │   ├── ReactionController.java  # /api/reactions/*
│   │   │   ├── CommentController.java   # /api/comments/*
│   │   │   ├── NotificationController.java # /api/notifications/*
│   │   │   ├── ProvinceController.java  # /api/provinces/*
│   │   │   └── AlbumController.java     # /api/albums/*
│   │   │
│   │   ├── services/                    # Business logic
│   │   │   ├── AuthService.java         # Authentication logic
│   │   │   ├── MomentService.java       # Moment CRUD và feed
│   │   │   ├── UserService.java         # User management
│   │   │   ├── FriendshipService.java   # Friend relationships
│   │   │   ├── ReactionService.java     # Reaction management
│   │   │   ├── CommentService.java      # Comment management
│   │   │   ├── NotificationService.java # Notification system
│   │   │   ├── ProvinceService.java     # Province detection
│   │   │   ├── AlbumService.java        # Album management
│   │   │   ├── OtpService.java          # OTP generation/verification
│   │   │   ├── EmailService.java        # Email sending
│   │   │   ├── HashtagService.java      # Hashtag processing
│   │   │   ├── MomentStatsService.java  # Statistics calculation
│   │   │   ├── DataSeederService.java   # Database seeding
│   │   │   └── AdminAuthService.java    # Admin authentication
│   │   │
│   │   ├── repositories/                # Data access layer (JPA)
│   │   │   ├── UserRepository.java
│   │   │   ├── MomentRepository.java
│   │   │   ├── FriendshipRepository.java
│   │   │   ├── ReactionRepository.java
│   │   │   ├── CommentRepository.java
│   │   │   ├── NotificationRepository.java
│   │   │   ├── ProvinceRepository.java
│   │   │   ├── AlbumRepository.java
│   │   │   ├── SavedMomentRepository.java
│   │   │   ├── HashtagRepository.java
│   │   │   ├── TaggingRepository.java
│   │   │   └── OtpTokenRepository.java
│   │   │
│   │   ├── entities/                    # JPA entities (50+ classes)
│   │   │   ├── User.java               # User entity
│   │   │   ├── UserProfile.java        # User profile
│   │   │   ├── Moment.java             # Moment entity
│   │   │   ├── Province.java           # Province entity
│   │   │   ├── Friendship.java         # Friendship relationship
│   │   │   ├── Reaction.java           # Base reaction
│   │   │   ├── MomentReaction.java     # Moment reactions
│   │   │   ├── CommentReaction.java    # Comment reactions
│   │   │   ├── Comment.java            # Comment entity
│   │   │   ├── Notification.java       # Notification entity
│   │   │   ├── Album.java              # Album entity
│   │   │   ├── AlbumItem.java          # Album items
│   │   │   ├── SavedMoment.java        # Saved moments
│   │   │   ├── Hashtag.java            # Hashtag entity
│   │   │   ├── Tagging.java            # Tagging relationship
│   │   │   ├── OtpToken.java           # OTP tokens
│   │   │   ├── Account.java            # Base account class
│   │   │   ├── Admin.java              # Admin entity
│   │   │   ├── Moderator.java          # Moderator entity
│   │   │   ├── Message.java            # Chat message (base)
│   │   │   ├── Conversation.java       # Chat conversation
│   │   │   ├── SOSAlert.java           # SOS alert (future)
│   │   │   └── ... (enums và entities khác)
│   │   │
│   │   ├── dtos/                        # Data Transfer Objects
│   │   │   ├── ApiResponse.java        # Generic API response
│   │   │   ├── AuthResponse.java       # Authentication response
│   │   │   ├── MomentDto.java          # Moment DTO
│   │   │   ├── CommentDto.java         # Comment DTO
│   │   │   ├── ReactionDto.java        # Reaction DTO
│   │   │   ├── FriendshipDto.java      # Friendship DTO
│   │   │   ├── PageResponse.java       # Paginated response
│   │   │   ├── RegisterRequest.java    # Registration request
│   │   │   ├── LoginRequest.java       # Login request
│   │   │   ├── CreateMomentRequest.java # Create moment request
│   │   │   └── ... (các DTOs khác)
│   │   │
│   │   ├── utils/                       # Utility classes
│   │   │   ├── JwtUtil.java            # JWT token generation/validation
│   │   │   ├── OtpGenerator.java       # OTP code generation
│   │   │   ├── OtpSender.java          # OTP email sending
│   │   │   └── ImageDownloader.java    # Image download utility
│   │   │
│   │   └── MapicBackendApplication.java # Main application class
│   │
│   └── resources/
│       ├── application.properties       # Application configuration
│       ├── data.sql                     # Initial data (45 provinces)
│       ├── static/                      # Static resources
│       └── templates/                   # Email templates
│
├── uploads/                             # File storage
│   ├── moments/                         # Moment images (435 files)
│   └── avatars/                         # User avatars (11 files)
│
├── pom.xml                              # Maven dependencies
├── mvnw                                 # Maven wrapper (Linux/Mac)
├── mvnw.cmd                             # Maven wrapper (Windows)
└── QUICK_START.md                       # Quick start guide
```

---

## 4. Database Schema

### 4.1. PostgreSQL Configuration

```properties
Database: mapic_db
Host: localhost
Port: 5432
Username: postgres
Password: 123
```

### 4.2. Core Tables

#### Users & Authentication
- **users**: User accounts (extends Account)
- **user_profiles**: User profile information
- **admins**: Admin accounts
- **moderators**: Moderator accounts
- **otp_tokens**: OTP verification codes

#### Moments & Content
- **moments**: User-generated moments
- **provinces**: 45 Vietnamese provinces
- **saved_moments**: Saved moments by users
- **albums**: User albums
- **album_items**: Moments in albums

#### Social Features
- **friendships**: Friend relationships
- **comments**: Comments on moments
- **moment_reactions**: Reactions on moments
- **comment_reactions**: Reactions on comments
- **notifications**: User notifications

#### Hashtags & Tagging
- **hashtags**: Hashtag entities
- **taggings**: Polymorphic tagging relationships

#### Messaging (Infrastructure)
- **conversations**: Chat conversations
- **messages**: Chat messages (polymorphic)
- **participants**: Conversation participants

#### Reports & Moderation
- **moment_reports**: Reported moments
- **comment_reports**: Reported comments

#### Future Features
- **sos_alerts**: Emergency SOS alerts
- **sos_recipients**: SOS alert recipients

### 4.3. Key Relationships

```
User (1) ─────< (N) Moment
User (1) ─────< (N) Comment
User (1) ─────< (N) Reaction
User (1) ─────< (N) Notification
User (1) ─────< (N) Friendship
User (1) ─────< (N) Album
Moment (1) ───< (N) Comment
Moment (1) ───< (N) Reaction
Moment (N) ───> (1) Province
Comment (1) ──< (N) Comment (self-referencing for replies)
```

---

## 5. Security Architecture

### 5.1. Authentication Flow

```
1. User Login
   ↓
2. Backend validates credentials (BCrypt)
   ↓
3. Generate JWT token (24h expiration)
   ↓
4. Return token + user info
   ↓
5. Frontend stores token in AsyncStorage
   ↓
6. All subsequent requests include token in Authorization header
   ↓
7. JwtAuthenticationFilter validates token
   ↓
8. Request processed if valid
```

### 5.2. JWT Token Structure

```json
{
  "sub": "username",
  "userId": 123,
  "role": "USER",
  "iat": 1234567890,
  "exp": 1234654290
}
```

### 5.3. Security Features

- **Password Hashing**: BCrypt với salt
- **JWT Secret**: 256-bit secure key
- **Token Expiration**: 24 giờ
- **CORS**: Configured cho all origins (development)
- **Stateless Sessions**: Không lưu session trên server
- **Role-Based Access**: USER, ADMIN, MODERATOR
- **OTP Verification**: Lưu trữ trong **Redis** với TTL tự động xóa sau 5 phút
- **Real-time Location**: Tọa độ người dùng được cập nhật liên tục vào **Redis** để giảm tải cho DB
- **Rate Limiting**: Bucket4j với Redis cho API throttling

### 5.4. Authorization Levels

| Endpoint Pattern | Access Level |
|-----------------|--------------|
| `/api/auth/**` | Public |
| `/api/provinces/**` | Public |
| `/uploads/**` | Public |
| `/api/admin/**` | ADMIN only |
| `/api/moderator/**` | ADMIN + MODERATOR |
| `/api/**` | Authenticated users |

---

## 6. API Architecture

### 6.1. RESTful Design

- **Base URL**: `http://localhost:8080/api`
- **Content-Type**: `application/json` (hoặc `multipart/form-data` cho uploads)
- **Authentication**: Bearer token trong Authorization header

### 6.2. Response Format

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

### 6.3. Pagination Format

```json
{
  "content": [...],
  "page": 0,
  "size": 10,
  "totalElements": 100,
  "totalPages": 10,
  "last": false,
  "first": true
}
```

### 6.4. Error Handling

```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

---

## 7. Media & File Storage (Cloud Based)

### 7.1. Storage Providers
Hệ thống hỗ trợ lưu trữ linh hoạt trên Cloud để đảm bảo tính sẵn sàng cao và khả năng mở rộng:

- **AWS S3**: Lưu trữ hình ảnh moments và avatars quy mô lớn.
- **Cloudinary**: Tự động tối ưu hóa hình ảnh, resize và phân phối qua CDN.
- **Local Filesystem** (Chỉ dùng cho môi trường Development): Lưu trong thư mục `backend/uploads/`.

### 7.2. File Naming & Organization
- **Format**: `{UUID}.{extension}`
- **Pathing**: Phân loại theo `/moments/` và `/avatars/` trên bucket.
- **CDN Access**: Sử dụng URL trực tiếp từ Cloudinary/S3 để hiển thị trên app.

---

## 8. Development & Deployment

### 8.1. Backend Startup

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### 8.2. Frontend Startup

```bash
cd frontend
npm install
npx expo start
```

### 8.3. Environment Variables

**Frontend (.env)**:
```
EXPO_PUBLIC_API_URL=http://localhost:8080/api
```

**Backend (application.properties)**:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mapic_db
spring.datasource.username=postgres
spring.datasource.password=123
jwt.secret=mapic-secret-key-for-jwt-token-generation-minimum-256-bits-long-secure-key
jwt.expiration=86400000
server.port=8080
```

### 8.4. Database Initialization

- **Auto-seeding**: Chạy tự động lần đầu khởi động
- **Data**: 20 users, 100 moments, 45 provinces, friendships
- **Images**: Tự động download từ picsum.photos

---

## 9. Performance & Optimization

### 9.1. Database Optimization

- **Indexes**: Trên các cột thường query (username, email, hashtag name, notification recipient)
- **Lazy Loading**: Relationships được load khi cần
- **Pagination**: Tất cả list endpoints đều có pagination
- **Connection Pooling**: HikariCP (default trong Spring Boot)

### 9.2. Frontend Optimization

- **Image Optimization**: Expo Image với caching
- **Lazy Loading**: Components load khi cần
- **State Management**: Zustand (lightweight, fast)
- **Navigation**: File-based routing (Expo Router)

### 9.3. Caching Strategy

- **Frontend**: AsyncStorage cho token và user data
- **Backend**: JPA second-level cache (có thể enable)
- **Images**: Browser/app cache

---

## 10. Scalability Considerations

### 10.1. Horizontal Scaling

- **Stateless Backend**: Có thể scale multiple instances
- **JWT**: Không cần shared session storage
- **Database**: PostgreSQL có thể replicate

### 10.2. Future Improvements

- **CDN**: Cho static files và images
- **Redis**: Cho caching và session management
- **Message Queue**: Cho async tasks (email, notifications)
- **Microservices**: Tách các services độc lập
- **WebSocket**: Cho real-time features (chat, notifications)

---

## 11. Monitoring & Logging

### 11.1. Backend Logging

- **Framework**: SLF4J + Logback (Spring Boot default)
- **Levels**: INFO, DEBUG, ERROR
- **Format**: Timestamp, level, class, message

### 11.2. Frontend Logging

- **Console**: Development logging
- **Error Tracking**: Có thể tích hợp Sentry

---

## 12. Testing Strategy

### 12.1. Backend Testing

- **Unit Tests**: JUnit 5
- **Integration Tests**: Spring Boot Test
- **Test Database**: H2 in-memory hoặc PostgreSQL test instance

### 12.2. Frontend Testing

- **Unit Tests**: Jest
- **Component Tests**: React Testing Library
- **E2E Tests**: Detox (có thể thêm)

---

## Tổng Kết

MAPIC được xây dựng với kiến trúc hiện đại, scalable và maintainable:

- **Frontend**: React Native + Expo cho cross-platform mobile
- **Backend**: Spring Boot với RESTful API
- **Database**: PostgreSQL với JPA/Hibernate
- **Security**: JWT + Spring Security
- **File Storage**: Local filesystem (có thể migrate sang cloud)

Hệ thống sẵn sàng cho development và có thể mở rộng cho production với các improvements về caching, CDN, và microservices.
