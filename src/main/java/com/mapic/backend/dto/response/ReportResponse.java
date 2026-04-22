package com.mapic.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReportResponse {
    private String id;
    private String reportedContentId;
    private String contentType; // MOMENT, COMMENT, USER_PROFILE
    private String reason;
    private String description;
    private String status; // PENDING, REVIEWED, RESOLVED, DISMISSED
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    
    // Reporter info
    private ReporterInfo reporter;
    
    // Reported user info
    private ReportedUserInfo reportedUser;
    
    // Content info
    private ContentInfo content;
    
    @Data
    @Builder
    public static class ReporterInfo {
        private String id;
        private String username;
        private String name;
        private String avatarUrl;
    }
    
    @Data
    @Builder
    public static class ReportedUserInfo {
        private String id;
        private String username;
        private String name;
        private String avatarUrl;
        private int totalReports;
        private boolean isBanned;
    }
    
    @Data
    @Builder
    public static class ContentInfo {
        private String id;
        private String type;
        private String content;
        private String mediaUrl;
        private LocalDateTime createdAt;
        private boolean isDeleted;
    }
}
