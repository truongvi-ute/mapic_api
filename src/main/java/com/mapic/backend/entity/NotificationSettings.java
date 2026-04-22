package com.mapic.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_settings", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "notification_type"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    // Enable/disable in-app notification
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    // Enable/disable push notification
    @Column(nullable = false)
    @Builder.Default
    private Boolean pushEnabled = true;

    // Enable/disable sound
    @Column(nullable = false)
    @Builder.Default
    private Boolean soundEnabled = true;
}
