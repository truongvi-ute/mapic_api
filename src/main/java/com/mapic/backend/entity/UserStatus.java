package com.mapic.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_statuses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatus {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private Double lastLat;
    private Double lastLng;
    private LocalDateTime lastSeenAt;
    private Integer batteryLevel;

    @Builder.Default
    private Boolean isSharingLocation = true;
    
    private String statusMessage;
}
