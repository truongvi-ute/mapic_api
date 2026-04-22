package com.mapic.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendLocationDTO {
    private Long userId;
    private String name;
    private String username;
    private String avatarUrl;
    private Double latitude;
    private Double longitude;
    private LocalDateTime lastSeenAt;
    private Boolean isOnline;
    private LocalDateTime profileUpdatedAt; // For avatar cache invalidation
}
