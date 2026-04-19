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
public class FriendResponse {
    private Long id;
    private String username;
    private String name;
    private String avatarUrl;
    private LocalDateTime friendsSince;
}
