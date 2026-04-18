package com.mapic.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "moment_media")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MomentMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moment_id", nullable = false)
    private Moment moment;

    @Column(nullable = false)
    private String mediaUrl;

    private String publicId; // For Cloudinary or AWS S3

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType mediaType;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
