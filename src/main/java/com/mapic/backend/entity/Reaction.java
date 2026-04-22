package com.mapic.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moment_id", nullable = true)
    private Moment moment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = true)
    private Comment comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReactionType type;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        validateTarget();
    }

    @PreUpdate
    protected void onUpdate() {
        validateTarget();
    }

    private void validateTarget() {
        if ((moment == null && comment == null) || (moment != null && comment != null)) {
            throw new IllegalStateException("Reaction must belong to exactly one target (moment or comment)");
        }
    }
}
