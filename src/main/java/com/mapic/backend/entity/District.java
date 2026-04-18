package com.mapic.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "districts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class District {
    @Id
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Province province;
}
