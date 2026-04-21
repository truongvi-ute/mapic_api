package com.mapic.backend.dto.response;

import com.mapic.backend.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileWithFriendshipResponse {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String phone;
    private String bio;
    private String avatarUrl;
    private String coverImageUrl;
    private Gender gender;
    private LocalDate dateOfBirth;
    private FriendshipStatus friendshipStatus;
    
    public enum FriendshipStatus {
        NONE,              // No relationship
        PENDING_SENT,      // Current user sent friend request
        PENDING_RECEIVED,  // Current user received friend request
        FRIENDS            // Already friends
    }
}
