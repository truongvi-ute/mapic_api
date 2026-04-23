package com.mapic.backend.service;

import com.mapic.backend.dto.request.UpdateUserStatusRequest;
import com.mapic.backend.dto.response.UserProfileResponse;
import com.mapic.backend.entity.AccountStatus;
import com.mapic.backend.entity.User;
import com.mapic.backend.exception.AppException;
import com.mapic.backend.exception.NotFoundException;
import com.mapic.backend.repository.FriendshipRepository;
import com.mapic.backend.repository.MomentRepository;
import com.mapic.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    private final UserRepository userRepository;
    private final MomentRepository momentRepository;
    private final FriendshipRepository friendshipRepository;
    private final IStorageService storageService;

    public Page<UserProfileResponse> getUsers(int page, int size, String search, String status) {
        log.info("Getting users - page: {}, size: {}, search: {}, status: {}", page, size, search, status);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<User> usersPage;
        
        if (search != null && !search.trim().isEmpty()) {
            List<User> searchResults = userRepository.searchByUsernameOrFullName(search, pageable);
            usersPage = new org.springframework.data.domain.PageImpl<>(searchResults, pageable, searchResults.size());
        } else {
            usersPage = userRepository.findAll(pageable);
        }
        
        return usersPage.map(this::convertToUserProfileResponse);
    }

    public UserProfileResponse getUserById(String userId) {
        log.info("Getting user by ID: {}", userId);
        
        Long id = Long.parseLong(userId);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
        
        return convertToUserProfileResponse(user);
    }

    @Transactional
    public void updateUserStatus(String userId, UpdateUserStatusRequest request) {
        log.info("Updating user {} status to {}", userId, request.getStatus());
        
        Long id = Long.parseLong(userId);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found"));
        
        // Validate status - chỉ hỗ trợ ACTIVE và BLOCK
        AccountStatus newStatus;
        if ("ACTIVE".equals(request.getStatus())) {
            newStatus = AccountStatus.ACTIVE;
        } else if ("BLOCK".equals(request.getStatus()) || "BANNED".equals(request.getStatus())) {
            newStatus = AccountStatus.BLOCK;
        } else {
            throw new AppException("Invalid status. Only ACTIVE and BLOCK are supported.");
        }
        
        user.setStatus(newStatus);
        userRepository.save(user);
        
        log.info("User {} status updated to {}", userId, newStatus);
    }

    public Object getUserActivity(String userId, int days) {
        log.info("Getting activity for user {} (last {} days)", userId, days);
        
        Long id = Long.parseLong(userId);
        User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("User not found"));
        
        Map<String, Object> activity = new HashMap<>();
        
        // Get moments count
        long momentsCount = momentRepository.findByAuthorId(id).size();
        activity.put("totalMoments", momentsCount);
        
        // Get friends count
        long friendsCount = friendshipRepository.findAllFriendsByUserId(id).size();
        activity.put("totalFriends", friendsCount);
        
        // Basic info
        activity.put("username", user.getUsername());
        activity.put("email", user.getEmail());
        activity.put("status", user.getStatus().name());
        activity.put("createdAt", user.getCreatedAt());
        activity.put("lastUpdated", user.getUpdatedAt());
        
        return activity;
    }

    public Page<UserProfileResponse> searchUsers(String query, int page, int size) {
        log.info("Searching users with query: {}", query);
        
        Pageable pageable = PageRequest.of(page, size);
        List<User> users = userRepository.searchByUsernameOrFullName(query, pageable);
        
        List<UserProfileResponse> responses = users.stream()
            .map(this::convertToUserProfileResponse)
            .collect(Collectors.toList());
        
        return new org.springframework.data.domain.PageImpl<>(responses, pageable, users.size());
    }
    
    private UserProfileResponse convertToUserProfileResponse(User user) {
        // Get additional data
        long momentsCount = momentRepository.findByAuthorId(user.getId()).size();
        long friendsCount = friendshipRepository.findAllFriendsByUserId(user.getId()).size();
        
        // Resolve avatar URL
        String avatarUrl = null;
        if (user.getUserProfile() != null && user.getUserProfile().getAvatarUrl() != null) {
            avatarUrl = storageService.resolveUrl(user.getUserProfile().getAvatarUrl(), "avatars");
        }
        
        return UserProfileResponse.builder()
            .id(user.getId().toString())
            .username(user.getUsername())
            .name(user.getName())
            .email(user.getEmail())
            .phone(user.getPhone())
            .status(user.getStatus().name())
            .createdAt(user.getCreatedAt())
            .lastSeenAt(user.getUpdatedAt() != null ? user.getUpdatedAt() : user.getCreatedAt())
            .bio(user.getUserProfile() != null ? user.getUserProfile().getBio() : null)
            .avatarUrl(avatarUrl)
            .gender(user.getUserProfile() != null && user.getUserProfile().getGender() != null 
                ? user.getUserProfile().getGender().name() : null)
            .location(user.getUserProfile() != null ? user.getUserProfile().getLocation() : null)
            .adminData(UserProfileResponse.AdminUserData.builder()
                .totalMoments((int) momentsCount)
                .totalFriends((int) friendsCount)
                .isBanned(user.getStatus() == AccountStatus.BLOCK)
                .build())
            .build();
    }
}
