package com.mapic.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "communes") // or "wards"
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Commune {
    @Id
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private District district;
}
