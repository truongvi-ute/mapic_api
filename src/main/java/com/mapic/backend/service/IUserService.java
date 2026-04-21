package com.mapic.backend.service;

import com.mapic.backend.dto.UpdateProfileRequest;
import com.mapic.backend.dto.UserProfileResponse;
import com.mapic.backend.dto.response.UserProfileWithFriendshipResponse;
import org.springframework.web.multipart.MultipartFile;

public interface IUserService {
    UserProfileResponse getProfile(String username);
    UserProfileWithFriendshipResponse getUserProfileById(Long userId, Long currentUserId);
    UserProfileResponse updateProfile(String username, UpdateProfileRequest request);
    String uploadAvatar(String username, MultipartFile file);
    String uploadCoverImage(String username, MultipartFile file);
}
