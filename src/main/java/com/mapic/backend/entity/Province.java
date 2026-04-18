package com.mapic.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "provinces")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Province {
    @Id
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String region;
}
