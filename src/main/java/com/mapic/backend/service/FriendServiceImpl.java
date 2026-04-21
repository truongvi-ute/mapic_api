package com.mapic.backend.service;

import com.mapic.backend.dto.request.SendFriendRequestDto;
import com.mapic.backend.dto.response.FriendRequestResponse;
import com.mapic.backend.dto.response.FriendResponse;
import com.mapic.backend.dto.response.UserSearchResponse;
import com.mapic.backend.entity.*;
import com.mapic.backend.exception.BadRequestException;
import com.mapic.backend.exception.NotFoundException;
import com.mapic.backend.repository.FriendRequestRepository;
import com.mapic.backend.repository.FriendshipRepository;
import com.mapic.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendServiceImpl implements IFriendService {

    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipRepository friendshipRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserSearchResponse> searchUsers(String query, Long currentUserId) {
        if (query == null || query.trim().length() < 2) {
            throw new BadRequestException("Search query must be at least 2 characters");
        }

        log.info("Searching users with query: {} for user: {}", query, currentUserId);

        String searchQuery = "%" + query.toLowerCase() + "%";
        List<User> users = userRepository.searchByNameOrUsername(searchQuery);

        log.info("Found {} users matching query", users.size());

        return users.stream()
                .filter(user -> !user.getId().equals(currentUserId)) // Exclude current user
                .map(user -> {
                    try {
                        return mapToUserSearchResponse(user, currentUserId);
                    } catch (Exception e) {
                        log.error("Error mapping user {} to response: {}", user.getId(), e.getMessage());
                        throw e;
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void sendFriendRequest(SendFriendRequestDto dto, Long senderId) {
        // Validate
        if (senderId.equals(dto.getReceiverId())) {
            throw new BadRequestException("Cannot send friend request to yourself");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new NotFoundException("Sender not found"));
        User receiver = userRepository.findById(dto.getReceiverId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check if already friends
        boolean areFriends = friendshipRepository.existsFriendshipBetweenUsers(senderId, dto.getReceiverId());
        if (areFriends) {
            throw new BadRequestException("Already friends");
        }

        // Check if request already exists (not expired)
        Optional<FriendRequest> existingRequest = friendRequestRepository
                .findPendingRequestBetweenUsers(senderId, dto.getReceiverId(), LocalDateTime.now());
        if (existingRequest.isPresent()) {
            throw new BadRequestException("Friend request already sent");
        }

        // Create new request
        FriendRequest request = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(FriendRequestStatus.PENDING)
                .build();

        friendRequestRepository.save(request);
        log.info("Friend request sent from user {} to user {}", senderId, dto.getReceiverId());
    }

    @Override
    @Transactional
    public void acceptFriendRequest(Long requestId, Long currentUserId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Friend request not found"));

        // Verify receiver
        if (!request.getReceiver().getId().equals(currentUserId)) {
            throw new BadRequestException("Unauthorized to accept this request");
        }

        // Check if expired
        if (request.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Friend request has expired");
        }

        // Update request status
        request.setStatus(FriendRequestStatus.ACCEPTED);
        friendRequestRepository.save(request);

        // Create bidirectional friendship
        Friendship friendship1 = Friendship.builder()
                .user1(request.getSender())
                .user2(request.getReceiver())
                .build();

        Friendship friendship2 = Friendship.builder()
                .user1(request.getReceiver())
                .user2(request.getSender())
                .build();

        friendshipRepository.save(friendship1);
        friendshipRepository.save(friendship2);

        log.info("Friend request {} accepted. Users {} and {} are now friends",
                requestId, request.getSender().getId(), request.getReceiver().getId());
    }

    @Override
    @Transactional
    public void rejectFriendRequest(Long requestId, Long currentUserId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Friend request not found"));

        // Verify receiver
        if (!request.getReceiver().getId().equals(currentUserId)) {
            throw new BadRequestException("Unauthorized to reject this request");
        }

        // Delete request
        friendRequestRepository.delete(request);
        log.info("Friend request {} rejected by user {}", requestId, currentUserId);
    }

    @Override
    @Transactional
    public void cancelFriendRequest(Long requestId, Long currentUserId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Friend request not found"));

        // Verify sender
        if (!request.getSender().getId().equals(currentUserId)) {
            throw new BadRequestException("Unauthorized to cancel this request");
        }

        // Delete request
        friendRequestRepository.delete(request);
        log.info("Friend request {} cancelled by user {}", requestId, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendResponse> getAllFriends(Long userId) {
        List<Friendship> friendships = friendshipRepository.findAllFriendsByUserId(userId);
        List<FriendResponse> friends = new ArrayList<>();
        
        // Use Set to track already added friend IDs to avoid duplicates
        java.util.Set<Long> addedFriendIds = new java.util.HashSet<>();

        for (Friendship friendship : friendships) {
            User friend = friendship.getUser1().getId().equals(userId)
                    ? friendship.getUser2()
                    : friendship.getUser1();

            // Skip if already added (because of bidirectional friendship)
            if (addedFriendIds.contains(friend.getId())) {
                continue;
            }
            
            addedFriendIds.add(friend.getId());

            FriendResponse response = FriendResponse.builder()
                    .id(friend.getId())
                    .username(friend.getUsername())
                    .name(friend.getName())
                    .avatarUrl(friend.getUserProfile() != null ? friend.getUserProfile().getAvatarUrl() : null)
                    .friendsSince(friendship.getCreatedAt())
                    .build();

            friends.add(response);
        }

        return friends;
    }

    @Override
    @Transactional
    public void unfriend(Long friendId, Long currentUserId) {
        // Check if friendship exists
        boolean areFriends = friendshipRepository.existsFriendshipBetweenUsers(currentUserId, friendId);

        if (!areFriends) {
            throw new NotFoundException("Friendship not found");
        }

        // Delete both directions
        friendshipRepository.deleteFriendshipBetweenUsers(currentUserId, friendId);
        log.info("User {} unfriended user {}", currentUserId, friendId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendRequestResponse> getPendingRequests(Long userId) {
        List<FriendRequest> requests = friendRequestRepository
                .findPendingRequestsByReceiver(userId, LocalDateTime.now());

        return requests.stream()
                .map(this::mapToFriendRequestResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long countPendingRequests(Long userId) {
        return friendRequestRepository.countPendingRequestsByReceiver(userId, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public String getFriendshipStatus(Long currentUserId, Long targetUserId) {
        // Check if friends
        boolean areFriends = friendshipRepository.existsFriendshipBetweenUsers(currentUserId, targetUserId);
        if (areFriends) {
            return "FRIENDS";
        }

        // Check if pending request exists (not expired)
        Optional<FriendRequest> request = friendRequestRepository
                .findPendingRequestBetweenUsers(currentUserId, targetUserId, LocalDateTime.now());
        if (request.isPresent()) {
            if (request.get().getSender().getId().equals(currentUserId)) {
                return "PENDING_SENT";
            } else {
                return "PENDING_RECEIVED";
            }
        }

        return "NONE";
    }

    @Override
    @Transactional(readOnly = true)
    public UserSearchResponse getUserById(Long userId, Long currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return mapToUserSearchResponse(user, currentUserId);
    }

    @Override
    @Transactional
    public void cleanupExpiredRequests() {
        List<FriendRequest> expiredRequests = friendRequestRepository
                .findByStatusAndExpiresAtBefore(FriendRequestStatus.PENDING, LocalDateTime.now());

        if (!expiredRequests.isEmpty()) {
            friendRequestRepository.deleteAll(expiredRequests);
            log.info("Cleaned up {} expired friend requests", expiredRequests.size());
        }
    }

    // Helper methods
    private UserSearchResponse mapToUserSearchResponse(User user, Long currentUserId) {
        String status = getFriendshipStatus(currentUserId, user.getId());

        return UserSearchResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .avatarUrl(user.getUserProfile() != null ? user.getUserProfile().getAvatarUrl() : null)
                .friendshipStatus(status)
                .build();
    }

    private FriendRequestResponse mapToFriendRequestResponse(FriendRequest request) {
        User sender = request.getSender();
        return FriendRequestResponse.builder()
                .id(request.getId())
                .senderId(sender.getId())
                .senderName(sender.getName())
                .senderUsername(sender.getUsername())
                .senderAvatarUrl(sender.getUserProfile() != null ? sender.getUserProfile().getAvatarUrl() : null)
                .createdAt(request.getCreatedAt())
                .build();
    }
}
