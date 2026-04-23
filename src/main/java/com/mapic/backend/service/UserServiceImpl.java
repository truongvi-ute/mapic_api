package com.mapic.backend.service;

import com.mapic.backend.dto.UpdateProfileRequest;
import com.mapic.backend.dto.UserProfileResponse;
import com.mapic.backend.dto.response.UserProfileWithFriendshipResponse;
import com.mapic.backend.entity.FriendRequest;
import com.mapic.backend.entity.User;
import com.mapic.backend.entity.UserProfile;
import com.mapic.backend.exception.AppException;
import com.mapic.backend.exception.NotFoundException;
import com.mapic.backend.repository.FriendRequestRepository;
import com.mapic.backend.repository.FriendshipRepository;
import com.mapic.backend.repository.UserProfileRepository;
import com.mapic.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final FriendshipRepository friendshipRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final IStorageService storageService;

    @Override
    public UserProfileResponse getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("User not found"));
        
        return mapToResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileWithFriendshipResponse getUserProfileById(Long userId, Long currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        
        // Determine friendship status
        UserProfileWithFriendshipResponse.FriendshipStatus friendshipStatus = 
            determineFriendshipStatus(currentUserId, userId);
        
        UserProfile profile = user.getUserProfile();
        
        // Validate avatar file existence
        String avatarUrl = null;
        if (profile != null && profile.getAvatarUrl() != null) {
            String rawAvatarUrl = profile.getAvatarUrl();
            // For external URLs, use as-is
            if (rawAvatarUrl.startsWith("http://") || rawAvatarUrl.startsWith("https://")) {
                avatarUrl = rawAvatarUrl;
            } 
            // For local files, check existence
            else if (storageService.exists(rawAvatarUrl, "avatars")) {
                avatarUrl = storageService.resolveUrl(rawAvatarUrl, "avatars");
            } else {
                log.warn("Avatar file not found for user {}: {}", user.getUsername(), rawAvatarUrl);
            }
        }
        
        // Validate cover image file existence
        String coverImageUrl = null;
        if (profile != null && profile.getCoverImageUrl() != null) {
            String rawCoverUrl = profile.getCoverImageUrl();
            // For external URLs, use as-is
            if (rawCoverUrl.startsWith("http://") || rawCoverUrl.startsWith("https://")) {
                coverImageUrl = rawCoverUrl;
            }
            // For local files, check existence
            else if (storageService.exists(rawCoverUrl, "covers")) {
                coverImageUrl = storageService.resolveUrl(rawCoverUrl, "covers");
            } else {
                log.warn("Cover image file not found for user {}: {}", user.getUsername(), rawCoverUrl);
            }
        }
        
        return UserProfileWithFriendshipResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .bio(profile != null ? profile.getBio() : null)
                .avatarUrl(avatarUrl)
                .coverImageUrl(coverImageUrl)
                .gender(profile != null ? profile.getGender() : null)
                .dateOfBirth(profile != null ? profile.getDateOfBirth() : null)
                .friendshipStatus(friendshipStatus)
                .build();
    }

    private UserProfileWithFriendshipResponse.FriendshipStatus determineFriendshipStatus(Long currentUserId, Long targetUserId) {
        // Check if they are friends
        if (friendshipRepository.existsFriendshipBetweenUsers(currentUserId, targetUserId)) {
            return UserProfileWithFriendshipResponse.FriendshipStatus.FRIENDS;
        }
        
        // Check for pending friend requests
        Optional<FriendRequest> pendingRequest = friendRequestRepository.findPendingRequestBetweenUsers(
            currentUserId, targetUserId, LocalDateTime.now()
        );
        
        if (pendingRequest.isPresent()) {
            FriendRequest request = pendingRequest.get();
            if (request.getSender().getId().equals(currentUserId)) {
                return UserProfileWithFriendshipResponse.FriendshipStatus.PENDING_SENT;
            } else {
                return UserProfileWithFriendshipResponse.FriendshipStatus.PENDING_RECEIVED;
            }
        }
        
        return UserProfileWithFriendshipResponse.FriendshipStatus.NONE;
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("User not found"));

        // Update User fields
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        userRepository.save(user);

        // Update UserProfile fields
        UserProfile profile = user.getUserProfile();
        if (profile == null) {
            profile = UserProfile.builder()
                    .user(user)
                    .build();
        }
        
        profile.setBio(request.getBio());
        profile.setGender(request.getGender());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setLocation(request.getLocation());
        
        userProfileRepository.save(profile);

        return mapToResponse(user);
    }

    @Override
    @Transactional
    public String uploadAvatar(String username, MultipartFile file) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("User not found"));

        UserProfile profile = user.getUserProfile();
        if (profile == null) {
            profile = UserProfile.builder().user(user).build();
        }

        // Delete old avatar only if it's a local file (not external URL)
        if (profile.getAvatarUrl() != null 
                && !profile.getAvatarUrl().startsWith("http://") 
                && !profile.getAvatarUrl().startsWith("https://")) {
            storageService.delete(profile.getAvatarUrl(), "avatars");
        }

        String filename = storageService.store(file, "avatars");
        profile.setAvatarUrl(filename);
        userProfileRepository.save(profile);

        return storageService.resolveUrl(filename, "avatars");
    }

    @Override
    @Transactional
    public String uploadCoverImage(String username, MultipartFile file) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("User not found"));

        UserProfile profile = user.getUserProfile();
        if (profile == null) {
            profile = UserProfile.builder().user(user).build();
        }

        // Delete old cover only if it's a local file (not external URL)
        if (profile.getCoverImageUrl() != null
                && !profile.getCoverImageUrl().startsWith("http://")
                && !profile.getCoverImageUrl().startsWith("https://")) {
            storageService.delete(profile.getCoverImageUrl(), "covers");
        }

        String filename = storageService.store(file, "covers");
        profile.setCoverImageUrl(filename);
        userProfileRepository.save(profile);

        return storageService.resolveUrl(filename, "covers");
    }

    @Override
    @Transactional
    public void savePushToken(String username, String pushToken) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("User not found"));

        UserProfile profile = user.getUserProfile();
        if (profile == null) {
            profile = UserProfile.builder().user(user).build();
        }

        profile.setExpoPushToken(pushToken);
        userProfileRepository.save(profile);
        
        log.info("Saved push token for user: {}", username);
    }

    private UserProfileResponse mapToResponse(User user) {
        UserProfile profile = user.getUserProfile();
        
        // Validate avatar file existence
        String avatarUrl = null;
        if (profile != null && profile.getAvatarUrl() != null) {
            String rawAvatarUrl = profile.getAvatarUrl();
            // For external URLs, use as-is
            if (rawAvatarUrl.startsWith("http://") || rawAvatarUrl.startsWith("https://")) {
                avatarUrl = rawAvatarUrl;
            } 
            // For local files, check existence
            else if (storageService.exists(rawAvatarUrl, "avatars")) {
                avatarUrl = storageService.resolveUrl(rawAvatarUrl, "avatars");
            } else {
                log.warn("Avatar file not found for user {}: {}", user.getUsername(), rawAvatarUrl);
                // Clear the broken reference from database
                profile.setAvatarUrl(null);
                userProfileRepository.save(profile);
            }
        }
        
        // Validate cover image file existence
        String coverImageUrl = null;
        if (profile != null && profile.getCoverImageUrl() != null) {
            String rawCoverUrl = profile.getCoverImageUrl();
            // For external URLs, use as-is
            if (rawCoverUrl.startsWith("http://") || rawCoverUrl.startsWith("https://")) {
                coverImageUrl = rawCoverUrl;
            }
            // For local files, check existence
            else if (storageService.exists(rawCoverUrl, "covers")) {
                coverImageUrl = storageService.resolveUrl(rawCoverUrl, "covers");
            } else {
                log.warn("Cover image file not found for user {}: {}", user.getUsername(), rawCoverUrl);
                // Clear the broken reference from database
                profile.setCoverImageUrl(null);
                userProfileRepository.save(profile);
            }
        }
        
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .bio(profile != null ? profile.getBio() : null)
                .avatarUrl(avatarUrl)
                .coverImageUrl(coverImageUrl)
                .gender(profile != null ? profile.getGender() : null)
                .dateOfBirth(profile != null ? profile.getDateOfBirth() : null)
                .location(profile != null ? profile.getLocation() : null)
                .profileUpdatedAt(profile != null ? profile.getUpdatedAt() : null)
                .build();
    }
}
