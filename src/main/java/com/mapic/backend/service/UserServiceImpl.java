package com.mapic.backend.service;

import com.mapic.backend.dto.UpdateProfileRequest;
import com.mapic.backend.dto.UserProfileResponse;
import com.mapic.backend.entity.User;
import com.mapic.backend.entity.UserProfile;
import com.mapic.backend.exception.AppException;
import com.mapic.backend.repository.UserProfileRepository;
import com.mapic.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final IStorageService storageService;

    @Override
    public UserProfileResponse getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("User not found"));
        
        return mapToResponse(user);
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

        // Delete old avatar
        if (profile.getAvatarUrl() != null) {
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

        // Delete old cover
        if (profile.getCoverImageUrl() != null) {
            storageService.delete(profile.getCoverImageUrl(), "covers");
        }

        String filename = storageService.store(file, "covers");
        profile.setCoverImageUrl(filename);
        userProfileRepository.save(profile);

        return storageService.resolveUrl(filename, "covers");
    }

    private UserProfileResponse mapToResponse(User user) {
        UserProfile profile = user.getUserProfile();
        
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .bio(profile != null ? profile.getBio() : null)
                .avatarUrl(profile != null ? storageService.resolveUrl(profile.getAvatarUrl(), "avatars") : null)
                .coverImageUrl(profile != null ? storageService.resolveUrl(profile.getCoverImageUrl(), "covers") : null)
                .gender(profile != null ? profile.getGender() : null)
                .dateOfBirth(profile != null ? profile.getDateOfBirth() : null)
                .location(profile != null ? profile.getLocation() : null)
                .build();
    }
}
