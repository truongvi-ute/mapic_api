package com.mapic.backend.dto;

import com.mapic.backend.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String name;
    private String phone;
    private String bio;
    private String avatarUrl;
    private String coverImageUrl;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String location;
    private LocalDateTime profileUpdatedAt; // For cache busting
}
