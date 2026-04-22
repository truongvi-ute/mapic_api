package com.mapic.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ContentAnalyticsResponse {
    private OverallStats overall;
    private List<TrendData> trends;
    private Map<String, Integer> reportsByReason;
    private Map<String, Integer> reportsByType;
    private List<TopReportedContent> topReported;
    
    @Data
    @Builder
    public static class OverallStats {
        private int totalReports;
        private int pendingReports;
        private int resolvedReports;
        private int totalMoments;
        private int totalComments;
        private int deletedContent;
        private double resolutionRate;
        private double averageResolutionTime; // in hours
    }
    
    @Data
    @Builder
    public static class TrendData {
        private LocalDateTime date;
        private int newReports;
        private int resolvedReports;
        private int newContent;
        private int deletedContent;
    }
    
    @Data
    @Builder
    public static class TopReportedContent {
        private String contentId;
        private String contentType;
        private String content;
        private int reportCount;
        private String status;
        private String authorUsername;
    }
}
