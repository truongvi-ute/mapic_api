package com.mapic.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sos_alert_recipients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SOSAlertRecipient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id", nullable = false)
    private SOSAlert alert;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(name = "viewed_at")
    private LocalDateTime viewedAt;

    @Column(name = "has_responded", nullable = false)
    @Builder.Default
    private Boolean hasResponded = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (hasResponded == null) {
            hasResponded = false;
        }
    }
}
