package com.mapic.backend.controller;

import com.mapic.backend.dto.ApiResponse;
import com.mapic.backend.service.IStorageService;
import com.mapic.backend.repository.UserProfileRepository;
import com.mapic.backend.entity.UserProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
@Slf4j
public class MaintenanceController {

    private final IStorageService storageService;
    private final UserProfileRepository userProfileRepository;

    @PostMapping("/cleanup-broken-avatars")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cleanupBrokenAvatars() {
        log.info("Starting cleanup of broken avatar references");
        
        List<UserProfile> profiles = userProfileRepository.findAll();
        int totalProfiles = profiles.size();
        int brokenAvatars = 0;
        int brokenCovers = 0;
        
        for (UserProfile profile : profiles) {
            boolean updated = false;
            
            // Check avatar
            if (profile.getAvatarUrl() != null) {
                String avatarUrl = profile.getAvatarUrl();
                // Skip external URLs
                if (!avatarUrl.startsWith("http://") && !avatarUrl.startsWith("https://")) {
                    if (!storageService.exists(avatarUrl, "avatars")) {
                        log.warn("Removing broken avatar reference for user {}: {}", 
                                profile.getUser().getUsername(), avatarUrl);
                        profile.setAvatarUrl(null);
                        brokenAvatars++;
                        updated = true;
                    }
                }
            }
            
            // Check cover image
            if (profile.getCoverImageUrl() != null) {
                String coverUrl = profile.getCoverImageUrl();
                // Skip external URLs
                if (!coverUrl.startsWith("http://") && !coverUrl.startsWith("https://")) {
                    if (!storageService.exists(coverUrl, "covers")) {
                        log.warn("Removing broken cover reference for user {}: {}", 
                                profile.getUser().getUsername(), coverUrl);
                        profile.setCoverImageUrl(null);
                        brokenCovers++;
                        updated = true;
                    }
                }
            }
            
            if (updated) {
                userProfileRepository.save(profile);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalProfiles", totalProfiles);
        result.put("brokenAvatarsFixed", brokenAvatars);
        result.put("brokenCoversFixed", brokenCovers);
        
        log.info("Cleanup completed: {} profiles checked, {} broken avatars fixed, {} broken covers fixed", 
                totalProfiles, brokenAvatars, brokenCovers);
        
        return ResponseEntity.ok(ApiResponse.success("Cleanup completed successfully", result));
    }
}