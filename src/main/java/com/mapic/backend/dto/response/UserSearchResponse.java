package com.mapic.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSearchResponse {
    private Long id;
    private String username;
    private String name;
    private String avatarUrl;
    private String friendshipStatus; // NONE, PENDING_SENT, PENDING_RECEIVED, FRIENDS
}
