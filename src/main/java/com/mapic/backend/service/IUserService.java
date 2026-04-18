package com.mapic.backend.service;

import com.mapic.backend.dto.UpdateProfileRequest;
import com.mapic.backend.dto.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface IUserService {
    UserProfileResponse getProfile(String username);
    UserProfileResponse updateProfile(String username, UpdateProfileRequest request);
    String uploadAvatar(String username, MultipartFile file);
    String uploadCoverImage(String username, MultipartFile file);
}
