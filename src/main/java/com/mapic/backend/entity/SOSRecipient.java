package com.mapic.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sos_recipients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SOSRecipient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sos_alert_id", nullable = false)
    private SOSAlert sosAlert;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isNotified = false;

    private LocalDateTime notifiedAt;
}
