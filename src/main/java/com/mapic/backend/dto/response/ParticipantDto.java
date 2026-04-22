package com.mapic.backend.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantDto {
    private Long userId;
    private String username;
    private String fullName;
    private String avatarUrl;
    private String role; // ADMIN, MEMBER
}
