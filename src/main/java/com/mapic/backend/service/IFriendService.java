package com.mapic.backend.service;

import com.mapic.backend.dto.request.SendFriendRequestDto;
import com.mapic.backend.dto.response.FriendRequestResponse;
import com.mapic.backend.dto.response.FriendResponse;
import com.mapic.backend.dto.response.UserSearchResponse;

import java.util.List;

public interface IFriendService {
    
    // Search users by name or username
    List<UserSearchResponse> searchUsers(String query, Long currentUserId);
    
    // Send friend request
    void sendFriendRequest(SendFriendRequestDto dto, Long senderId);
    
    // Accept friend request
    void acceptFriendRequest(Long requestId, Long currentUserId);
    
    // Reject friend request
    void rejectFriendRequest(Long requestId, Long currentUserId);
    
    // Cancel sent friend request
    void cancelFriendRequest(Long requestId, Long currentUserId);
    
    // Get all friends
    List<FriendResponse> getAllFriends(Long userId);
    
    // Unfriend
    void unfriend(Long friendId, Long currentUserId);
    
    // Get pending friend requests
    List<FriendRequestResponse> getPendingRequests(Long userId);
    
    // Count pending requests
    Long countPendingRequests(Long userId);
    
    // Get friendship status with another user
    String getFriendshipStatus(Long currentUserId, Long targetUserId);
    
    // Get user by ID (for QR code)
    UserSearchResponse getUserById(Long userId, Long currentUserId);
    
    // Cleanup expired requests (scheduled task)
    void cleanupExpiredRequests();
}
