package com.mapic.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    private Long targetId;
    
    @Column(length = 50)
    private String targetType; // MOMENT, COMMENT, FRIENDSHIP

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    // ========== NEW FIELDS FOR ENHANCEMENT ==========
    
    // Priority: HIGH (SOS), NORMAL (friend requests), LOW (reactions)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.NORMAL;
    
    // Rich media: thumbnail of moment/photo
    @Column(length = 500)
    private String thumbnailUrl;
    
    // Content preview: snippet of comment, message, etc.
    @Column(length = 200)
    private String contentPreview;
    
    // Aggregation: store multiple actor IDs as comma-separated string
    // Example: "1,2,3,4,5" means users 1,2,3,4,5 all liked the same post
    @Column(length = 1000)
    private String actorIds;
    
    // Count of actors (for "John and 5 others")
    @Builder.Default
    private Integer actorCount = 1;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
