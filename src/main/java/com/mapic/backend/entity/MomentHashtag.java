package com.mapic.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "moment_hashtags")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MomentHashtag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moment_id", nullable = false)
    private Moment moment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hashtag_id", nullable = false)
    private Hashtag hashtag;
}
