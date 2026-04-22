package com.mapic.backend.dto;

import com.mapic.backend.entity.NotificationPriority;
import com.mapic.backend.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private Long actorId;
    private String actorName;
    private String actorAvatar;
    private Long recipientId;
    private NotificationType type;
    private String targetType;
    private Long targetId;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private String message; // Human readable message
    
    // ========== NEW FIELDS ==========
    private NotificationPriority priority;
    private String thumbnailUrl;
    private String contentPreview;
    
    // For aggregation: multiple actors
    private List<Long> actorIds;
    private Integer actorCount;
    private List<String> actorAvatars; // For displaying stacked avatars
}

