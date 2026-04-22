package com.mapic.backend.controller;

import com.mapic.backend.dto.ApiResponse;
import com.mapic.backend.entity.NotificationSettings;
import com.mapic.backend.entity.NotificationType;
import com.mapic.backend.entity.User;
import com.mapic.backend.repository.UserRepository;
import com.mapic.backend.service.NotificationSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notification-settings")
@RequiredArgsConstructor
public class NotificationSettingsController {

    private final NotificationSettingsService notificationSettingsService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationSettings>>> getSettings(Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<NotificationSettings> settings = notificationSettingsService.getUserSettings(user);
        return ResponseEntity.ok(ApiResponse.success("Lấy cài đặt thông báo thành công", settings));
    }

    @PutMapping("/{type}")
    public ResponseEntity<ApiResponse<NotificationSettings>> updateSetting(
            @PathVariable NotificationType type,
            @RequestBody Map<String, Boolean> updates,
            Authentication authentication) {
        
        User user = getCurrentUser(authentication);
        
        Boolean enabled = updates.get("enabled");
        Boolean pushEnabled = updates.get("pushEnabled");
        Boolean soundEnabled = updates.get("soundEnabled");
        
        NotificationSettings settings = notificationSettingsService.updateSetting(
                user, type, enabled, pushEnabled, soundEnabled);
        
        return ResponseEntity.ok(ApiResponse.success("Cập nhật cài đặt thành công", settings));
    }

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
