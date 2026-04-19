# Friend API Testing Guide

## Overview

This document provides testing instructions for the Friend Management API endpoints.

## Prerequisites

- Backend server running on `http://localhost:8080`
- Valid JWT token (login first)
- At least 2 user accounts for testing

## API Endpoints

### 1. Search Users

Search for users by name or username.

```http
GET /api/friends/search?query={searchQuery}
Authorization: Bearer {token}
```

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/friends/search?query=john" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Found 2 users",
  "data": [
    {
      "id": 2,
      "username": "john_doe",
      "name": "John Doe",
      "avatarUrl": "/uploads/avatars/123.jpg",
      "friendshipStatus": "NONE"
    }
  ]
}
```

**Friendship Status Values:**
- `NONE` - No relationship
- `PENDING_SENT` - Current user sent request
- `PENDING_RECEIVED` - Current user received request
- `FRIENDS` - Already friends

### 2. Send Friend Request

Send a friend request to another user.

```http
POST /api/friends/request
Authorization: Bearer {token}
Content-Type: application/json

{
  "receiverId": 2
}
```

**Example Request:**
```bash
curl -X POST "http://localhost:8080/api/friends/request" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"receiverId": 2}'
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Friend request sent successfully"
}
```

**Error Cases:**
- `400` - Cannot send to yourself
- `400` - Already friends
- `400` - Request already sent
- `404` - User not found

### 3. Get Pending Requests

Get all pending friend requests received by current user.

```http
GET /api/friends/requests/pending
Authorization: Bearer {token}
```

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/friends/requests/pending" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Retrieved 2 pending requests",
  "data": [
    {
      "id": 1,
      "senderId": 2,
      "senderName": "John Doe",
      "senderUsername": "john_doe",
      "senderAvatarUrl": "/uploads/avatars/123.jpg",
      "createdAt": "2024-01-15T10:30:00"
    }
  ]
}
```

### 4. Count Pending Requests

Get count of pending friend requests.

```http
GET /api/friends/requests/pending/count
Authorization: Bearer {token}
```

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/friends/requests/pending/count" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Pending requests count",
  "data": 2
}
```

### 5. Accept Friend Request

Accept a friend request.

```http
POST /api/friends/accept/{requestId}
Authorization: Bearer {token}
```

**Example Request:**
```bash
curl -X POST "http://localhost:8080/api/friends/accept/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Friend request accepted"
}
```

**Error Cases:**
- `404` - Request not found
- `400` - Unauthorized (not the receiver)
- `400` - Request expired

### 6. Reject Friend Request

Reject a friend request.

```http
POST /api/friends/reject/{requestId}
Authorization: Bearer {token}
```

**Example Request:**
```bash
curl -X POST "http://localhost:8080/api/friends/reject/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Friend request rejected"
}
```

### 7. Cancel Friend Request

Cancel a sent friend request.

```http
DELETE /api/friends/request/{requestId}
Authorization: Bearer {token}
```

**Example Request:**
```bash
curl -X DELETE "http://localhost:8080/api/friends/request/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Friend request cancelled"
}
```

### 8. Get All Friends

Get list of all friends.

```http
GET /api/friends
Authorization: Bearer {token}
```

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/friends" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Retrieved 5 friends",
  "data": [
    {
      "id": 2,
      "username": "john_doe",
      "name": "John Doe",
      "avatarUrl": "/uploads/avatars/123.jpg",
      "friendsSince": "2024-01-15T10:30:00"
    }
  ]
}
```

### 9. Unfriend

Remove a friend.

```http
DELETE /api/friends/{friendId}
Authorization: Bearer {token}
```

**Example Request:**
```bash
curl -X DELETE "http://localhost:8080/api/friends/2" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Unfriended successfully"
}
```

**Error Cases:**
- `404` - Friendship not found

### 10. Get Friendship Status

Check friendship status with another user.

```http
GET /api/friends/status/{targetUserId}
Authorization: Bearer {token}
```

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/friends/status/2" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Friendship status",
  "data": "FRIENDS"
}
```

**Status Values:**
- `NONE` - No relationship
- `PENDING_SENT` - Current user sent request
- `PENDING_RECEIVED` - Current user received request
- `FRIENDS` - Already friends

### 11. Get User By ID

Get user information by ID (for QR code scanning).

```http
GET /api/friends/user/{userId}
Authorization: Bearer {token}
```

**Example Request:**
```bash
curl -X GET "http://localhost:8080/api/friends/user/2" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Expected Response:**
```json
{
  "success": true,
  "message": "User found",
  "data": {
    "id": 2,
    "username": "john_doe",
    "name": "John Doe",
    "avatarUrl": "/uploads/avatars/123.jpg",
    "friendshipStatus": "NONE"
  }
}
```

## Testing Workflow

### Scenario 1: Send and Accept Friend Request

1. **User A** searches for User B:
   ```bash
   GET /api/friends/search?query=userB
   ```

2. **User A** sends friend request to User B:
   ```bash
   POST /api/friends/request
   Body: { "receiverId": 2 }
   ```

3. **User B** checks pending requests:
   ```bash
   GET /api/friends/requests/pending
   ```

4. **User B** accepts the request:
   ```bash
   POST /api/friends/accept/1
   ```

5. **Both users** can now see each other in friends list:
   ```bash
   GET /api/friends
   ```

### Scenario 2: Send and Reject Friend Request

1. **User A** sends friend request to User B
2. **User B** checks pending requests
3. **User B** rejects the request:
   ```bash
   POST /api/friends/reject/1
   ```

### Scenario 3: Cancel Sent Request

1. **User A** sends friend request to User B
2. **User A** changes mind and cancels:
   ```bash
   DELETE /api/friends/request/1
   ```

### Scenario 4: Unfriend

1. **User A** and **User B** are friends
2. **User A** unfriends User B:
   ```bash
   DELETE /api/friends/2
   ```

## Database Schema

### friend_requests table

```sql
CREATE TABLE friend_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    
    UNIQUE (sender_id, receiver_id),
    INDEX (receiver_id),
    INDEX (status),
    INDEX (expires_at)
);
```

### friendships table

```sql
CREATE TABLE friendships (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id_1 BIGINT NOT NULL,
    user_id_2 BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    
    UNIQUE (user_id_1, user_id_2),
    INDEX (user_id_1),
    INDEX (user_id_2)
);
```

## Business Rules

1. **Cannot send friend request to yourself**
2. **Cannot send duplicate requests**
3. **Friend requests expire after 30 days**
4. **Friendships are bidirectional** (2 records created)
5. **Unfriending deletes both directions**
6. **Only receiver can accept/reject**
7. **Only sender can cancel**

## Scheduled Tasks

### Cleanup Expired Requests

Runs daily at 2 AM to delete expired friend requests.

```java
@Scheduled(cron = "0 0 2 * * *")
public void cleanupExpiredRequests()
```

## Error Handling

All endpoints return consistent error responses:

```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

**Common HTTP Status Codes:**
- `200` - Success
- `400` - Bad Request (validation error, business rule violation)
- `401` - Unauthorized (invalid/missing token)
- `404` - Not Found (user/request not found)
- `500` - Internal Server Error

## Performance Considerations

1. **Search query** must be at least 2 characters
2. **JOIN FETCH** used to avoid N+1 queries
3. **Indexes** on foreign keys and status fields
4. **Pagination** not implemented yet (consider for large friend lists)

## Security

1. **JWT Authentication** required for all endpoints
2. **Authorization checks** ensure users can only:
   - Accept/reject requests sent to them
   - Cancel requests sent by them
   - Unfriend their own friends
3. **Input validation** on all request bodies
4. **SQL injection prevention** via JPA/Hibernate

## Testing Checklist

- [ ] Search users by name
- [ ] Search users by username
- [ ] Send friend request
- [ ] Cannot send to self
- [ ] Cannot send duplicate request
- [ ] Cannot send if already friends
- [ ] View pending requests
- [ ] Count pending requests
- [ ] Accept friend request
- [ ] Reject friend request
- [ ] Cancel sent request
- [ ] View friends list
- [ ] Unfriend
- [ ] Check friendship status
- [ ] Get user by ID
- [ ] Request expires after 30 days
- [ ] Cleanup task runs daily

## Notes

- All timestamps are in ISO 8601 format
- Avatar URLs are relative paths (prepend base URL in frontend)
- Friendship status is calculated dynamically
- Bidirectional friendships ensure consistency
