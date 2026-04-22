package com.mapic.backend.controller;

import com.mapic.backend.dto.ApiResponse;
import com.mapic.backend.entity.User;
import com.mapic.backend.entity.UserStatus;
import com.mapic.backend.repository.UserRepository;
import com.mapic.backend.repository.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final Random random = new Random();

    /**
     * Force seed GPS coordinates for all users
     * Useful for testing map features
     */
    @PostMapping("/seed-gps")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> seedGPSCoordinates() {
        log.info("[Admin] Force seeding GPS coordinates for all users...");
        
        List<User> users = userRepository.findAll();
        
        // Base GPS coordinates (around Ho Chi Minh City)
        double baseLat = 10.7361;
        double baseLng = 106.9487;
        
        int count = 0;
        for (User user : users) {
            // Check if UserStatus already exists
            UserStatus status = userStatusRepository.findById(user.getId())
                    .orElse(UserStatus.builder()
                            .user(user)
                            .build());
            
            // Add random offset to create different locations
            double latOffset = (random.nextDouble() - 0.5) * 0.01; // ~500m radius
            double lngOffset = (random.nextDouble() - 0.5) * 0.01;
            
            status.setLastLat(baseLat + latOffset);
            status.setLastLng(baseLng + lngOffset);
            status.setLastSeenAt(LocalDateTime.now());
            status.setIsSharingLocation(true);
            status.setBatteryLevel(80 + random.nextInt(20));
            
            userStatusRepository.save(status);
            count++;
            
            log.info("[Admin] Set GPS for user {}: {}, {}", 
                    user.getUsername(), 
                    status.getLastLat(), 
                    status.getLastLng());
        }
        
        return ResponseEntity.ok(ApiResponse.success(
                "GPS coordinates seeded for " + count + " users", 
                Map.of("count", count)));
    }
}
