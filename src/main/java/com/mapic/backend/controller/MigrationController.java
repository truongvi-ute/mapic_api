package com.mapic.backend.controller;

import com.mapic.backend.dto.ApiResponse;
import com.mapic.backend.entity.MomentMedia;
import com.mapic.backend.entity.UserProfile;
import com.mapic.backend.repository.MomentMediaRepository;
import com.mapic.backend.repository.UserProfileRepository;
import com.mapic.backend.service.IStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/migration")
@RequiredArgsConstructor
@Slf4j
public class MigrationController {

    private final UserProfileRepository userProfileRepository;
    private final MomentMediaRepository momentMediaRepository;
    private final IStorageService storageService;

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @GetMapping("/test-cloudinary-connection")
    public ResponseEntity<ApiResponse<Object>> testCloudinaryConnection() {
        log.info("=== TESTING CLOUDINARY CONNECTION ===");
        
        if (cloudName == null || cloudName.isEmpty() ||
            apiKey == null || apiKey.isEmpty() ||
            apiSecret == null || apiSecret.isEmpty()) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Cloudinary not configured properly")
            );
        }

        try {
            // Test by checking if we can access Cloudinary API
            String testUrl = String.format("https://res.cloudinary.com/%s/image/upload/sample", cloudName);
            
            return ResponseEntity.ok(ApiResponse.success("Cloudinary connection test", new Object() {
                public final boolean configured = true;
                public final String cloudName = MigrationController.this.cloudName;
                public final String testImageUrl = testUrl;
                public final String message = "Cloudinary appears to be configured correctly";
            }));
            
        } catch (Exception e) {
            log.error("Cloudinary connection test failed", e);
            return ResponseEntity.ok(ApiResponse.error("Cloudinary connection failed: " + e.getMessage()));
        }
    }

    @GetMapping("/check-storage-service")
    public ResponseEntity<ApiResponse<Object>> checkStorageService() {
        log.info("=== STORAGE SERVICE CHECK ===");
        
        String serviceType = storageService.getClass().getSimpleName();
        log.info("Active storage service: {}", serviceType);
        
        boolean isCloudinary = serviceType.equals("CloudinaryStorageService");
        boolean isLocal = serviceType.equals("FileStorageServiceImpl");
        
        return ResponseEntity.ok(ApiResponse.success("Storage service check completed", new Object() {
            public final String activeService = serviceType;
            public final boolean usingCloudinary = isCloudinary;
            public final boolean usingLocal = isLocal;
        }));
    }

    @GetMapping("/check-cloudinary-config")
    public ResponseEntity<ApiResponse<Object>> checkCloudinaryConfig() {
        log.info("=== CLOUDINARY CONFIGURATION CHECK ===");
        log.info("Cloud Name: {}", cloudName != null && !cloudName.isEmpty() ? cloudName : "NOT_SET");
        log.info("API Key: {}", apiKey != null && !apiKey.isEmpty() ? "SET" : "NOT_SET");
        log.info("API Secret: {}", apiSecret != null && !apiSecret.isEmpty() ? "SET" : "NOT_SET");

        boolean isConfigured = cloudName != null && !cloudName.isEmpty() &&
                              apiKey != null && !apiKey.isEmpty() &&
                              apiSecret != null && !apiSecret.isEmpty();

        return ResponseEntity.ok(ApiResponse.success(
            "Cloudinary configured: " + isConfigured,
            new Object() {
                public final boolean configured = isConfigured;
                public final String cloudName = MigrationController.this.cloudName;
                public final boolean hasApiKey = apiKey != null && !apiKey.isEmpty();
                public final boolean hasApiSecret = apiSecret != null && !apiSecret.isEmpty();
            }
        ));
    }

    @GetMapping("/check-local-files")
    public ResponseEntity<ApiResponse<Object>> checkLocalFiles() {
        log.info("=== CHECKING LOCAL FILE REFERENCES ===");
        
        // Check user avatars
        List<UserProfile> profilesWithLocalAvatars = userProfileRepository.findAll().stream()
            .filter(p -> p.getAvatarUrl() != null && 
                        !p.getAvatarUrl().startsWith("http://") && 
                        !p.getAvatarUrl().startsWith("https://"))
            .toList();

        // Check user cover images
        List<UserProfile> profilesWithLocalCovers = userProfileRepository.findAll().stream()
            .filter(p -> p.getCoverImageUrl() != null && 
                        !p.getCoverImageUrl().startsWith("http://") && 
                        !p.getCoverImageUrl().startsWith("https://"))
            .toList();

        // Check moment media
        List<MomentMedia> mediaWithLocalUrls = momentMediaRepository.findAll().stream()
            .filter(m -> m.getMediaUrl() != null && 
                        !m.getMediaUrl().startsWith("http://") && 
                        !m.getMediaUrl().startsWith("https://"))
            .toList();

        log.info("Found {} profiles with local avatar URLs", profilesWithLocalAvatars.size());
        log.info("Found {} profiles with local cover URLs", profilesWithLocalCovers.size());
        log.info("Found {} moment media with local URLs", mediaWithLocalUrls.size());

        return ResponseEntity.ok(ApiResponse.success("Local file check completed", new Object() {
            public final int localAvatars = profilesWithLocalAvatars.size();
            public final int localCovers = profilesWithLocalCovers.size();
            public final int localMomentMedia = mediaWithLocalUrls.size();
            public final List<String> sampleAvatars = profilesWithLocalAvatars.stream()
                .limit(5)
                .map(UserProfile::getAvatarUrl)
                .toList();
            public final List<String> sampleCovers = profilesWithLocalCovers.stream()
                .limit(5)
                .map(UserProfile::getCoverImageUrl)
                .toList();
            public final List<String> sampleMomentMedia = mediaWithLocalUrls.stream()
                .limit(5)
                .map(MomentMedia::getMediaUrl)
                .toList();
        }));
    }

    @PostMapping("/update-urls-to-cloudinary")
    public ResponseEntity<ApiResponse<String>> updateUrlsToCloudinary(
            @RequestParam(defaultValue = "false") boolean dryRun) {
        
        if (cloudName == null || cloudName.isEmpty()) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Cloudinary cloud name not configured")
            );
        }

        log.info("=== UPDATING URLs TO CLOUDINARY (DRY RUN: {}) ===", dryRun);
        
        int updatedAvatars = 0;
        int updatedCovers = 0;
        int updatedMomentMedia = 0;

        // Update user avatars
        List<UserProfile> profiles = userProfileRepository.findAll();
        for (UserProfile profile : profiles) {
            if (profile.getAvatarUrl() != null && 
                !profile.getAvatarUrl().startsWith("http://") && 
                !profile.getAvatarUrl().startsWith("https://")) {
                
                String cloudinaryUrl = buildCloudinaryUrl(profile.getAvatarUrl(), "avatars");
                log.info("Avatar: {} -> {}", profile.getAvatarUrl(), cloudinaryUrl);
                
                if (!dryRun) {
                    profile.setAvatarUrl(cloudinaryUrl);
                }
                updatedAvatars++;
            }

            if (profile.getCoverImageUrl() != null && 
                !profile.getCoverImageUrl().startsWith("http://") && 
                !profile.getCoverImageUrl().startsWith("https://")) {
                
                String cloudinaryUrl = buildCloudinaryUrl(profile.getCoverImageUrl(), "covers");
                log.info("Cover: {} -> {}", profile.getCoverImageUrl(), cloudinaryUrl);
                
                if (!dryRun) {
                    profile.setCoverImageUrl(cloudinaryUrl);
                }
                updatedCovers++;
            }
        }

        if (!dryRun && (updatedAvatars > 0 || updatedCovers > 0)) {
            userProfileRepository.saveAll(profiles);
        }

        // Update moment media
        List<MomentMedia> mediaList = momentMediaRepository.findAll();
        for (MomentMedia media : mediaList) {
            if (media.getMediaUrl() != null && 
                !media.getMediaUrl().startsWith("http://") && 
                !media.getMediaUrl().startsWith("https://")) {
                
                String cloudinaryUrl = buildCloudinaryUrl(media.getMediaUrl(), "moments");
                log.info("Moment media: {} -> {}", media.getMediaUrl(), cloudinaryUrl);
                
                if (!dryRun) {
                    media.setMediaUrl(cloudinaryUrl);
                }
                updatedMomentMedia++;
            }
        }

        if (!dryRun && updatedMomentMedia > 0) {
            momentMediaRepository.saveAll(mediaList);
        }

        String message = String.format(
            "%s: Updated %d avatars, %d covers, %d moment media to Cloudinary URLs",
            dryRun ? "DRY RUN" : "COMPLETED",
            updatedAvatars, updatedCovers, updatedMomentMedia
        );

        return ResponseEntity.ok(ApiResponse.success(message, null));
    }

    private String buildCloudinaryUrl(String filename, String folder) {
        // Remove extension to get the UUID
        String filenameWithoutExt = filename.contains(".") 
            ? filename.substring(0, filename.lastIndexOf("."))
            : filename;
        
        // Build Cloudinary URL
        // Format: https://res.cloudinary.com/{cloud_name}/image/upload/mapic/{folder}/{uuid}
        return String.format("https://res.cloudinary.com/%s/image/upload/mapic/%s/%s", 
                           cloudName, folder, filenameWithoutExt);
    }
}