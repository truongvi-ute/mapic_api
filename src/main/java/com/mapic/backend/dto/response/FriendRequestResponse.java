package com.mapic.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendRequestResponse {
    private Long id;
    private Long senderId;
    private String senderName;
    private String senderUsername;
    private String senderAvatarUrl;
    private LocalDateTime createdAt;
}
