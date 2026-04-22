package com.mapic.backend.controller;

import com.mapic.backend.dto.ApiResponse;
import com.mapic.backend.dto.UpdateProfileRequest;
import com.mapic.backend.dto.UserProfileResponse;
import com.mapic.backend.dto.response.UserProfileWithFriendshipResponse;
import com.mapic.backend.entity.User;
import com.mapic.backend.repository.UserRepository;
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
    private final UserRepository userRepository;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(Authentication authentication) {
        String username = authentication.getName();
        UserProfileResponse profile = userService.getProfile(username);
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileWithFriendshipResponse>> getUserProfile(
            @PathVariable Long userId,
            Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserProfileWithFriendshipResponse profile = userService.getUserProfileById(userId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", profile));
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

    @PostMapping("/push-token")
    public ResponseEntity<ApiResponse<Void>> savePushToken(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        String username = authentication.getName();
        String pushToken = request.get("pushToken");
        userService.savePushToken(username, pushToken);
        return ResponseEntity.ok(ApiResponse.success("Push token saved successfully", null));
    }
}
