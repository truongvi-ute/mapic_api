package com.mapic.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserProfileResponse {
    private String id;
    private String username;
    private String name;
    private String email;
    private String phone;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime lastSeenAt;
    
    // Profile details
    private String bio;
    private String avatarUrl;
    private String coverImageUrl;
    private String gender;
    private String dateOfBirth;
    private String location;
    private LocalDateTime profileUpdatedAt; // For cache busting
    
    // Admin-specific data
    private AdminUserData adminData;
    
    @Data
    @Builder
    public static class AdminUserData {
        private int totalMoments;
        private int totalFriends;
        private int totalReports;
        private int warningCount;
        private boolean isBanned;
        private String banReason;
        private LocalDateTime banExpiresAt;
        private List<String> deviceIds;
        private Double riskScore;
        private String lastKnownLocation;
    }
}
