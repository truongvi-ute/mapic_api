package com.mapic.backend.controller;

import com.mapic.backend.dto.ApiResponse;
import com.mapic.backend.dto.UpdateProfileRequest;
import com.mapic.backend.dto.UserProfileResponse;
import com.mapic.backend.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(Authentication authentication) {
        String username = authentication.getName();
        UserProfileResponse profile = userService.getProfile(username);
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));
    }

    @PutMapping("/update-profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        UserProfileResponse profile = userService.updateProfile(username, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", profile));
    }

    @PostMapping("/upload-avatar")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        String username = authentication.getName();
        String avatarUrl = userService.uploadAvatar(username, file);
        return ResponseEntity.ok(ApiResponse.success("Avatar uploaded successfully", Map.of("avatarUrl", avatarUrl)));
    }

    @PostMapping("/upload-cover")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadCover(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        String username = authentication.getName();
        String coverUrl = userService.uploadCoverImage(username, file);
        return ResponseEntity.ok(ApiResponse.success("Cover image uploaded successfully", Map.of("coverUrl", coverUrl)));
    }
}
