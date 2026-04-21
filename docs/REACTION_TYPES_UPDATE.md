# Cập nhật Reaction Types

## Yêu cầu

Giới hạn các loại reaction cho từng entity:

### 1. Moment Reactions
- **Chỉ cho phép**: HEART (❤️ tim đỏ)
- **Loại bỏ**: LIKE, HAHA, WOW, SAD, ANGRY

### 2. Comment Reactions
- **Cho phép**: LIKE 👍, HEART ❤️, HAHA 😂, WOW 😮, SAD 😢, ANGRY 😠 (6 loại)

### 3. Message Reactions
- **Cho phép**: LIKE 👍, HEART ❤️, HAHA 😂, WOW 😮, SAD 😢, ANGRY 😠 (6 loại)

## Database Schema

Giữ nguyên enum trong database:
```sql
CREATE TYPE reaction_type AS ENUM ('LIKE', 'HEART', 'HAHA', 'WOW', 'SAD', 'ANGRY');
```

Không cần migration - database đã có đủ các giá trị này.

## Backend Implementation

### ReactionType.java
```java
public enum ReactionType {
    LIKE,   // 👍 Thích - comment/message only
    HEART,  // ❤️ Tim đỏ - all entities
    HAHA,   // 😂 Cười - comment/message only
    WOW,    // 😮 Ngạc nhiên - comment/message only
    SAD,    // 😢 Buồn - comment/message only
    ANGRY   // 😠 Phẫn nộ - comment/message only
}
```

### Validation
```java
// Moment - chỉ HEART
private static final Set<ReactionType> ALLOWED_MOMENT_REACTIONS = 
    Set.of(ReactionType.HEART);

// Comment & Message - 6 loại
private static final Set<ReactionType> ALLOWED_COMMENT_MESSAGE_REACTIONS = 
    Set.of(ReactionType.LIKE, ReactionType.HEART, ReactionType.HAHA, 
           ReactionType.WOW, ReactionType.SAD, ReactionType.ANGRY);
```

## Frontend Implementation

### MomentCard.tsx
- Nút Heart đơn giản (icon tim ❤️)
- Chỉ toggle HEART on/off
- Không có popup chọn reaction
- Animation khi thả tim

### CommentItem.tsx (future)
- Popup/menu chọn 6 reaction
- Icons: 👍 ❤️ 😂 😮 😢 😠

### MessageItem.tsx (future)
- Giống Comment: 6 reaction types

## API Examples

### Moment - Success
```json
POST /api/reactions/moments/123
Body: { "type": "HEART" }

Response: {
  "success": true,
  "message": "Reaction added successfully",
  "data": { "id": 1, "type": "HEART", ... }
}
```

### Moment - Error (wrong type)
```json
POST /api/reactions/moments/123
Body: { "type": "LIKE" }

Response: {
  "success": false,
  "message": "Reaction type LIKE is not allowed for moments. Only HEART is supported."
}
```

## Notes

- ✅ Tương thích với database cũ (không cần migration)
- ✅ HEART là reaction duy nhất cho moment
- ✅ Comment/Message có đầy đủ 6 loại reaction
- ✅ Validation ở service layer
- ✅ Frontend chỉ hiển thị options hợp lệ
